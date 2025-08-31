package io.anonero.ui.onboard.restore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import io.anonero.model.BackupPayload
import io.anonero.model.Wallet
import io.anonero.ui.home.graph.routes.SettingsLogs
import io.anonero.ui.home.settings.LogViewer
import io.anonero.ui.onboard.graph.OnboardLogsScreen
import io.anonero.util.backup.BackupHelper
import io.anonero.util.rememberShakeController
import io.anonero.util.shake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

private const val TAG = "RestorePreview"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestorePreview(
    modifier: Modifier = Modifier,
    backUpPath: String,
    onBackPressed: () -> Unit = {},
    oNextPressed: () -> Unit = {},
            navigateTo: (route: Any) -> Unit = {}
) {

    var passPhrase by remember { mutableStateOf<String>("") }
    var loadingMessage by remember { mutableStateOf<String>("Loading...") }
    var errorMessage by remember { mutableStateOf<String>("") }
    var backupPayload by remember { mutableStateOf<BackupPayload?>(null) }
    var passphraseDialog by remember { mutableStateOf(true) }
     var loading by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val errorShake = rememberShakeController()
    val view = LocalView.current

    fun restoreFromBackup() {
        scope.launch(Dispatchers.IO) {
            try {
                loading = true
                passphraseDialog = false
                loadingMessage = "Extracting backup..."
                backupPayload = BackupHelper.extractBackUp(backUpPath, passPhrase)
                loading = false
            } catch (e: Exception) {
                loading = false
                scope.launch {
                    errorShake.shake(view)
                }
                e.printStackTrace()
            }
        }
    }


    LaunchedEffect(passphraseDialog) {
        scope.launch {
            if (passphraseDialog) {
                delay(100)
                focusRequester.requestFocus()
            }
        }
    }

    if (passphraseDialog) AlertDialog(
        modifier = Modifier
            .shake(errorShake)
            .border(
                1.dp,
                color = MaterialTheme.colorScheme.onSecondary.copy(
                    alpha = .2f
                ),
                shape = MaterialTheme.shapes.medium,
            ), containerColor = MaterialTheme.colorScheme.secondary, properties = DialogProperties(
            securePolicy = SecureFlagPolicy.SecureOn, dismissOnBackPress = false
        ), title = {
            Text(
                text = "Enter Seed Phrase", style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 18.sp
                )
            )
        }, text = {
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
                        restoreFromBackup()
                    }),
                onValueChange = {
                    passPhrase = it
                },

                )
        }, onDismissRequest = {
            passphraseDialog = false
        }, confirmButton = {
            Button(
                shape = MaterialTheme.shapes.small, border = BorderStroke(
                    1.dp, color = MaterialTheme.colorScheme.onBackground
                ), colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,

                    ), onClick = {
                    restoreFromBackup()
                }) { Text("Restore") }
        }, dismissButton = {
            Button(
                onClick = {
                    passphraseDialog = false
                    onBackPressed.invoke()
                }, shape = MaterialTheme.shapes.small, border = BorderStroke(
                    1.dp, color = MaterialTheme.colorScheme.onSecondary
                ), colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Text(
                    "Cancel", style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondary.copy(
                            alpha = 0.8f
                        )
                    )
                )
            }
        })


    if (passphraseDialog) {
        return Box {

        }
    }


    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }, title = {
                Text(
                    "Backup Preview",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondary.copy(
                            alpha = 0.8f
                        )
                    )
                )
            })
        }, bottomBar = {
            if (errorMessage.isEmpty()) {
                OutlinedButton(
                    enabled = !loading, onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                loading = true
                                loadingMessage = "Restoring wallet..."
                                val success = BackupHelper.restoreBackUp(backupPayload!!, passPhrase)
                                if (success) {
                                    BackupHelper.cleanCacheDir()
                                    delay(650)
                                    withContext(Dispatchers.Main) {
                                        oNextPressed()
                                    }
                                }
                                loading = false
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e)
                                e.printStackTrace()
                            } finally {
                                loading = false
                            }
                        }

                    }, modifier = Modifier
                        .fillMaxWidth()
                        .safeContentPadding()
                        .padding(
                            horizontal = 16.dp,
                        ), shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(12.dp)
                ) {
                    Text("Restore Wallet")
                }
            }

        }) { paddingValues ->
        if (errorMessage.isNotEmpty()) {
            return@Scaffold Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    errorMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp
                    )
                )
                OutlinedButton(
                    onClick = {
                         navigateTo(OnboardLogsScreen)
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .safeContentPadding()
                        .padding(
                            horizontal = 16.dp,
                        ), shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(12.dp)
                ) {
                    Text("View Logs")
                }
            }
        }

        if (loading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(140.dp), strokeWidth = 2.dp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    loadingMessage,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSecondary.copy(
                            alpha = 0.8f
                        )
                    )
                )
            }
        }

        if (backupPayload != null && !loading) {
            val wallet = backupPayload!!.backup.wallet
            val node = backupPayload!!.backup.node
            val meta = backupPayload!!.backup.meta
            backupPayload!!.version
            wallet.neroPayload != null
            val neroPayload = wallet.neroPayload

            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(meta.timestamp), java.time.ZoneId.systemDefault()
            ).format(formatter)


            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(
                        horizontal = 16.dp
                    )
                    .fillMaxSize()
            ) {
                if (neroPayload == null) {

                    item {
                        Text(
                            "Wallet ",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                top = 8.dp
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondary.copy(
                                    alpha = 0.8f
                                )
                            )
                        )
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )
                    }
                    item {
                        ListWidget(
                            title = "Seed",
                            subtitle = "${wallet.seed}",
                            modifier = Modifier.padding(
                                bottom = 8.dp
                            )
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ListWidget(
                                title = "Balance",
                                subtitle = "${Wallet.getDisplayAmount(wallet.balanceAll)}",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            ListWidget(
                                title = "Restore Height",
                                subtitle = "${wallet.restoreHeight}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                } else {
                    item {
                        Text(
                            "Wallet | Nero ",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                top = 8.dp
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondary.copy(
                                    alpha = 0.8f
                                )
                            )
                        )
                    }
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )
                    }
                    item {
                        ListWidget(
                            title = "Primary Address",
                            subtitle = "${neroPayload.primaryAddress}",
                            modifier = Modifier.padding(
                                bottom = 8.dp
                            )
                        )
                    }
                    item {
                        ListWidget(
                            title = "Private View Key",
                            subtitle = "${neroPayload.primaryAddress}",
                            modifier = Modifier.padding(
                                bottom = 8.dp
                            )
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
//                            ListWidget(
//                                title = "Restore Height",
//                                subtitle = "${neroPayload.version}",
//                                modifier = Modifier.weight(1f)
//                            )
//                            Spacer(Modifier.width(8.dp))
                            ListWidget(
                                title = "Restore Height",
                                subtitle = "${neroPayload.restoreHeight}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Node ",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = 8.dp
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary.copy(
                                alpha = 0.8f
                            )
                        )
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 8.dp
                        )
                    )
                }
                item {
                    ListWidget(
                        title = "Host",
                        subtitle = "${node.host}:${node.rpcPort}",
                        modifier = Modifier.padding(
                            bottom = 8.dp
                        )
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ListWidget(
                            title = "Username",
                            subtitle = "${node.username}",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        ListWidget(
                            title = "Password",
                            subtitle = "${node.password}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    ListWidget(
                        title = "Network",
                        subtitle = "${node.networkType}",
                        modifier = Modifier.padding(
                            bottom = 8.dp
                        )
                    )
                }

            }
        } else {

        }

    }

}


@Composable
fun ListWidget(
    subtitle: String, title: String, modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.border(
            .5.dp,
            color = MaterialTheme.colorScheme.primary.copy(
                alpha = .8f
            ),
            shape = MaterialTheme.shapes.medium,
        ), headlineContent = {
            Text(
                title, style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp, color = Color.White
                )
            )
        }, supportingContent = {
            Text(subtitle)
        })
}