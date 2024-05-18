package io.anonero

import android.content.Context
import io.anonero.model.NetworkType
import java.io.File

object AnonConfig {

    fun getNetworkType(): NetworkType {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE == "stageNet") {
            return NetworkType.NetworkType_Stagenet
        }
        return NetworkType.NetworkType_Mainnet
    }

    fun getDefaultWalletFile(context: Context): File {
        val walletDir =  getDefaultWalletDir(context)
        val anonWallet = File(walletDir, "anon")
        return anonWallet
    }

    fun getDefaultWalletDir(context: Context): File {
        val walletDir = File(context.applicationContext.filesDir, "wallets")
        if (!walletDir.exists()) {
            walletDir.mkdirs()
        }
        return walletDir
    }
}