package io.anonero.model

data class CoinsInfo(
    val unlockTime: Long,
    val spent: Boolean,
    val key: String,
    val amount: Long,
    val hash: String,
    val pub_key: String,
    val frozen: Boolean,
    val creationTime: Long,
)