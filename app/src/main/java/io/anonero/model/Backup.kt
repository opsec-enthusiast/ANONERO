package io.anonero.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val backup: Backup,
    val version: String
)

@Serializable
data class NodeBackup(
    val host: String,
    val isOnion: Boolean,
    val networkType: String,
    val password: String,
    val rpcPort: Int,
    val username: String
)
@Serializable
data class Backup(
    val meta: BackupMeta,
    val node: NodeBackup,
    val wallet: WalletBackup
)


@Serializable
data class BackupMeta(
    val network: String,
    val timestamp: Long
)

@Serializable
data class WalletBackup(
    val address: String,
    val balanceAll: Long,
    val isSynchronized: Boolean,
    val isWatchOnly: Boolean,
    val numAccounts: Int,
    val numSubaddresses: Int,
    val restoreHeight: Long?,
    val seed: String?,
    val neroPayload: NeroKeyPayload? = null
)