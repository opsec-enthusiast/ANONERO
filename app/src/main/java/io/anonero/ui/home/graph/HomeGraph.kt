package io.anonero.ui.home.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.anonero.model.TransactionInfo
import io.anonero.model.WalletManager
import io.anonero.ui.home.HomeScreenComposable
import io.anonero.ui.home.LockScreen
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
data object SendRoute

@Serializable
data class ReviewTransactionRoute(val toAddress:String)

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

@Serializable
data object SpendRoute


fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    val walletStatus = WalletManager.instance?.wallet?.status?.isOk
    navigation<Home>(
        startDestination = if(walletStatus == true) HomeScreenRoute else LockScreenRoute
    ) {
        composable<HomeScreenRoute> {
            HomeScreenComposable(mainNavController=navController)
        }
        composable<LockScreenRoute> {
            LockScreen(
                onUnLocked = {
                    navController.navigate(HomeScreenRoute) {
                        popUpTo(LockScreenRoute) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}