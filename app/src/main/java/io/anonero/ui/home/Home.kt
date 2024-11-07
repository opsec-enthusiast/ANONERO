package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.anonero.ui.home.addresses.SubAddressesScreen
import io.anonero.ui.home.graph.ReceiveRoute
import io.anonero.ui.home.graph.SendRoute
import io.anonero.ui.home.graph.SettingsRoute
import io.anonero.ui.home.graph.SubAddressesRoute
import io.anonero.ui.home.graph.TransactionsRoute

@Composable
fun HomeScreenComposable(modifier: Modifier = Modifier) {

    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        ),
        selectedTextColor = MaterialTheme.colorScheme.primary,
    )
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TransactionsRoute

    Scaffold(
        contentWindowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp
        ),
        bottomBar = {
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
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            NavHost(bottomNavController, startDestination = TransactionsRoute,
                exitTransition = {
                    fadeOut(animationSpec = tween(340))
                },
                enterTransition = {
                    fadeIn(animationSpec = tween(340))
                }
            ) {
                composable<TransactionsRoute> {
                    TransactionScreen()
                }
                composable<ReceiveRoute> {
                    ReceiveScreen(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        },
                        navigateToSubAddresses = {
                            bottomNavController.navigate(SubAddressesRoute)
                        }
                    )
                }
                composable<SendRoute> {
                    SendScreen()
                }
                composable<SettingsRoute> {
                    SettingsScreen()
                }
                composable<SubAddressesRoute> {
                    SubAddressesScreen(
                        onBackPress = {
                            bottomNavController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}


@Preview(device = "id:pixel_8")
@Composable
private fun HomeScreenComposablePrev() {
    AnonNeroTheme {
        HomeScreenComposable()
    }
}