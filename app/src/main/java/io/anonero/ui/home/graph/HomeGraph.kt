package io.anonero.ui.home.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.anonero.ui.home.HomeScreenComposable
import kotlinx.serialization.Serializable


@Serializable
data object HomeScreen

@Serializable
data object Home

fun NavGraphBuilder.homeGraph(navController: NavHostController) {

    navigation<Home>(
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreenComposable()
        }
    }
}