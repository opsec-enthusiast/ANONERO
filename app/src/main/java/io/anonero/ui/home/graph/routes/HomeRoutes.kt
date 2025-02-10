package io.anonero.ui.home.graph.routes

import kotlinx.serialization.Serializable


@Serializable
data object HomeScreenRoute

@Serializable
data object LockScreenRoute

@Serializable
data object Home

@Serializable
data object TransactionsRoute

@Serializable
data class TransactionDetailRoute(val transactionId: String)

@Serializable
data object ReceiveRoute

@Serializable
data class ReviewTransactionRoute(val toAddress: String)

@Serializable
data object SettingsRoute

@Serializable
data object SettingsViewSeedRoute

@Serializable
data object SettingsExportBackUp

@Serializable
data object SettingsLogs

@Serializable
data object SettingsNodeRoute

@Serializable
data object ProxySettingsRoute

@Serializable
data object SecureWipeRoute

@Serializable
data object SubAddressesRoute
