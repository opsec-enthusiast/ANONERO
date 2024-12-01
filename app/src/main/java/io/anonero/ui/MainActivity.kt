package io.anonero.ui

import AnonNeroTheme
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.anonero.AnonConfig
import io.anonero.ui.home.graph.Home
import io.anonero.ui.home.graph.LockScreenRoute
import io.anonero.ui.home.graph.homeGraph
import io.anonero.ui.onboard.graph.LandingScreen
import io.anonero.ui.onboard.graph.onboardingGraph
import io.anonero.ui.viewmodels.AppViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val content: View = this.findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener {
             appViewModel.isReady
        }
        setContent {
            val walletExist by appViewModel.existWallet.asLiveData().observeAsState(false)
            AnonNeroTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    popExitTransition = {
                        ExitTransition.None
                    },
                    popEnterTransition = {
                        EnterTransition.None
                    },
                    startDestination = if (walletExist) Home else LandingScreen
                ) {
                    //landing screen and onboarding screens
                    onboardingGraph(navController)
                    //home bottom navigation
                    homeGraph(navController)
                }
            }
        }

        // Set up an OnPreDrawListener to the root view.
    }

}

