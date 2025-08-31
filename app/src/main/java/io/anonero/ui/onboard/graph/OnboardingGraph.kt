package io.anonero.ui.onboard.graph

import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import io.anonero.AnonConfig
import io.anonero.ui.home.graph.routes.Home
import io.anonero.ui.home.settings.LogViewer
import io.anonero.ui.home.settings.ProxySettings
import io.anonero.ui.onboard.Mode
import io.anonero.ui.onboard.OnboardLoadingComposable
import io.anonero.ui.onboard.OnboardViewModel
import io.anonero.ui.onboard.OnboardingWelcome
import io.anonero.ui.onboard.PinSetup
import io.anonero.ui.onboard.RestoreWallet
import io.anonero.ui.onboard.SeedSetup
import io.anonero.ui.onboard.SetupNodeComposable
import io.anonero.ui.onboard.SetupPassphrase
import io.anonero.ui.onboard.restore.RestorePreview
import io.anonero.ui.onboard.viewonly.RestoreFromKeys
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object LandingScreenRoute

@Serializable
data object OnboardingWelcomeScreen

@Serializable
data object OnboardingProxyScreen

@Serializable
data object OnboardPassPhraseScreen

@Serializable
data object OnboardLogsScreen

@Serializable
data class OnboardLoading(val message: String)

@Serializable
data class OnboardSeedScreen(val seed: String)

@Serializable
data object OnboardPinScreen

@Serializable
data object OnboardNodeSetupScreen

@Serializable
data object OnboardRestoreScreen

@Serializable
data class OnboardRestorePreviewScreen(val backUpPath: String)

@Serializable
data object OnboardImportKeysScreen

//onboarding
fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    onboardViewModel: OnboardViewModel
) {

    navigation<LandingScreenRoute>(
        startDestination = OnboardingWelcomeScreen,
    ) {
        dialog<OnboardLoading>(
            dialogProperties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            OnboardLoadingComposable(loadingState = it.toRoute())
        }
        composable<OnboardingWelcomeScreen> {
            OnboardingWelcome(
                onRestoreClick = {
                    onboardViewModel.setMode(Mode.RESTORE)
                    if (it != null) {
                        navController.navigate(OnboardRestorePreviewScreen(it))
                    } else {
                        navController.navigate(OnboardNodeSetupScreen)
                    }
                },
                onCreateClick = {
                    onboardViewModel.setMode(Mode.CREATE)
                    navController.navigate(OnboardNodeSetupScreen)
                },
                onRestoreFromKeys = {
                    navController.navigate(OnboardImportKeysScreen)
                },
                onProxySettings = {
                    navController.navigate(OnboardingProxyScreen)
                },
                onLogsScreen = {
                    navController.navigate(OnboardLogsScreen)
                }
            )
        }
        composable<OnboardingProxyScreen> {
            ProxySettings(
                onBackPress = {
                    navController.navigateUp()
                },
            )
        }
        composable<OnboardNodeSetupScreen> {
            SetupNodeComposable(
                onBackPressed = {
                    navController.navigateUp()
                },
                oNextPressed = {
                    if (AnonConfig.viewOnly) {
                        navController.navigate(OnboardPinScreen)
                    } else {
                        if (onboardViewModel.getMode() == Mode.RESTORE) {
                            navController.navigate(OnboardRestoreScreen)
                        } else {
                            navController.navigate(OnboardPassPhraseScreen)
                        }
                    }
                }
            )
        }
        composable<OnboardRestoreScreen> {
            RestoreWallet(
                onBackPressed = {
                    navController.navigateUp()
                },
                oNextPressed = {
                    onboardViewModel.setRestorePayload(it)
                    navController
                        .navigate(OnboardPassPhraseScreen)
                }
            )
        }
        composable<OnboardRestorePreviewScreen> { navBackStackEntry ->
            val backUpPath = navBackStackEntry.toRoute<OnboardRestorePreviewScreen>()
            RestorePreview(
                backUpPath = backUpPath.backUpPath,
                onBackPressed = {
                    navController.navigateUp()
                },
                navigateTo = {
                    navController.navigate(it)
                },
                oNextPressed = {
                    navController.navigate(
                        Home
                    ) {
                        popUpTo(LandingScreenRoute)
                    }
                }
            )
        }
        composable<OnboardPassPhraseScreen> {
            SetupPassphrase(
                onBackPressed = {
                    navController.navigateUp()
                },
                oNextPressed = {
                    onboardViewModel.setPassPhrase(it)
                    navController.navigate(OnboardPinScreen)
                }
            )
        }


        composable<OnboardLogsScreen>() {
            LogViewer(
                onBackPress = {
                    navController.navigateUp()
                }
            )
        }

        composable<OnboardPinScreen> {
            PinSetup(
                onNext = { pin ->
                    var loadingMessage = "Creating wallet..."
                    if (onboardViewModel.getMode() == Mode.RESTORE) {
                        loadingMessage = "Restoring wallet..."
                    }
                    navController.navigate(OnboardLoading(loadingMessage))
                    onboardViewModel.viewModelScope.launch {
                        if (AnonConfig.viewOnly) {
                            onboardViewModel.createViewOnly(pin)
                        } else {
                            if (onboardViewModel.getMode() == Mode.RESTORE) {
                                onboardViewModel.restoreFromSeed(pin)
                            } else {
                                onboardViewModel.create(pin)
                            }
                        }
                        delay(600)
                    }.invokeOnCompletion {
                        if (it == null) {
                            if (AnonConfig.viewOnly) {
                                navController.navigate(
                                    Home
                                ) {
                                    popUpTo(LandingScreenRoute)
                                }
                            } else {
                                if (onboardViewModel.getMode() == Mode.RESTORE) {
                                    navController.navigate(
                                        Home
                                    ) {
                                        popUpTo(LandingScreenRoute)
                                    }
                                } else {
                                    navController.navigate(OnboardSeedScreen(onboardViewModel.getSeed()))
                                }
                            }
                        }
                    }
                }
            )
        }
        composable<OnboardSeedScreen> { navBackStackEntry ->
            val state = navBackStackEntry.toRoute<OnboardSeedScreen>()
            SeedSetup(
                seed = state.seed
                    .split(" ").toList(),
                oNextPressed = {
                    navController.navigate(
                        Home
                    ) {
                        popUpTo(LandingScreenRoute)
                    }
                },
                onBackPressed = {
                    navController.navigate("")
                }
            )
        }
        composable<OnboardImportKeysScreen> {
            RestoreFromKeys(
                oNextPressed = {
                    onboardViewModel.setNeroPayload(it)
                    navController.navigate(OnboardNodeSetupScreen)
                },
                onBackPressed = {
                    navController.navigateUp()
                }
            )
        }
    }
}