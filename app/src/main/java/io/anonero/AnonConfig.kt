package io.anonero

import android.content.Context
import io.anonero.model.NetworkType
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.pow

val jsonDecoder = Json { ignoreUnknownKeys = true }

object AnonConfig {

    const val XMR_DECIMALS = 12
    val ONE_XMR = Math.round(10.0.pow(XMR_DECIMALS.toDouble()))
    const val MAX_LOG_SIZE = 250
    const val PREFS = "anonPref"
    var context: AnonApplication? = null

    fun getNetworkType(): NetworkType {
        if (BuildConfig.APPLICATION_ID.lowercase().contains("stagenet")) {
            return NetworkType.NetworkType_Stagenet
        }
        return NetworkType.NetworkType_Mainnet
    }

    fun getDefaultWalletFile(context: Context): File {
        val walletDir = getDefaultWalletDir(context)
        val anonWallet = File(walletDir, "anon")
        return anonWallet
    }


    fun getLogFile(context: Context): File {
        val logDir = File(context.applicationContext.cacheDir, "logs")
        if (!logDir.exists()) {
            logDir.mkdirs() // Create the directory if it doesn't exist
        }
        val logFile = File(logDir, "anon_log")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        return logFile
    }

    fun getDefaultWalletDir(context: Context): File {
        val walletDir = File(context.applicationContext.filesDir, "wallets")
        if (!walletDir.exists()) {
            walletDir.mkdirs()
        }
        return walletDir
    }
}