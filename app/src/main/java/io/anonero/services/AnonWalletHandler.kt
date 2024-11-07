package io.anonero.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.anonero.AnonConfig
import io.anonero.model.Node
import io.anonero.model.WalletManager
import io.anonero.ui.PREFS
import org.json.JSONObject


class InvalidPin : Exception("invalid pin")

class AnonWalletHandler(
    private val sharedPrefs: SharedPreferences,
    private val walletRepo: WalletRepo
) {

    private var handler: MoneroHandlerThread? = null

    fun openWallet(pin: String): Boolean {
        val walletFile = AnonConfig.getDefaultWalletFile(AnonConfig.context!!)
        val anonWallet = WalletManager.instance?.openWallet(
            walletFile.path,
            pin,
        )
        walletRepo.setLoading(true)
        return anonWallet?.status?.isOk ?: throw InvalidPin()
    }

    fun startService() {
        val wallet = WalletManager.instance?.wallet ?: return
        handler = MoneroHandlerThread(
            wallet,
            walletRepo
        )
        wallet.setListener(handler)
        wallet.refreshHistory()
        handler?.start()
        walletRepo.update()
        val prefs = AnonConfig.context!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try {
            val host = prefs.getString("host", "singapore.node.xmr.pm")
            val rpcPort = prefs.getString("rpcPort", "38081")
            val rpcUsername = prefs.getString("rpcUsername", "")
            val rpcPassphrase = prefs.getString("rpcPassphrase", "")
            val node = Node.fromJson(
                JSONObject()
                    .apply {
                        put("rpcHost", host)
                        put("rpcPort", rpcPort)
                        put("rpcUsername", rpcUsername)
                        put("rpcPassphrase", rpcPassphrase)
                        put("network", AnonConfig.getNetworkType().toString())
                        put("name", "anon")
                    }
            )
            WalletManager.instance?.setDaemon(node)
            wallet.init(0)
            walletRepo.update()
            wallet.setTrustedDaemon(true)
            wallet.startRefresh()
            wallet.refreshHistory()
        } catch (_: Exception) {
        }

    }
}