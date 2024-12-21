package io.anonero.model

data class DaemonInfo(val daemon: String?,
                      val connectionStatus: Wallet.ConnectionStatus,
                      val daemonHeight: Long,)
