package io.anonero.util.backup

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import androidx.core.content.edit
import androidx.core.net.toUri
import io.anonero.AnonConfig
import io.anonero.R
import io.anonero.di.provideWalletSharedPrefs
import io.anonero.model.Backup
import io.anonero.model.BackupMeta
import io.anonero.model.BackupPayload
import io.anonero.model.NeroKeyPayload
import io.anonero.model.NodeBackup
import io.anonero.model.WalletBackup
import io.anonero.model.WalletManager
import io.anonero.model.node.Node
import io.anonero.model.node.NodeFields
import io.anonero.store.NodesRepository
import io.anonero.util.KeyStoreHelper
import io.anonero.util.PREFS_PIN_HASH
import io.anonero.util.WalletKey
import io.anonero.util.PinHash
import io.anonero.util.PREFS_PASSPHRASE_HASH
import io.anonero.util.RESTORE_HEIGHT
import io.anonero.util.WALLET_PREFERENCES
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class NetworkMismatchException : Exception()
object BackupHelper {
    // 3: wallet files inside the zip are keyed to the seed passphrase (portable).
    // 2 and below: keyed to the PIN of the phone that made the backup -- which is also
    // what a wallet that has not migrated to the Keystore still exports.
    private const val BACKUP_VERSION = 3
    private const val BACKUP_VERSION_PIN_KEYED = 2
    const val BUFFER = 2048
    private const val TAG = "BackUpHelper"
    val walletPrefs: SharedPreferences by inject(
        SharedPreferences::class.java, named(WALLET_PREFERENCES)
    )
    val nodeRepository: NodesRepository by inject(NodesRepository::class.java)

    suspend fun createBackUp(seedPassphrase: String, context: Context): String {
        val wallet = WalletManager.instance?.wallet ?: throw Exception("Wallet not found")
        val prefs = provideWalletSharedPrefs(context)


        val walletPayload = WalletBackup(
            address = wallet.address,
            seed = wallet.getSeed(seedPassphrase),
            restoreHeight = wallet.getRestoreHeight(),
            balanceAll = wallet.getBalanceAll(),
            numSubaddresses = wallet.numSubAddresses,
            numAccounts = wallet.getNumAccounts(),
            primaryAddress = wallet.address,
            isWatchOnly = wallet.isWatchOnly(),
            isSynchronized = wallet.isSynchronized,
            neroPayload = NeroKeyPayload.fromWallet(wallet),
        );


        val nodeBackup = NodeBackup(
            host = prefs.getString(NodeFields.RPC_HOST.value, "") ?: "",
            password = prefs.getString(NodeFields.RPC_PASSWORD.value, "") ?: "",
            username = prefs.getString(NodeFields.RPC_USERNAME.value, "") ?: "",
            rpcPort = prefs.getInt(NodeFields.RPC_PORT.value, Node.defaultRpcPort),
            networkType = AnonConfig.getNetworkType().toString(),
            isOnion = false
        )
        val backup = Backup(
            meta = BackupMeta(
                timestamp = System.currentTimeMillis(),
                network = AnonConfig.getNetworkType().toStringForBackUp(),
            ), node = nodeBackup, wallet = walletPayload,
            nodes = nodeRepository.getAll()
        )

        // The version tells restore which key opens the wallet files. A keystore-bound
        // wallet's copy is re-keyed to the passphrase below (v3); a wallet that never
        // migrated is still PIN-keyed and must say so (v2), or restore asks for a new PIN
        // instead of the one the files are locked with. If the device secret exists but
        // cannot be loaded, fail the export rather than write a backup nothing can open.
        val walletName = AnonConfig.getDefaultWalletFile(context).name
        val w = if (WalletKey.exists(context, walletName)) WalletKey.load(context, walletName) else null
        val backUpPayload = BackupPayload(
            version = if (w != null) BACKUP_VERSION else BACKUP_VERSION_PIN_KEYED,
            backup = backup,
        )
        val json = Json.encodeToString(backUpPayload)

        context.cacheDir.deleteRecursively()

        val tmpBackupDir = File(context.cacheDir, "tmp_backup")
        if (tmpBackupDir.exists()) {
            tmpBackupDir.deleteRecursively()
        }
        val date = Date()
        val sdf = SimpleDateFormat("dd_MM_yyyy' 'HH_mm_a", Locale.getDefault())
        val timeStamp: String = sdf.format(date)
        val backupFile = File(context.cacheDir, "${R.string.app_name}_backup_$timeStamp.zip")
        tmpBackupDir.mkdirs()
        try {
            val tmpBackupFile = File(tmpBackupDir, "anon.json")
            tmpBackupFile.writeText(json)
            val walletDir = File(context.filesDir, "wallets")
            // Quiesce background sync before copying: MoneroHandlerThread calls store() on
            // every refresh, and a store() landing mid-copy throws. Pause + settle, copy,
            // then resume. The store() is what puts the live sync state into the cache file.
            wallet.pauseRefresh()
            wallet.store()
            // keep the live wallet paused until the copy is re-keyed and closed
            try {
                walletDir.copyRecursively(tmpBackupDir, true)
                // The live wallet is keyed to the device secret W, which exists only in this
                // phone's Keystore, so a raw copy opens nowhere else. Re-key the COPY to the seed
                // passphrase -- the same one that decrypts the zip -- so the backup restores on
                // any device, with its full cache (history, sync state). The live wallet is never
                // touched: nothing here can leave it in a state the Keystore cannot open. A legacy
                // wallet that never migrated has no W and is still PIN-keyed; it is left as is.
                if (w != null) {
                    val manager = WalletManager.instance ?: throw Exception("Wallet manager not found")
                    val copy = manager.openWalletUnmanaged(File(tmpBackupDir, walletName).path, w)
                    try {
                        if (!copy.status.isOk) {
                            throw Exception("backup copy did not open: ${copy.status.errorString}")
                        }
                        if (!copy.setPassword(seedPassphrase)) {
                            throw Exception("backup copy could not be re-keyed: ${copy.status.errorString}")
                        }
                        copy.store()
                    } finally {
                        manager.closeUnmanaged(copy)
                    }
                }
            } finally {
                if (wallet.isInitialized) wallet.startRefresh()
            }
            val list = tmpBackupDir.listFiles()
            val files = list?.map { it.absolutePath }?.toTypedArray()
            val backupCacheDir = File(context.cacheDir, "backup")
            if (!backupCacheDir.exists()) {
                backupCacheDir.mkdirs()
            }
            val backupFileEncrypted =
                File(backupCacheDir, "${R.string.app_name}_backup_$timeStamp.anon")

            if (files != null) {
                zip(files, backupFile.absolutePath)
                EncryptUtil.encryptFile(seedPassphrase, backupFile, backupFileEncrypted)
            }
            return backupFileEncrypted.absolutePath
        } finally {
            // delete the unencrypted copies
            tmpBackupDir.deleteRecursively()
            backupFile.delete()
        }
    }

