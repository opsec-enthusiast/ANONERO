package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.anonero.ui.home.addresses.SubAddressesScreen
import io.anonero.ui.home.graph.HomeScreenRoute
import io.anonero.ui.home.graph.ProxySettingsRoute
import io.anonero.ui.home.graph.ReceiveRoute
import io.anonero.ui.home.graph.ReviewTransactionRoute
import io.anonero.ui.home.graph.SecureWipeRoute
import io.anonero.ui.home.graph.SendRoute
import io.anonero.ui.home.graph.SettingsExportBackUp
import io.anonero.ui.home.graph.SettingsLogs
import io.anonero.ui.home.graph.SettingsNodeRoute
import io.anonero.ui.home.graph.SettingsRoute
import io.anonero.ui.home.graph.SettingsViewSeedRoute
import io.anonero.ui.home.graph.SubAddressesRoute
import io.anonero.ui.home.graph.TransactionsRoute
import io.anonero.ui.home.settings.ExportBackUp
import io.anonero.ui.home.settings.LogViewer
import io.anonero.ui.home.settings.NodeSettings
import io.anonero.ui.home.settings.ProxySettings
import io.anonero.ui.home.settings.SecureWipe
import io.anonero.ui.home.settings.SeedSettingsPage
import io.anonero.ui.home.settings.SettingsPage
import io.anonero.ui.home.spend.ReviewTransactionScreen
import io.anonero.ui.onboard.graph.LandingScreenRoute

@Composable
fun HomeScreenComposable(modifier: Modifier = Modifier, mainNavController: NavHostController?) {

    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        ),
        selectedTextColor = MaterialTheme.colorScheme.primary,
    )
    var showBottomNavigation by remember { mutableStateOf(true) }
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TransactionsRoute

    Scaffold(contentWindowInsets = WindowInsets(
        top = 0.dp, bottom = 0.dp
    ), bottomBar = {
        AnimatedVisibility(visible = showBottomNavigation,
            enter = slideInVertically(
                initialOffsetY = { it }, // Slide in from the bottom
                animationSpec = tween(durationMillis = 500)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }, // Slide out to the bottom
                animationSpec = tween(durationMillis = 500)
            )
            ) {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                getRoutes().forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.getRouteAsString,
                        colors = navigationItemColors,
                        onClick = {
                            if (currentRoute != item.getRouteAsString) {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                    )
                }
            }
        }

    }) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            NavHost(bottomNavController, startDestination = TransactionsRoute, exitTransition = {
                fadeOut(animationSpec = tween(340))
            }, enterTransition = {
                fadeIn(animationSpec = tween(340))
            }) {
                composable<TransactionsRoute> {
                    TransactionScreen()
                }
                composable<ReceiveRoute> {
                    ReceiveScreen(onBackPress = {
                        bottomNavController.popBackStack()
                    }, navigateToSubAddresses = {
                        showBottomNavigation = false
                        bottomNavController.navigate(SubAddressesRoute)
                    })
                }
                composable<SendRoute> {
                    SendScreen(navigateToReview = {
                        showBottomNavigation = false
                        bottomNavController.navigate(it)
                    }, onBackPress = {
                        bottomNavController.popBackStack()
                    })
                }
                composable<ReviewTransactionRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<ReviewTransactionRoute>()
                    ReviewTransactionScreen(route, onFinished = {
                        bottomNavController.navigate(HomeScreenRoute) {
                            popUpTo(TransactionsRoute) {
                                inclusive = true
                            }
                        }
                    }, onBackPressed = {
                        showBottomNavigation = true
                        bottomNavController.popBackStack()
                    })
                }
                composable<SettingsRoute> {
                    SettingsPage(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }, navigateTo = {
                            bottomNavController.navigate(it)
                        }
                    )
                }
                composable<SettingsViewSeedRoute> {
                    SeedSettingsPage(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable<SettingsExportBackUp> {
                    ExportBackUp(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable<SettingsLogs> {
                    LogViewer(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable<ProxySettingsRoute> {
                    ProxySettings(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable<SecureWipeRoute> {
                    SecureWipe(
                        onBackPress = {
                            showBottomNavigation = true
                            bottomNavController.popBackStack()
                        },
                        requestClearScreen = {
                            showBottomNavigation = it
                        },
                        goToHome = {
                            mainNavController?.navigate(LandingScreenRoute) {
                                popUpTo(LandingScreenRoute) {
                                    inclusive = false
                                }
                            }
                        }
                    )
                }
                composable<SettingsNodeRoute> {
                    NodeSettings(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
                composable<SubAddressesRoute> {
                    SubAddressesScreen(onBackPress = {
                        showBottomNavigation=true
                        bottomNavController.popBackStack()
                    })
                }
            }
        }
    }
}


@Preview(device = "id:pixel_8")
@Composable
private fun HomeScreenComposablePrev() {
    AnonNeroTheme {
        HomeScreenComposable(mainNavController = null)
    }
}