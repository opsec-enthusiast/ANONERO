package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.AnonConfig
import io.anonero.icons.AnonIcons
import io.anonero.model.TransactionInfo
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.services.WalletState
import io.anonero.ui.components.WalletProgressIndicator
import io.anonero.ui.home.graph.routes.CoinsScreenRoute
import io.anonero.ui.home.graph.routes.ReviewTransactionRoute
import io.anonero.ui.home.graph.routes.SendScreenRoute
import io.anonero.ui.home.spend.qr.ExportType
import io.anonero.ui.home.spend.qr.ImportEvents
import io.anonero.ui.home.spend.qr.QRExchangeScreen
import io.anonero.ui.home.spend.qr.SpendQRExchangeParam
import io.anonero.ui.home.spend.qr.URQRScanner
import io.anonero.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber


class TransactionsViewModel : ViewModel() {
    private val walletState: WalletState by inject(WalletState::class.java)

    val balance = walletState.balanceInfo.map {
        it ?: 0L
    }.asLiveData()

    val transactions = walletState.transactions
        .asLiveData()

}

private const val TAG = "Transactions"

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    onItemClick: (TransactionInfo) -> Unit = {},
    navigateTo: (route: Any) -> Unit = {},
    navigateToShortCut: (shortcut: LockScreenShortCut) -> Unit = {},
    animatedContentScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope,
) {

    val transactionsViewModel = viewModel<TransactionsViewModel>()
    val balance by transactionsViewModel.balance.observeAsState()
    val transactions by transactionsViewModel.transactions.observeAsState(listOf())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showLockScreen by remember { mutableStateOf(false) }
    var scanFailure by remember { mutableStateOf<String?>(null) }
    var spendDialog by remember { mutableStateOf<String?>(null) }
    var broadcastSignedTxPath by remember { mutableStateOf<String?>(null) }
    var broadcastProgree by remember { mutableStateOf(false) }
    var settingBSync by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var qrScannerParam by remember { mutableStateOf<SpendQRExchangeParam?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val walletState = koinInject<WalletState>()

    val scope = rememberCoroutineScope()
    if (showLockScreen) {
        Dialog(
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
                decorFitsSystemWindows = false
            ),
            onDismissRequest = {
                showLockScreen = false
            }) {
            LockScreen(
                mode = LockScreenMode.LOCK_SCREEN,
                modifier = Modifier.fillMaxHeight(0.95f),
                onUnLocked = { _, shortCut ->
                    scope.launch {
                        walletState.setBackGroundSync(false)
                        if (shortCut != LockScreenShortCut.HOME) {
                            navigateToShortCut(shortCut)
                            delay(130)
                        }
                        showLockScreen = false
                        withContext(Dispatchers.IO) {
                            walletState.update()
                        }
                    }
                }
            )
        }
    }

    if (broadcastSignedTxPath != null) {
        AlertDialog(
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(
                        alpha = .2f
                    ),
                    shape = MaterialTheme.shapes.medium,
                ),
            containerColor = MaterialTheme.colorScheme.background,
            properties = DialogProperties(
                securePolicy = SecureFlagPolicy.SecureOn, dismissOnBackPress = false
            ),
            title = {
                Text(
                    text = "Anon",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 18.sp
                    )
                )
            },
            text = {
                Text("Do you want to broadcast transaction?")
            },
            onDismissRequest = {
                scanFailure = null
            },
            dismissButton = {
                Button(
                    onClick = {
                        broadcastSignedTxPath = null
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
                        broadcastProgree = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                WalletManager.instance?.wallet?.submitTransaction(
                                    broadcastSignedTxPath ?: ""
                                )
                                WalletManager.instance?.wallet?.refreshHistory()
                                WalletManager.instance?.wallet?.store()
                            } catch (ex: Exception) {
                                Timber.tag(TAG).e(ex)
                            } finally {
                                broadcastProgree = false
                                broadcastSignedTxPath = null
                            }
                        }
                    }) { Text("Yes") }
            },
        )
    }

    if (qrScannerParam != null) {
        QRExchangeScreen(
            params = qrScannerParam!!,
            onBackPressed = {
                qrScannerParam = null
            },
            onCtaCalled = {
                qrScannerParam = null
                showScanner = true
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    if (showScanner)
        URQRScanner(
            onQRCodeScanned = {
                if (it.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        SendScreenRoute.parse(it)?.let { paymentUri ->
                            withContext(Dispatchers.Main) {
                                navigateTo(paymentUri)
                            }
                        }
                    }
                }
                showScanner = false
            },
            onDismiss = {
                showScanner = false
            },
            onURResult = {
                when (it.getOrNull()) {
                    ImportEvents.IMPORT_OUTPUTS -> {
                        showScanner = false
                        qrScannerParam = SpendQRExchangeParam(
                            exportType = ExportType.IMAGE,
                            title = "KEY IMAGES",
                            ctaText = "SCAN UNSIGNED TX",
                        )
                    }

                    null -> {

                    }

                    ImportEvents.IMPORT_KEY_IMAGES -> {}
                    ImportEvents.IMPORT_UNSIGNED_TX -> {
                        if (!AnonConfig.viewOnly) {
                            navigateTo.invoke(ReviewTransactionRoute(
                                "",
                            ));
                        }
                    }
                    ImportEvents.IMPORT_SIGNED_TX -> {}
                }
                showScanner = false
            },
            showScanner = showScanner
        )

    if (spendDialog != null) {
        AlertDialog(
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(
                        alpha = .2f
                    ),
                    shape = MaterialTheme.shapes.medium,
                ),
            containerColor = MaterialTheme.colorScheme.background,
            properties = DialogProperties(
                securePolicy = SecureFlagPolicy.SecureOn, dismissOnBackPress = false
            ),
            title = {
                Text(
                    text = "Anon",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 18.sp
                    )
                )
            },
            text = {
                Text("${spendDialog}")
            },
            onDismissRequest = {
                spendDialog = null
            },
            dismissButton = {
                Button(
                    onClick = {
                        spendDialog = null
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
                        "Ok",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary.copy(
                                alpha = 0.8f
                            )
                        )
                    )
                }
            },
            confirmButton = {

            },
        )
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                scrollBehavior = scrollBehavior,
                title = {
                    Text(if (AnonConfig.viewOnly) "[ИΞR0]" else "[ΛИ0И]")
                },
                actions = {
                    LockButton(
                        onLock = {
                            scope.launch {
                                try {
                                    settingBSync = true
                                    walletState.blockUpdates(true)
                                    withContext(Dispatchers.IO) {
                                        WalletManager.instance?.wallet?.let { wallet: Wallet ->
                                            if (wallet.startBackgroundSync()) {
                                                walletState.setBackGroundSync(true)
                                                showLockScreen = true
                                            }
                                        }
                                    }
                                } finally {
                                    walletState.blockUpdates(false)
                                    settingBSync = false
                                }
                            }
                        }, loading = settingBSync
                    )
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        ),
                        onClick = {
                            showScanner = true
                        }
                    ) {
                        Icon(AnonIcons.Scan, contentDescription = "Scan")
                    }
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        ),
                        onClick = {
                            showMenu = !showMenu
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                        DropdownMenu(
                            expanded = showMenu,
                            containerColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(
                                        alpha = .2f
                                    ),
                                    shape = MaterialTheme.shapes.medium,
                                ),
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Coin Control") },
                                onClick = {
                                    navigateTo(CoinsScreenRoute)
                                }
                            )
                            if (AnonConfig.viewOnly) {
                                DropdownMenuItem(
                                    text = { Text("Show Outputs") },
                                    onClick = {
                                        qrScannerParam =
                                            SpendQRExchangeParam(
                                                exportType = ExportType.OUTPUT,
                                                title = "OUTPUTS",
                                                ctaText = "SCAN KEY IMAGES",
                                            )
                                    }
                                )
                            }
                            if (!AnonConfig.viewOnly) {
                                DropdownMenuItem(
                                    text = { Text("Export Key Images") },
                                    onClick = {
                                        qrScannerParam =
                                            SpendQRExchangeParam(
                                                exportType = ExportType.IMAGE,
                                                title = "KEY IMAGES",
                                                ctaText = "",
                                            )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = contentPadding
        ) {
            stickyHeader {
                WalletProgressIndicator()
            }
            item {
                Box(
                    modifier = Modifier
                        .padding(
                            vertical = 32.dp
                        )
                        . combinedClickable(
                            onClick = {},
                            onLongClick = {
                                navigateTo(CoinsScreenRoute)
                            }
                        )
                        .fillParentMaxWidth()
                ) {
                    Text(
                        Formats.getDisplayAmount(balance ?: 0),
                        style = MaterialTheme.typography
                            .displaySmall,
                        modifier = Modifier.fillParentMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(transactions.size, key = { transactions[it].getListKey() }) {
                with(sharedTransitionScope) {
                    TransactionItem(
                        transactions[it], modifier = Modifier
                            .clickable {
                                onItemClick(transactions[it])
                            }
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(
                                    key = "${transactions[it].hash}",
                                ),
                                animatedVisibilityScope = animatedContentScope
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun TransactionItem(tx: TransactionInfo, modifier: Modifier = Modifier) {
    val isIncoming = tx.direction == TransactionInfo.Direction.Direction_In
    val amount = if (isIncoming) tx.amount else tx.amount
    val confirmations = tx.confirmations
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 20.dp
            )
            .border(
                border = BorderStroke(
                    1.dp,
                    Color.Black
                ),
                shape = MaterialTheme.shapes.medium
            )
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.padding(top = 2.dp)) {
            if (confirmations >= 10)
                Icon(
                    if (isIncoming) AnonIcons.ArrowDownLeft else AnonIcons.ArrowUpRight,
                    modifier = Modifier.size(32.dp),
                    tint = if (isIncoming) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    contentDescription = ""
                )
            else
                Box(
                    modifier = Modifier
                        .size(28.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        progress = {
                            ((confirmations.toFloat()) / (10f))
                        }
                    )
                    Text(
                        text = "$confirmations",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.sp
                        )
                    )
                }
        }
        Text(
            Formats.getDisplayAmount(amount),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            Formats.formatTransactionTime(tx.timestamp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun LockButton(onLock: () -> Unit, loading: Boolean = false) {
    val walletState = koinInject<WalletState>()
    val walletLoading by walletState.isLoading.asLiveData().observeAsState(false)
    IconButton(
        modifier = Modifier.alpha(if (walletLoading) 0.2f else 1.0f),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.White
        ),
        onClick = {
            if (walletLoading || loading) {
                return@IconButton
            }
            onLock()
        }
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Lock, contentDescription = "Lock")
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
private fun TransactionScreenReview() {
    AnonNeroTheme {
//        TransactionScreen(
//            animatedContentScope = this@composable,
//            sharedTransitionScope = this@SharedTransitionLayout
//        )
    }
}
