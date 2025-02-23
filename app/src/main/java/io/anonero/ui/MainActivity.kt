package io.anonero.ui

import AnonNeroTheme
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.anonero.model.WalletManager
import io.anonero.services.AnonNeroService
import io.anonero.services.WalletState
import io.anonero.services.startAnonService
import io.anonero.ui.home.graph.homeGraph
import io.anonero.ui.home.graph.routes.Home
import io.anonero.ui.onboard.OnboardViewModel
import io.anonero.ui.onboard.graph.LandingScreenRoute
import io.anonero.ui.onboard.graph.onboardingGraph
import io.anonero.ui.viewmodels.AppViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by inject()

    private val walletState: WalletState by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val content: View = this.findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener {
            appViewModel.isReady
        }
        if (intent.hasExtra("notification")) {
            stopNotificationService()
            WalletManager.instance?.wallet?.close()
            walletState.setBackGroundSync(false)
            walletState.update()
        }
        setContent {
            val walletExist by appViewModel.existWallet.asLiveData().observeAsState(false)
            val onboardViewModel = koinViewModel<OnboardViewModel>()

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
                    startDestination = if (walletExist) Home else LandingScreenRoute
                ) {
                    //landing screen and onboarding screens
                    onboardingGraph(navController, onboardViewModel)
                    //home bottom navigation
                    homeGraph(navController)
                }
            }
        }


    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Intent(applicationContext, AnonNeroService::class.java)
                .also {
                    it.action = "start"
                    ContextCompat.startForegroundService(applicationContext, it)
                }
        }
        //handle and show dialog
    }

    fun startNotificationService() {
        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startAnonService(applicationContext)
        }
    }

    fun stopNotificationService() {
        Intent(applicationContext, AnonNeroService::class.java)
            .also {
                it.action = "stop"
                ContextCompat.startForegroundService(applicationContext, it)
            }
    }

}

