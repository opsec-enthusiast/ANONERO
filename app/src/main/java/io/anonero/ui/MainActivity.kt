package io.anonero.ui

import AnonNeroTheme
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.asLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.anonero.AnonConfig
import io.anonero.R
import io.anonero.model.WalletManager
import io.anonero.services.AnonNeroService
import io.anonero.services.TorService
import io.anonero.services.WalletState
import io.anonero.services.startAnonService
import io.anonero.ui.home.graph.homeGraph
import io.anonero.ui.home.graph.routes.Home
import io.anonero.ui.onboard.OnboardViewModel
import io.anonero.ui.onboard.graph.LandingScreenRoute
import io.anonero.ui.onboard.graph.onboardingGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber


private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val walletState: WalletState by inject()
    private val torService: TorService by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashscreen = installSplashScreen()
        var isAppReady by mutableStateOf(false)
        super.onCreate(savedInstanceState)

        splashscreen.setKeepOnScreenCondition { false }

        if (intent.hasExtra("notification")) {
            walletState.setBackGroundSync(false)
            walletState.update()
        }

        val scrimColor = Color.Black.copy(alpha = 0.1f).toArgb()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrimColor),
            navigationBarStyle = SystemBarStyle.dark(scrimColor),
        )
        setContent {
            var walletExist by remember { mutableStateOf(AnonConfig.isWalletFileExist()) }
            val scope = rememberCoroutineScope()
            val onboardViewModel = koinViewModel<OnboardViewModel>()
            val socks by torService.socksFlow.asLiveData().observeAsState(null)

            val progressAnim by animateFloatAsState(
                targetValue = if (socks != null) .98f else 0.4f,
                label = "progressAnim"
            )

            LaunchedEffect(key1 = walletExist, socks) {
                if (socks != null) {
                    scope.launch(Dispatchers.IO) {
                        walletExist = AnonConfig.getDefaultWalletFile(applicationContext).exists()
                        delay(200)
                        isAppReady = true
                    }
                }
            }
            if (!isAppReady) {
                return@setContent Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.ic_anon),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(120.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { progressAnim },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .padding(
                                    bottom = 24.dp
                                ),
                            strokeCap = StrokeCap.Round,
                            color = Color.White,
                            drawStopIndicator = {},
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "Waiting for Tor ...",
                            color = Color.White
                        )
                    }
                }

            } else {
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

    override fun onDestroy() {
        scope.launch(Dispatchers.IO) {
            WalletManager.instance?.wallet?.let {
                it.store()
                it.close()
            }
        }.invokeOnCompletion {
            if (it != null) {
                Timber.tag(TAG).e(it)
            }
        }
        scope.cancel()
        super.onDestroy()
        //fix app retain state after close
        Process.killProcess(Process.myPid())
    }
}

