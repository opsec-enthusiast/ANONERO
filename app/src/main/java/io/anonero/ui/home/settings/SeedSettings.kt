package io.anonero.ui.home.settings

import AnonNeroTheme
import android.content.SharedPreferences
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import io.anonero.AnonConfig
import io.anonero.model.NeroKeyPayload
import io.anonero.model.WalletManager
import io.anonero.ui.components.qr.QrCodeColors
import io.anonero.ui.components.qr.QrCodeView
import io.anonero.util.KeyStoreHelper
import io.anonero.util.PREFS_PASSPHRASE_HASH
import io.anonero.util.ShakeConfig
import io.anonero.util.WALLET_PREFERENCES
import io.anonero.util.rememberShakeController
import io.anonero.util.shake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.random.Random


fun getWalletSeed(passPhrase: String): String? {
    return WalletManager.instance?.wallet?.getSeed(passPhrase)
}

fun generatePlaceHolderSeed(): List<String> {
    return (1..16).map {
        (1..Random.nextInt(4, 8)).joinToString("") { "*" }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedSettingsPage(onBackPress: () -> Unit = {}) {

    val prefs = koinInject<SharedPreferences>(named(WALLET_PREFERENCES))

    var seedWords by remember {
        mutableStateOf(
            generatePlaceHolderSeed()
        )
    }
    val focusRequester = remember { FocusRequester() }
    var passPhraseDialog by remember { mutableStateOf(true) }
    var passPhrase by remember { mutableStateOf("") }
    var neroPayload by remember { mutableStateOf("") }
    val errorShake = rememberShakeController()
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val backgroundBlur: Float by animateFloatAsState(
        if (passPhraseDialog) 6f else 0f,
        label = "blur-radius"
    )


    fun exportForNero() {
        val wallet = WalletManager.instance?.wallet ?: return
        if (passPhrase.isEmpty()) {
            passPhraseDialog = true
            return
        }
        val payload = Json.encodeToString(NeroKeyPayload.fromWallet(wallet))
        neroPayload = payload.toString();
    }


    LaunchedEffect(true) {
        scope.launch {
            delay(100)
            focusRequester.requestFocus()
        }
    }
    fun getSeed() {
        scope.launch(Dispatchers.IO) {
            val hash = prefs.getString(PREFS_PASSPHRASE_HASH, "")
            val hashedPass = KeyStoreHelper.getCrazyPass(AnonConfig.context, passPhrase)
            if (hash == hashedPass) {
                val seed = getWalletSeed(passPhrase)?.split(" ")
                if (seed != null) {
                    seedWords = seed
                    passPhraseDialog = false
                }
            } else {
                errorShake.shake(
                    ShakeConfig(
                        6, translateX = 5f
                    )
                )
                repeat(6) {
                    delay(50)
                    view.performHapticFeedback(
                        HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
                delay(100)
            }

        }
    }
    if (neroPayload.isNotEmpty()) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
            ),
            scrimColor = MaterialTheme.colorScheme.background.copy(
                alpha = 0.5f
            ),
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                neroPayload = ""
            }
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.padding(8.dp))
                Text(
                    "[ИΞR0] keys",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Box(modifier = Modifier.padding(8.dp))
                QrCodeView(
                    data = neroPayload,
                    colors = QrCodeColors(
                        background = MaterialTheme.colorScheme.background,
                        foreground = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier
                        .size(320.dp)
                        .padding(20.dp)
                )
                Box(modifier = Modifier.padding(16.dp))
            }
        }
    }


    if (passPhraseDialog)
        AlertDialog(
            modifier = Modifier
                .shake(errorShake)
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(
                        alpha = .2f
                    ),
                    shape = MaterialTheme.shapes.medium,
                ),
            containerColor = MaterialTheme.colorScheme.secondary,
            properties = DialogProperties(
                securePolicy = SecureFlagPolicy.SecureOn, dismissOnBackPress = false
            ),
            title = {
                Text(
                    text = "Enter Seed Phrase",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 18.sp
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = passPhrase,
                    shape = MaterialTheme.shapes.small,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(
                            top = 8.dp
                        ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            getSeed()
                        }
                    ),
                    onValueChange = {
                        passPhrase = it
                    },

                    )
            },
            onDismissRequest = {
                passPhraseDialog = false
            },
            confirmButton = {
                Button(
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        1.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,

                        ),
                    onClick = {
                        getSeed()
                    }) { Text("View") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        passPhraseDialog = false
                        onBackPress.invoke()
                    },
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        1.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary.copy(
                                alpha = 0.8f
                            )
                        )
                    )
                }
            }
        )


    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text("")
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    1.dp,
                    color = MaterialTheme.colorScheme.onSecondary
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                contentPadding = PaddingValues(
                    all = 12.dp
                ),
                modifier = Modifier.fillMaxWidth(0.85f),
                onClick = {
                    exportForNero()
                }) {
                Text(
                    "Export [ИΞR0] keys", style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondary.copy(
                            alpha = 0.8f
                        )
                    )
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .blur(backgroundBlur.dp)
                .padding(
                    horizontal = 8.dp
                )
        ) {
            LazyVerticalGrid(
                modifier = Modifier.padding(
                    vertical = 12.dp,
                    horizontal = 8.dp
                ),
                columns = GridCells.Fixed(
                    2
                )
            ) {
                items(seedWords.size) { index ->
                    Column(
                        modifier = Modifier.padding(
                            vertical = 4.dp
                        ), Arrangement.Center, Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                vertical = 12.dp,
                                horizontal = 8.dp
                            )
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme
                                    .typography.titleMedium.copy(
                                        fontWeight = FontWeight.W800,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.8f
                                        )
                                    ),
                                modifier = Modifier
                                    .width(26.dp)
                                    .align(Alignment.CenterVertically),
                            )
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    seedWords[index],
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                HorizontalDivider(
                                    color = Color.Gray,
                                    thickness = .5.dp
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(device = "id:pixel_5")
@Composable
private fun SeedSettingsPre() {
    AnonNeroTheme {
        SeedSettingsPage()
    }
}