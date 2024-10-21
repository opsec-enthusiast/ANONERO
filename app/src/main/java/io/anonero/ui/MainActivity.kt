package io.anonero.ui

import AnonNeroTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.anonero.AnonConfig
import io.anonero.ui.home.graph.Home
import io.anonero.ui.home.graph.homeGraph
import io.anonero.ui.onboard.graph.LandingScreen
import io.anonero.ui.onboard.graph.onboardingGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnonNeroTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                LaunchedEffect(true) {
                    val walletFile = AnonConfig.getDefaultWalletFile(AnonConfig.context!!)
                    scope.launch(Dispatchers.IO) {
                        if (walletFile.exists()) {
                            withContext(Dispatchers.Main) {
                                navController.navigate(Home)
                            }
                        }
                    }
                }
                NavHost(
                    navController = navController,
                    popExitTransition = {
                        ExitTransition.None
                    },
                    popEnterTransition = {
                        EnterTransition.None
                    },
                    startDestination = LandingScreen
                ) {
                    //landing screen and onboarding screens
                    onboardingGraph(navController)
                    homeGraph(navController)
                }
            }
        }
    }

}

