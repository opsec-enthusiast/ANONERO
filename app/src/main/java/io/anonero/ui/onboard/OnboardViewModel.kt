package io.anonero.ui.onboard

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import io.anonero.AnonConfig
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.util.KeyStoreHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


enum class Mode {
    RESTORE,
    CREATE
}

class OnboardViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private var mode = Mode.CREATE
    private var passPhrase = ""
    private var walllet: Wallet? = null

    fun setMode(mode: Mode) = run { this.mode = mode }

    fun getMode() = mode

    fun getSeed() = walllet?.getSeed(passPhrase) ?: ""


    suspend fun create(pin: String) {
        if (AnonConfig.context == null) return
        withContext(Dispatchers.IO) {
            Log.i("TAG", "create: $passPhrase")
            val context = AnonConfig.context!!.applicationContext
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
            delay(100)
            if (anonWallet?.status?.isOk != true) {
                walletFile.delete()
                throw CancellationException("unable to create wallet")
            }
            val crazyPass: String = KeyStoreHelper.getCrazyPass(AnonConfig.context, passPhrase)
            prefs.edit().putString("passPhraseHash",crazyPass)
                .apply()
            walllet = anonWallet
            delay(500)
        }
    }

    fun setPassPhrase(it: String) {
        this.passPhrase = it
    }

}