package io.anonero.services

import android.content.SharedPreferences
import android.util.Log
import io.anonero.AnonConfig
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.model.node.DaemonInfo
import io.anonero.model.node.Node
import io.anonero.model.node.NodeFields
import io.anonero.util.PREFS_PIN_HASH
import io.anonero.util.PinHash
import io.anonero.util.RESTORE_HEIGHT
import io.anonero.util.WalletKey
import io.anonero.util.WALLET_PROXY
import io.anonero.util.WALLET_PROXY_PORT
import io.anonero.util.WALLET_USE_TOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.json.JSONObject
import java.io.File
import timber.log.Timber
import androidx.core.content.edit


class InvalidPin : Exception("invalid pin")

private const val TAG = "AnonWalletHandler"

class UnableToCloseWallet : Exception()

class AnonWalletHandler(
    private val prefs: SharedPreferences,
    private val walletState: WalletState,
    private val torService: TorService
) {

    private val _scope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    private var handler: MoneroHandlerThread? = null

    val scope get() = _scope

    init {
        scope.launch {
            torService.socksFlow.collect {
                val wallet = WalletManager.instance?.wallet
                if (prefs.getBoolean(WALLET_USE_TOR, true) && wallet?.isInitialized == true) {
                    setProxy(it.address.toString(), it.port.value)
                }
            }
        }
    }

    fun openWallet(pin: String): Boolean {
        val context = AnonConfig.context!!
        val walletFile = AnonConfig.getDefaultWalletFile(context)
        val walletName = walletFile.name

        val anonWallet = if (WalletKey.exists(context, walletName)) {
            // Keystore-bound: the PIN never encrypts the wallet, it gates access to the
            // wrapped secret. Verify it in software, then open with that secret.
            if (!PinHash.matches(pin, prefs.getString(PREFS_PIN_HASH, ""))) throw InvalidPin()
            val key = try {
                WalletKey.load(context, walletName)
            } catch (e: Exception) {
                // key gone (device change, Keystore reset) -- the wallet cannot be opened
                // on this device; the seed/backup is the only recovery
                Timber.tag(TAG).e(e, "wallet key unavailable")
                throw InvalidPin()
            }
            WalletManager.instance?.openWallet(walletFile.path, key)
        } else {
            // Legacy PIN-encrypted wallet: opening with the PIN verifies it. On success,
            // migrate it to a Keystore-bound key.
            val opened = WalletManager.instance?.openWallet(walletFile.path, pin)
            if (opened?.status?.isOk == true) {
                migrateToKeystore(walletFile, walletName, opened, pin)
            }
            opened
        }

        if (anonWallet?.status?.isOk != true) {
            Timber.tag(TAG).e("openWallet error: %s", anonWallet?.status?.errorString)
        }
        return anonWallet?.status?.isOk ?: throw InvalidPin()
    }

    /**
     * One-time migration of a legacy PIN-encrypted wallet to a Keystore-wrapped random
     * key. Snapshot the keys file, re-encrypt with the new secret, wrap it, then drop the
     * snapshot. Any failure rolls back to the snapshot and leaves the wallet
     * PIN-encrypted -- still openable. A crash between re-encrypt and wrap is caught at
     * startup by WalletKey.recoverInterruptedMigration. The seed is the ultimate net.
     */
    private fun migrateToKeystore(walletFile: File, walletName: String, wallet: Wallet, pin: String) {
        val context = AnonConfig.context!!
        // wallet2 keys the cache with the password as well as the keys file, so both are
        // snapshotted and both are restored together
        val snapshots = WalletKey.migrationSnapshots(walletFile.path)
        var rekeyed = false
        try {
            snapshots.forEach { (snapshot, live) ->
                // copy to a temp file, then rename it to the snapshot
                val partial = File(snapshot.path + ".tmp")
                live.copyTo(partial, overwrite = true)
                if (!partial.renameTo(snapshot)) throw Exception("snapshot failed: ${snapshot.name}")
            }
            val key = WalletKey.newSecret()
            rekeyed = true  // from here the files on disk may already be keyed to `key`
            if (!wallet.setPassword(key)) {
                throw Exception("setPassword failed: ${wallet.status.errorString}")
            }
            wallet.store()
            WalletKey.store(context, walletName, key)
            // the gate must match this exact PIN, which just opened the wallet; always
            // rewrite it rather than trusting whatever was there
            prefs.edit(commit = true) { putString(PREFS_PIN_HASH, PinHash.hash(pin)) }
            snapshots.forEach { (snapshot, _) -> snapshot.delete() }
            Timber.tag(TAG).i("wallet migrated to a keystore-bound key")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "keystore migration failed -- rolling back")
            WalletKey.remove(context, walletName)
            // Put the open wallet back on the PIN first: it keeps storing on every refresh,
            // and a store keyed to the abandoned secret would undo a file-level rollback.
            // That re-writes keys and cache from memory, current and PIN-keyed.
            val restoredInMemory = !rekeyed || runCatching { wallet.setPassword(pin) }.getOrDefault(false)
            if (!restoredInMemory) {
                runCatching {
                    snapshots.forEach { (snapshot, live) ->
                        if (snapshot.exists()) snapshot.copyTo(live, overwrite = true)
                    }
                }
                Timber.tag(TAG).e("could not re-key the open wallet to the PIN; restored snapshot, restart required")
            }
            snapshots.forEach { (snapshot, _) -> snapshot.delete() }
        }
    }

    suspend fun startService() {
        val wallet = WalletManager.instance?.wallet ?: return
        handler = MoneroHandlerThread(
            wallet,
            walletState
        )

        wallet.setListener(handler)
        wallet.refreshHistory()
        handler?.start()
        val host = prefs.getString(NodeFields.RPC_HOST.value, "")
        val offline = !AnonConfig.viewOnly && host.isNullOrEmpty()
        walletState.setOffline(offline)
        wallet.setOffline(offline)
        //nothing to load without a node
        if (!host.isNullOrEmpty()) walletState.setLoading(true)
        walletState.update()
        try {
            val rpcPort = prefs.getInt(NodeFields.RPC_PORT.value, Node.defaultRpcPort)
            val rpcUsername = prefs.getString(NodeFields.RPC_USERNAME.value, "")
            val rpcPassphrase = prefs.getString(NodeFields.RPC_PASSWORD.value, "")
            // no node (offline signer): nothing to route, leave Tor off
            if (host?.isNotEmpty() == true) applyProxy()
            if (host?.isNotEmpty() == true) {
                val nodeObj = JSONObject()
                    .apply {
                        put(NodeFields.RPC_HOST.value, host)
                        put(NodeFields.RPC_PORT.value, rpcPort)
                        put(NodeFields.RPC_USERNAME.value, rpcUsername)
                        put(NodeFields.RPC_PASSWORD.value, rpcPassphrase)
                        put(NodeFields.RPC_NETWORK.value, AnonConfig.getNetworkType().toString())
                        put(NodeFields.NODE_NAME.value, "anon")
                    }
                val node = Node.fromJson(nodeObj)
                updateDaemon(node)
                walletState.setLoading(true)
            }
            walletState.update()
            if(wallet.isSynchronized) {
                wallet.refreshHistory()
            }
            wallet.init(0)
            if(prefs.getLong(RESTORE_HEIGHT, 0L)!=0L) {
                wallet.setRestoreHeight(prefs.getLong(RESTORE_HEIGHT, 0L));
            }
            if (wallet.isInitialized) {
                wallet.refreshHistory()
                wallet.setTrustedDaemon(true)
                wallet.startRefresh()
                walletState.update()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            walletState.setLoading(false)
        }

    }

    /**
     * Routes the wallet through Tor (waiting for its socks port) or the manual proxy. Call it
     * before the wallet first talks to a node, or that traffic goes out unproxied.
     */
    suspend fun applyProxy() {
        val proxyHost = prefs.getString(WALLET_PROXY, "")
        val proxyPort = prefs.getInt(WALLET_PROXY_PORT, -1)
        val useTor = prefs.getBoolean(WALLET_USE_TOR, true)
        if (useTor || proxyHost.isNullOrBlank()) {
            torService.start();
            while (torService.socks == null) {
                delay(200)
            }
            val socket = torService.socks
            WalletManager.instance?.setProxy("${socket?.value}")
        } else if (proxyHost.isNotEmpty() && proxyPort != -1) {
            WalletManager.instance?.setProxy("${proxyHost}:$proxyPort")
        } else {
            throw Exception("no proxy")
        }
    }

    fun updateDaemon(node: Node?) {
        walletState.setLoading(true)
        val wallet = WalletManager.instance?.wallet
        val walletManager = WalletManager.instance
        wallet?.pauseRefresh()
        try {
            if (node != null) {
                walletState.setOffline(false)
                wallet?.setOffline(false)
                walletState.updateDaemon(
                    DaemonInfo(
                        daemon = "${node.host}:${node.rpcPort}",
                        Wallet.ConnectionStatus.ConnectionStatus_Disconnected,
                        daemonHeight = 0L
                    )
                )
            } else {
                walletState.updateDaemon(
                    DaemonInfo(
                        daemon = null,
                        Wallet.ConnectionStatus.ConnectionStatus_Disconnected,
                        daemonHeight = 0L
                    )
                )
            }
            walletManager?.setDaemon(node)
            wallet?.setTrustedDaemon(true)
            walletState.setLoading(false)
        } catch (e: Exception) {
            walletState.updateDaemon(
                DaemonInfo(
                    daemon = null,
                    Wallet.ConnectionStatus.ConnectionStatus_Disconnected,
                    daemonHeight = 0L
                )
            )
            e.printStackTrace()
        }

    }

    fun setProxy(proxy: String?, port: Int?) {
        Timber.tag(TAG).d("setProxy %s%s", proxy, port.toString())
        //disable proxy
        if (proxy == null && port == null) {
            prefs.edit {
                remove(WALLET_PROXY)
                remove(WALLET_PROXY_PORT)
            }
        } else {
            prefs.edit {
                putString(WALLET_PROXY, proxy)
                putInt(WALLET_PROXY_PORT, port ?: -1)
            }
            val proxyHost = prefs.getString(WALLET_PROXY, "")
            val proxyPort = prefs.getInt(WALLET_PROXY_PORT, -1)
            val proxyStr = if (proxyHost?.isNotEmpty() == true && proxyPort != -1) {
                "${proxyHost}:$proxyPort"
            } else {
                return
            }
            WalletManager.instance?.setProxy(proxyStr)
            WalletManager.instance?.wallet?.setProxy(proxyStr)
        }
    }

    fun getProxy(): Pair<String, Int>? {
        val proxyHost = prefs.getString(WALLET_PROXY, "")
        val proxyPort = prefs.getInt(WALLET_PROXY_PORT, -1)

        if (proxyHost?.isNotEmpty() == true && proxyPort != -1) {
            return Pair(proxyHost, proxyPort)
        }
        return null
    }

    fun wipe(passPhrase: String): Boolean {
        WalletManager.instance?.wallet?.pauseRefresh()
        WalletManager.instance?.wallet?.stopBackgroundSync(passPhrase)
        WalletManager.instance?.setDaemon(null)
        if (WalletManager.instance?.wallet?.close() == true) {
            AnonConfig.context?.let { context ->
                val walletName = AnonConfig.getDefaultWalletFile(context).name
                AnonConfig.getDefaultWalletDir(context).deleteRecursively()
                // the wrapped wallet secret and its Keystore key outlive the files otherwise
                runCatching { WalletKey.remove(context, walletName) }
            }
            return true
        } else {
            throw UnableToCloseWallet()
        }

    }

}
