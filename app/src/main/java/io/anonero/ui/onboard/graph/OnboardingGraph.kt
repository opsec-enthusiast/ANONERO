package io.anonero.ui.onboard.graph

import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import io.anonero.ui.home.graph.routes.Home
import io.anonero.ui.onboard.Mode
import io.anonero.ui.onboard.OnboardLoadingComposable
import io.anonero.ui.onboard.OnboardViewModel
import io.anonero.ui.onboard.OnboardingWelcome
import io.anonero.ui.onboard.PinSetup
import io.anonero.ui.onboard.SeedSetup
import io.anonero.ui.onboard.SetupNodeComposable
import io.anonero.ui.onboard.SetupPassphrase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


@Serializable
data object LandingScreenRoute

//Onboarding
@Serializable
data object OnboardingScreen

//nested onboarding screens

@Serializable
data object OnboardingWelcomeScreen

@Serializable
data class OnboardingNodeSetupScreen(val restoreWallet: Boolean)

@Serializable
data object OnboardMnemonicScreen

@Serializable
data object OnboardPassPhraseScreen

@Serializable
data class OnboardLoading(val message: String)

@Serializable
data class OnboardSeedScreen(val seed: String)

@Serializable
data object OnboardPinScreen

@Serializable
data object OnboardNodeSetupScreen

//End onboarding
enum class PinEntryType {
    Verify,
    Create
}


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
                },
                onCreateClick = {
                    onboardViewModel.setMode(Mode.CREATE)
                    navController.navigate(OnboardNodeSetupScreen)
                }
            )
        }
        composable<OnboardNodeSetupScreen> {
            SetupNodeComposable(
                onBackPressed = {
                    navController.navigateUp()
                },
                oNextPressed = {
                    navController.navigate(OnboardPassPhraseScreen)
                }
            )
        }
        composable<OnboardPassPhraseScreen> { navBackStackEntry ->
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

        composable<OnboardPinScreen> { navBackStackEntry ->
            PinSetup(
                onNext = { pin ->
                    navController.navigate(OnboardLoading("Creating wallet..."))
                    onboardViewModel.viewModelScope.launch {
                        onboardViewModel.create(pin)
                        delay(600)
                    }.invokeOnCompletion {
                        if (it == null) {
                            navController.navigate(OnboardSeedScreen(onboardViewModel.getSeed()))
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
    }
}