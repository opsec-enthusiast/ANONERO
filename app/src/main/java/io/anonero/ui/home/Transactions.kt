package io.anonero.ui.home

import AnonNeroTheme
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.icons.AnonIcons
import io.anonero.model.TransactionInfo
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.services.WalletState
import io.anonero.ui.components.WalletProgressIndicator
import io.anonero.ui.components.scanner.QRScannerDialog
import io.anonero.ui.home.graph.routes.SendScreenRoute
import io.anonero.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject


class TransactionsViewModel : ViewModel() {
    private val walletState: WalletState by inject(WalletState::class.java)

    val balance = walletState.balanceInfo.map {
        it ?: 0L
    }.asLiveData()

    val transactions = walletState.transactions
        .asLiveData()

}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    onItemClick: (TransactionInfo) -> Unit = {},
    navigateToSend: (paymentUri: SendScreenRoute) -> Unit = {},
    navigateToShortCut: (shortcut: LockScreenShortCut) -> Unit = {},
    animatedContentScope: AnimatedContentScope,
    sharedTransitionScope: SharedTransitionScope,
) {

    val transactionsViewModel = viewModel<TransactionsViewModel>()
    val balance by transactionsViewModel.balance.observeAsState()
    val transactions by transactionsViewModel.transactions.observeAsState(listOf())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showLockScreen by remember { mutableStateOf(false) }
    var settingBSync by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
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

    QRScannerDialog(
        show = showScanner,
        onQRCodeScanned = {
            if (it.isNotEmpty()) {
                scope.launch(Dispatchers.IO) {
                    SendScreenRoute.parse(it)?.let { paymentUri ->
                        withContext(Dispatchers.Main) {
                            navigateToSend(paymentUri)
                        }
                    }
                }
            }
            showScanner = false
        },
        onDismiss = {
            showScanner = false
        }
    )


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
                    Text("[ΛИ0И]")
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

                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
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
                            vertical = 12.dp
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
    val amount = if (isIncoming) tx.amount else tx.amount * -1
    val confirmations = tx.confirmations
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
            .border(
                border = BorderStroke(
                    1.dp,
                    Color.White
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
        Box {
            if (confirmations >= 10)
                Icon(
                    if (isIncoming) AnonIcons.ArrowDownLeft else AnonIcons.ArrowUpRight,
                    modifier = Modifier.size(28.dp),
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
            style = MaterialTheme.typography.titleMedium
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