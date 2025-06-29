package io.anonero.ui.onboard

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import io.anonero.AnonConfig
import io.anonero.model.NeroKeyPayload
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
    private var neroKeyPayload: NeroKeyPayload? = null

    fun setMode(mode: Mode) = run { this.mode = mode }

    fun getMode() = mode

    fun getSeed() = walllet?.getSeed(passPhrase) ?: ""


    suspend fun create(pin: String) {
        if (AnonConfig.context == null) return
        withContext(Dispatchers.IO) {
            val context = AnonConfig.context!!.applicationContext
            val walletFile = AnonConfig.getDefaultWalletFile(context)
            AnonConfig.getDefaultWalletDir(context).deleteRecursively()
            AnonConfig.getDefaultWalletDir(context).mkdirs()
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
            prefs.edit().putString("passPhraseHash", crazyPass)
                .apply()
            walllet = anonWallet
            delay(500)
        }
    }


    suspend fun createViewOnly(pin: String) {
        if (neroKeyPayload == null) return
        if (AnonConfig.context == null) return
        withContext(Dispatchers.IO) {
            val context = AnonConfig.context!!.applicationContext
            val walletFile = AnonConfig.getDefaultWalletFile(context)
            AnonConfig.getDefaultWalletDir(context).deleteRecursively()
            AnonConfig.getDefaultWalletDir(context).mkdirs()
            val anonWallet = WalletManager.instance?.createWalletWithKeys(
                walletFile,
                pin,
                language = "English",
                restoreHeight = neroKeyPayload!!.restoreHeight,
                viewKeyString = neroKeyPayload!!.privateViewKey,
                addressString = neroKeyPayload!!.primaryAddress,
                spendKeyString = ""
            )
            anonWallet?.store()
            delay(100)
            if (anonWallet?.status?.isOk != true) {
                walletFile.delete()
                throw CancellationException("unable to create wallet")
            }
            val crazyPass: String = KeyStoreHelper.getCrazyPass(AnonConfig.context, passPhrase)
            prefs.edit().putString("passPhraseHash", crazyPass)
                .apply()
            walllet = anonWallet
            delay(500)
        }
    }

    fun setPassPhrase(it: String) {
        this.passPhrase = it
    }

    fun setNeroPayload(it: NeroKeyPayload) {
        neroKeyPayload = it
    }

}