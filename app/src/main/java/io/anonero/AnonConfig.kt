package io.anonero

import android.content.Context
import io.anonero.model.NetworkType
import io.matthewnelson.kmp.tor.resource.exec.tor.ResourceLoaderTorExec
import io.matthewnelson.kmp.tor.runtime.TorRuntime
import io.matthewnelson.kmp.tor.runtime.core.ThisBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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


    fun getTorConfig(scope: CoroutineScope): TorRuntime.Environment {
        val torDir = File(context?.filesDir, "tor")
        torDir.mkdirs()
        val cacheDir = File(context?.cacheDir, "tor")
        torDir.mkdirs()
        return TorRuntime.Environment.Builder(
            workDirectory = torDir,
            cacheDirectory = cacheDir,
            loader = ResourceLoaderTorExec::getOrCreate,
        )
    }


    fun getTorCacheDir(): File {
        val torDir = File(context?.cacheDir, "tor")
        torDir.mkdirs()
        return torDir
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