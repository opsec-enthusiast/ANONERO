package io.anonero.ui.home.spend

import AnonNeroTheme
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.AnonConfig
import io.anonero.model.AnonUrRegistryTypes
import io.anonero.model.PendingTransaction
import io.anonero.model.UnsignedTransaction
import io.anonero.model.WalletManager
import io.anonero.ui.components.scanner.QRScannerDialog
import io.anonero.ui.home.graph.routes.ReviewTransactionRoute
import io.anonero.ui.theme.DangerColor
import io.anonero.ui.theme.SuccessColor
import io.anonero.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

enum class BroadcastState {
    STAGING,
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

data class ReviewModel(
    val address: String?,
    val amount: Long,
    val fee: Long,
    val total: Long,
    val txId: String
)

class ReviewTransactionViewModel : ViewModel() {
    private val reviewModel = MutableLiveData<ReviewModel?>(null)
    private val broadcastingTx = MutableLiveData(BroadcastState.STAGING)
    private var _broadcastError: Exception? = null

    val broadCastError get() = _broadcastError
    val getBroadcastingTxState get() = broadcastingTx
    val reviewModelLive: LiveData<ReviewModel?> get() = reviewModel

    var pendingTransaction: PendingTransaction? = null;
    var unsignedTransaction: UnsignedTransaction? = null;

    public fun isUnsignedTransaction(): Boolean {
        return unsignedTransaction != null
    }

    init {
        val wallet = WalletManager.instance?.wallet;
        val pendingTransaction = wallet?.getPendingTx()
        val unsignedTransaction = wallet?.getUnsginedTx()

        if (unsignedTransaction != null) {
            this.unsignedTransaction = unsignedTransaction
            reviewModel.postValue(
                ReviewModel(
                    address = unsignedTransaction.address ?: "",
                    amount = unsignedTransaction.amount,
                    fee = unsignedTransaction.fee,
                    total = unsignedTransaction.fee + unsignedTransaction.amount,
                    txId = unsignedTransaction.firstTxId
                )
            )
        } else if (pendingTransaction != null) {
            this.pendingTransaction = pendingTransaction
            reviewModel.postValue(
                ReviewModel(
                    address = null,
                    amount = pendingTransaction.getAmount(),
                    fee = pendingTransaction.getFee(),
                    total = pendingTransaction.getFee() + pendingTransaction.getAmount(),
                    txId = pendingTransaction.firstTxId
                )
            )
        }
    }