    fun testBackUp(destinationDir: File): Boolean {
        val items = destinationDir.listFiles()?.toList()?.filter {
            (it.name.endsWith(".keys") || it.name.endsWith(".json"))
        }
        return items?.size == 2
    }

    private fun zip(_files: Array<String>, zipFileName: String?) {
        try {
            var origin: BufferedInputStream?
            val dest = FileOutputStream(zipFileName)
            val out = ZipOutputStream(
                BufferedOutputStream(
                    dest
                )
            )
            val data = ByteArray(BUFFER)
            for (i in _files.indices) {
                val fi = FileInputStream(_files[i])
                origin = BufferedInputStream(fi, BUFFER)
                val entry = ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1))
                out.putNextEntry(entry)
                var count: Int
                while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                    out.write(data, 0, count)
                }
                origin.close()
            }
            out.close()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
            Timber.tag(TAG).e(e)
        }
    }

    fun unZip(toUnzip: File, destinationDir: File) {
        destinationDir.apply {
            if (!exists()) {
                mkdirs()
            }
        }
        ZipFile(toUnzip).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = "${destinationDir}${File.separator}${entry.name}"
                    if (!entry.isDirectory) {
                        input.copyTo(File(filePath).apply { createNewFile() }.outputStream())
                    } else {
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                }
            }
        }
    }


    /**
     * Binds freshly-restored wallet files to this device. A version-3 backup's files are
     * keyed to the seed passphrase; an older backup's files are keyed to the PIN of the
     * phone that made it, which the caller has prompted for. Open with whichever works,
     * re-encrypt to a fresh device secret, wrap it in the Keystore, and set the PIN gate.
     * The full snapshot (tx history, sync state) is preserved -- no re-scan.
     */
    private fun bindRestoredWallet(passPhrase: String, pin: String): Boolean {
        val context = AnonConfig.context!!
        val walletFile = AnonConfig.getDefaultWalletFile(context)
        val walletName = walletFile.name
        val prefs = provideWalletSharedPrefs(context)
        val manager = WalletManager.instance ?: return false
        var opened = manager.openWallet(walletFile.path, passPhrase)
        if (!opened.status.isOk) {
            runCatching { manager.close(opened) }
            opened = manager.openWallet(walletFile.path, pin)
        }
        if (!opened.status.isOk) {
            Timber.tag(TAG).e(
                "restore: neither passphrase nor PIN opened the restored wallet: %s",
                opened.status.errorString
            )
            runCatching { manager.close(opened) }
            return false
        }
        return try {
            val w = WalletKey.newSecret()
            if (!opened.setPassword(w)) {
                throw Exception("setPassword failed: ${opened.status?.errorString}")
            }
            opened.store()
            WalletKey.store(context, walletName, w)
            prefs.edit(commit = true) { putString(PREFS_PIN_HASH, PinHash.hash(pin)) }
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "restore: failed to bind wallet to device")
            false
        } finally {
            runCatching { manager.close(opened) }
        }
    }

    fun extractBackUp(backupPath: String, passPhrase: String): BackupPayload? {
        val destFile = File(AnonConfig.context?.cacheDir, "backup.anon").apply { createNewFile() }
        val decryptedDestFile =
            File(AnonConfig.context?.cacheDir, "backup.zip").apply { createNewFile() }
        val extractDestination = File(AnonConfig.context?.cacheDir, "tmp_extract")
        val inPutStream = AnonConfig.context?.contentResolver?.openInputStream(backupPath.toUri())
        inPutStream?.copyTo(destFile.outputStream())
        inPutStream?.close()
        val backup: BackupPayload
        try {
            EncryptUtil.decryptFile(passPhrase, destFile, decryptedDestFile)
            unZip(decryptedDestFile, extractDestination)

            val anonJson = File(extractDestination, "anon.json")
            val json = anonJson.readText()
            backup = Json.decodeFromString<BackupPayload>(json)
            if(backup.backup.meta.network != AnonConfig.getNetworkType().toStringForBackUp()){
                 throw NetworkMismatchException()
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
            throw e;
        } finally {
            // the decrypted zip holds the seed in the clear; the extracted copy that the
            // restore needs is removed when the preview screen goes away
            destFile.delete()
            decryptedDestFile.delete()
        }
        return backup;
    }

    suspend fun restoreBackUp(backupPayload: BackupPayload, passPhrase: String, pin: String): Boolean {
        try {

            val extractDestination = File(AnonConfig.context?.cacheDir, "tmp_extract")
            if (!extractDestination.exists() && !testBackUp(extractDestination)) {
                return false
            }

            val anonDir = AnonConfig.getDefaultWalletDir(AnonConfig.context!!)

            val v1Node = backupPayload.backup.node
            walletPrefs.edit(commit = true) {
                putString(NodeFields.RPC_HOST.value, v1Node.host)
                putInt(NodeFields.RPC_PORT.value, v1Node.rpcPort)
                putString(NodeFields.RPC_USERNAME.value, v1Node.username)
                putString(NodeFields.RPC_PASSWORD.value, v1Node.password)
                putString(NodeFields.RPC_NETWORK.value, v1Node.networkType)
            }

            val wallet = backupPayload.backup.wallet
            val nodes = backupPayload.backup.nodes;

            nodes.forEach {
                nodeRepository.addItem(it)
            }
            if (nodes.isEmpty() && v1Node.host.isNotEmpty()) {
                val node = Node().apply {
                    host = v1Node.host
                    rpcPort = v1Node.rpcPort
                    username = v1Node.username
                    password = v1Node.password
                    networkType = AnonConfig.getNetworkType()
                }
                nodes.forEach {
                    nodeRepository.addItem(node)
                }
            }
            walletPrefs.edit(commit = true) {
                putString(
                    PREFS_PASSPHRASE_HASH,
                    KeyStoreHelper.getCrazyPass(AnonConfig.context, passPhrase)
                )
                putLong(RESTORE_HEIGHT, wallet.restoreHeight ?: 0L)
            }
            extractDestination.listFiles()?.forEach { entry ->
                if (!entry.isDirectory && !entry.name.contains("anon.json")) {
                    entry.copyTo(File(anonDir, entry.name), true)
                }
            }
            if (bindRestoredWallet(passPhrase, pin)) return true
            // Copied but unbound files open nowhere. Left in place, the next launch finds
            // a wallet it cannot unlock; removed, the user is back in onboarding to retry.
            anonDir.deleteRecursively()
            return false
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
            return false
        }
    }

    fun cleanCacheDir() {
        AnonConfig.context?.cacheDir?.deleteRecursively()
    }
}