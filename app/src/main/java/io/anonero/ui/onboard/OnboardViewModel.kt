package io.anonero.ui.onboard

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.anonero.AnonConfig
import io.anonero.model.NeroKeyPayload
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.util.KeyStoreHelper
import io.anonero.util.PREFS_PASSPHRASE_HASH
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.edit


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
            anonWallet?.setRestoreHeight(3460000);
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

    fun restoreFromSeed() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val context = AnonConfig.context!!.applicationContext
//            val walletFile = AnonConfig.getDefaultWalletFile(context)
//            AnonConfig.getDefaultWalletDir(context).deleteRecursively()
//            AnonConfig.getDefaultWalletDir(context).mkdirs()
//            delay(100)
//            val anonWallet = WalletManager.instance?.recoveryWalletPolyseed(
//                walletFile,
//                "00000",
//            )
//            delay(1000)
//
//            Log.i("TAG", "create: done ${anonWallet?.status?.isOk}")
//            Log.i("TAG", "create: errorString ? ${anonWallet?.status?.errorString} balance: ${ anonWallet?.viewOnlyBalance()}" )
//            anonWallet?.store()
//            anonWallet?.store(walletFile.path)
//            delay(100)
//            if (anonWallet?.status?.isOk != true) {
//                walletFile.delete()
//                throw CancellationException("unable to create wallet ${anonWallet?.status?.errorString}")
//            }
//
//            val crazyPass: String = KeyStoreHelper.getCrazyPass(AnonConfig.context, "gggg")
//            Log.i("TAG", "create: crazyPass $crazyPass")
//            prefs.edit(commit = true) {
//                putString(PREFS_PASSPHRASE_HASH, crazyPass)
//            }
//        }.invokeOnCompletion {
//            if (it == null) {
//                Log.i("TAG", "create: done")
//            }else{
//                Log.e("TAG", "create: error", it)
//            }
//        }
    }

}