    fun broadCast(): Job? {
        if (broadcastingTx.value == BroadcastState.IN_PROGRESS) {
            return null
        }
        val signedTxFile = File(
            AnonConfig.context?.cacheDir,
            AnonConfig.IMPORT_SIGNED_TX_FILE
        );
        broadcastingTx.postValue(BroadcastState.IN_PROGRESS)
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                if (pendingTransaction != null) {
                    pendingTransaction?.let { WalletManager.instance?.wallet?.send(it) }
                }
                if ( signedTxFile.exists()) {
                    WalletManager.instance?.wallet?.submitTransaction(signedTxFile.absolutePath)
                    AnonConfig.context?.let {
                        AnonConfig.clearSpendCacheFiles(it)
                    }
                }
                WalletManager.instance?.wallet?.refreshHistory()
                WalletManager.instance?.wallet?.store()
                broadcastingTx.postValue(BroadcastState.SUCCESS)
            } catch (ex: Exception) {
                _broadcastError = ex
                broadcastingTx.postValue(BroadcastState.ERROR)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTransactionScreen(
    reviewParams: ReviewTransactionRoute,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {

    var qrScannerParam by remember { mutableStateOf<SpendQRExchangeParam?>(null) }
    val viewModel = viewModel<ReviewTransactionViewModel>()
    val reviewModel by viewModel.reviewModelLive.observeAsState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val broadcastState by viewModel.getBroadcastingTxState.observeAsState(BroadcastState.STAGING)
    var showScanner by remember { mutableStateOf(false) }
    var signing by remember { mutableStateOf(false) }
    var readyToBroadcast by remember { mutableStateOf(!AnonConfig.viewOnly) }

    val titleStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp
    )
    val subTitleStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp
    )

    BackHandler {
        onBackPressed()
    }

    QRScannerDialog(
        show = showScanner,
        onQRCodeScanned = {
            showScanner = false
        },
        onUrRusult = {
            showScanner = false
            val ur = it.ur;
            qrScannerParam = null;
            if (ur.type == AnonUrRegistryTypes.XMR_TX_SIGNED.type) {
                readyToBroadcast = true
            }
        },
        onDismiss = {
            showScanner = false
        }
    )

    QRExchangeDialog(
        show = qrScannerParam != null,
        params = qrScannerParam,
        onCtaCalled = {
            showScanner = true
        },
        onDismiss = {
            qrScannerParam = null
        },
    )

    val ctaText = if (AnonConfig.viewOnly) {
        if (!viewModel.isUnsignedTransaction() && !readyToBroadcast) {
            "Share unsigned transaction"
        } else {
            "Broadcast transaction"
        }
    } else {
        if (viewModel.isUnsignedTransaction()) {
            "Sign and share transaction"
        } else {
            "Broadcast transaction"
        }
    }



    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (broadcastState == BroadcastState.SUCCESS) {
                                onFinished.invoke()
                            } else {
                                onBackPressed.invoke()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {

                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(bottom = 0.dp)
        ) {
            if (reviewModel != null) {
                AnimatedVisibility(
                    visible = broadcastState == BroadcastState.ERROR,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Unable to broadcast transaction\n ${viewModel.broadCastError?.message}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DangerColor
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                AnimatedVisibility(
                    visible = broadcastState == BroadcastState.SUCCESS,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Check",
                            tint = SuccessColor,
                            modifier = Modifier.size(44.dp)
                        )
                        Text("Success")
                    }
                }
                AnimatedVisibility(
                    visible = broadcastState == BroadcastState.IN_PROGRESS,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(300.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Broadcast in progress...", modifier = Modifier.padding(top = 12.dp))
                    }
                }
                AnimatedVisibility(
                    visible = broadcastState == BroadcastState.STAGING,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        LazyColumn {
                            item {
                                ListItem(
                                    headlineContent = { Text("Address", style = titleStyle) },
                                    supportingContent = {
                                        Text(
                                            reviewModel!!.address ?: reviewParams.toAddress,
                                            style = subTitleStyle
                                        )
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text("Amount", style = titleStyle) },
                                    supportingContent = {
                                        Text(
                                            Formats.getDisplayAmount(
                                                reviewModel!!.amount
                                            ),
                                            style = subTitleStyle
                                        )
                                    }
                                )
                            }
                            item {

                                ListItem(
                                    headlineContent = { Text("Fee", style = titleStyle) },
                                    supportingContent = {
                                        Text(
                                            Formats.getDisplayAmount(
                                                reviewModel!!.fee
                                            ),
                                            style = subTitleStyle
                                        )
                                    }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text("Total", style = titleStyle) },
                                    supportingContent = {
                                        Text(
                                            Formats.getDisplayAmount(
                                                reviewModel!!.total
                                            ),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    vertical = 24.dp
                                                )
                                        )
                                    }
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                if (viewModel.isUnsignedTransaction()) {
                                    scope.launch {
                                        val unsignedTransaction = File(
                                            AnonConfig.context?.cacheDir,
                                            AnonConfig.IMPORT_UNSIGNED_TX_FILE
                                        );
                                        val signedTransaction = File(
                                            AnonConfig.context?.cacheDir,
                                            AnonConfig.EXPORT_SIGNED_TX_FILE
                                        );
                                        signing = true
                                        WalletManager.instance?.wallet?.signAndExportJ(
                                            unsignedTransaction.absolutePath,
                                            signedTransaction.absolutePath,
                                        )
                                        signing = false
                                        qrScannerParam = SpendQRExchangeParam(
                                            exportType = ExportType.SIGNED_TX,
                                            title = "Signed transaction",
                                            ctaText = "Share signed transaction",
                                        )
                                    }
                                } else {
                                    if (AnonConfig.viewOnly && !readyToBroadcast) {
                                        qrScannerParam = SpendQRExchangeParam(
                                            exportType = ExportType.UN_SIGNED_TX,
                                            title = "Unsigned transaction",
                                            ctaText = "Scan signed transaction",
                                        )
                                    } else {
                                        viewModel.broadCast()?.invokeOnCompletion { error ->
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                if (error == null) {
                                                    view.performHapticFeedback(
                                                        HapticFeedbackConstants.CONFIRM
                                                    )
                                                } else {
                                                    view.performHapticFeedback(
                                                        HapticFeedbackConstants.REJECT
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 12.dp
                                ),

                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            if (signing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(ctaText)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ReviewTransactionScreenPreview() {
    AnonNeroTheme {
        ReviewTransactionScreen(
            ReviewTransactionRoute("address"),
        )
    }
}