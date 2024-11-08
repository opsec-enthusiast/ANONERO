package io.anonero.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.anonero.AnonConfig
import io.anonero.model.Node
import io.anonero.model.NodeFields
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
            val host = prefs.getString(NodeFields.RPC_HOST.value, "")
            val rpcPort = prefs.getString(NodeFields.RPC_PORT.value, "")
            val rpcUsername = prefs.getString(NodeFields.RPC_USERNAME.value, "")
            val rpcPassphrase = prefs.getString(NodeFields.RPC_PASSWORD.value ,"")
            val nodeObj = JSONObject()
                .apply {
                    put(NodeFields.RPC_HOST.value, host)
                    put(NodeFields.RPC_PORT.value, rpcPort)
                    put(NodeFields.RPC_USERNAME.value, rpcUsername)
                    put(NodeFields.RPC_PASSWORD.value,rpcPassphrase)
                    put(NodeFields.RPC_NETWORK.value, AnonConfig.getNetworkType().toString())
                    put(NodeFields.NODE_NAME.value, "anon")
                }
            val node = Node.fromJson(nodeObj)
            Log.i("TAG", "startService: Node:\n ${nodeObj.toString(2)}\n")
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