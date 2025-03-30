package io.anonero.util.backup

import android.content.Context
import android.icu.text.SimpleDateFormat
import io.anonero.AnonConfig
import io.anonero.R
import io.anonero.di.provideWalletSharedPrefs
import io.anonero.model.NeroKeyPayload
import io.anonero.model.WalletManager
import io.anonero.model.node.Node
import io.anonero.model.node.NodeFields
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
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

object BackUpHelper {
    private const val BACKUP_VERSION = "1.0"
    const val BUFFER = 2048
    private const val TAG = "BackUpHelper"

    fun createBackUp(seedPassphrase: String, context: Context): String {
        val wallet = WalletManager.instance?.wallet ?: throw Exception("Wallet not found")
        val prefs = provideWalletSharedPrefs(context)

        val walletPayload = JSONObject()
            .apply {
                put("address", wallet.address)
                put("seed", wallet.getSeed(seedPassphrase))
                put("restoreHeight", wallet.getRestoreHeight())
                put("balanceAll", wallet.getBalanceAll())
                put("numSubaddresses", wallet.numSubAddresses)
                put("numAccounts", wallet.getNumAccounts())
                put("isWatchOnly", wallet.isWatchOnly())
                put("isSynchronized", wallet.isSynchronized)
                if (AnonConfig.viewOnly) {
                    put("nero", NeroKeyPayload.fromWallet(wallet).toJSONObject())
                }
            }
        val nodePayload = JSONObject()
            .apply {
                put("host", prefs.getString(NodeFields.RPC_HOST.value, ""))
                put("password", prefs.getString(NodeFields.RPC_PASSWORD.value, ""))
                put("username", prefs.getString(NodeFields.RPC_USERNAME.value, ""))
                put(
                    "rpcPort",
                    prefs.getInt(NodeFields.RPC_PORT.value, Node.defaultRpcPort)
                )
                put("networkType", AnonConfig.getNetworkType().toString())
                put("isOnion", false)
            }

        val metaPayload = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("network", AnonConfig.getNetworkType().toStringForBackUp())
        }


        val backUpPayload = JSONObject().apply {
            put("node", nodePayload)
            put("wallet", walletPayload)
            put("meta", metaPayload)
        }

        Timber.tag(TAG).d("BackUpPayload: $backUpPayload")

        val json = JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("backup", backUpPayload)
        }.toString()

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
        val tmpBackupFile = File(tmpBackupDir, "anon.json")
        tmpBackupFile.writeText(json)
        val walletDir = File(context.filesDir, "wallets")
        walletDir.copyRecursively(tmpBackupDir, true)
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
            tmpBackupDir.deleteRecursively()
            backupFile.delete()
        }
        return backupFileEncrypted.absolutePath
    }

    fun testBackUP(destinationDir: File): Boolean {
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

    fun cleanCacheDir() {
        AnonConfig.context?.cacheDir?.deleteRecursively()
    }
}