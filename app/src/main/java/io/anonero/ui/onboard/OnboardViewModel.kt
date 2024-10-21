package io.anonero.ui.onboard

import androidx.lifecycle.ViewModel
import io.anonero.AnonConfig
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext


enum class Mode {
    RESTORE,
    CREATE
}

class OnboardViewModel : ViewModel() {

    private var mode = Mode.CREATE
    private var passPhrase = ""
    private var walllet: Wallet? = null

    fun setMode(mode: Mode) = run { this.mode = mode }

    fun getMode() = mode

    fun getSeed() = walllet?.getSeed(passPhrase) ?: ""


    suspend fun create(pin: String) {
        if (AnonConfig.context == null) return
        withContext(Dispatchers.IO) {
            val context = AnonConfig.context!!.applicationContext;
            context.filesDir.deleteRecursively()
            val walletFile = AnonConfig.getDefaultWalletFile(context)
            val anonWallet = WalletManager.instance?.createWallet(
                walletFile,
                pin,
                passPhrase,
                "English",
                1,
            )
            anonWallet?.store()
            walllet = anonWallet
        }
    }

    fun setPassPhrase(it: String) {
        this.passPhrase = it
    }

}