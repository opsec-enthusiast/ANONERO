package io.anonero.ui.home

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.icons.AnonIcons
import io.anonero.model.PendingTransaction
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.services.WalletState
import io.anonero.ui.components.scanner.QRScannerDialog
import io.anonero.ui.home.graph.routes.ReviewTransactionRoute
import io.anonero.ui.home.graph.routes.SendScreenRoute
import io.anonero.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

private const val TAG = "Send"


enum class SpendType {
    NORMAL,
    SWEEP
}

class SendViewModel : ViewModel() {
    private val walletState: WalletState by inject(WalletState::class.java)
    val balance = walletState.unLockedBalance.asLiveData()
    private val _paymentUri = MutableLiveData<SendScreenRoute?>(null)
    private val _spendType = MutableLiveData(SpendType.NORMAL)
    val paymentUri = _paymentUri as LiveData<SendScreenRoute?>
    val spendType = _spendType as LiveData<SpendType>

    suspend fun prepareTransaction(addressField: String, amount: String): PendingTransaction? {
        val amountFromString: Long = Wallet.getAmountFromString(amount)
        val wallet = WalletManager.instance?.wallet
        return withContext(Dispatchers.IO) {
            try {
                val balance = wallet?.unlockedBalance ?: 0L
                if (spendType.value == SpendType.SWEEP) {
                    if (wallet?.unlockedBalance != wallet?.balance) {
                        return@withContext null
                    }
                    wallet?.createSweepTransaction(
                        dstAddr = addressField,
                        priority = PendingTransaction.Priority.Priority_Medium,
                        keyImages = arrayListOf()
                    )
                } else {
                    wallet?.createTransaction(
                        dst_addr = addressField,
                        amount = amountFromString
                    )
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                null
            }
        }
    }

    fun handleScan(data: String) {
        viewModelScope.launch(Dispatchers.Default) {
            SendScreenRoute.parse(data)?.let {
                _paymentUri.postValue(it)
            }
        }
    }

    fun setSpendType(type: SpendType) {
        _spendType.postValue(type)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    onBackPress: () -> Unit = {},
    navigateToReview: (route: ReviewTransactionRoute) -> Unit = {},
    paymentUri: SendScreenRoute? = null,
    modifier: Modifier =Modifier
) {
    var addressField by rememberSaveable { mutableStateOf("") }
    var amountField by rememberSaveable { mutableStateOf("") }
    var validSpend by rememberSaveable { mutableStateOf(false) }
    var preparingTx by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var inValidAddress by remember { mutableStateOf<Boolean?>(null) }
    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
    val scope = rememberCoroutineScope()
    val sendViewModel = viewModel<SendViewModel>()
    val unlockedBalance by sendViewModel.balance.observeAsState(0L)
    val paymentUriFromScanner by sendViewModel.paymentUri.observeAsState(null)
    val spendType by sendViewModel.spendType.observeAsState(SpendType.NORMAL)
    val view = LocalView.current
    val unLockedAmount = Formats.getDisplayAmount(
        unlockedBalance ?: 0L
    )

    LaunchedEffect(paymentUriFromScanner) {
        paymentUriFromScanner?.let {
            if (it.address.isNotEmpty())
                addressField = it.address
            if (it.amount != 0.0)
                amountField =
                    Wallet.getDisplayAmount(Wallet.getAmountFromString(it.amount.toString()))
        }
    }

    LaunchedEffect(paymentUri) {
        paymentUri?.let {
            if (it.address.isNotEmpty())
                addressField = it.address
            if (it.amount != 0.0)
                amountField =
                    Wallet.getDisplayAmount(Wallet.getAmountFromString(it.amount.toString()))
        }
    }
    val sweep = spendType == SpendType.SWEEP

    LaunchedEffect(addressField, amountField) {
        scope.launch {
            unlockedBalance?.let { maxSpendableBalance ->
                val amountFromString = Wallet.getAmountFromString(amountField)
                validSpend = when {
                    inValidAddress == true -> false
                    amountFromString == 0L || maxSpendableBalance == 0L -> false
                    amountFromString > maxSpendableBalance -> false
                    else -> true
                }
            }
        }.invokeOnCompletion {
            if (it != null) {
                Timber.tag(TAG).e(it)
            }
        }
    }

    QRScannerDialog(
        show = showScanner,
        onQRCodeScanned = {
            sendViewModel.handleScan(it)
            showScanner = false
        },
        onDismiss = {
            showScanner = false
        }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackPress
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {

                }
            )
        }
    ) { padding ->

        if (preparingTx) {

            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(300.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(
                        bottom = 64.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Address",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        supportingContent = {
                            OutlinedTextField(
                                value = addressField,
                                shape = MaterialTheme.shapes.medium,
                                minLines = 4,
                                isError = inValidAddress == true,
                                supportingText = {
                                    if (inValidAddress == true) {
                                        Text("invalid address")
                                    }
                                },
                                onValueChange = {
                                    addressField = it.trim()
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                ),
                                placeholder = {
                                    Text(
                                        text = "",
                                        color = labelColor
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        if (!it.hasFocus && addressField.isNotEmpty()) {
                                            scope.launch(Dispatchers.Default) {
                                                inValidAddress =
                                                    WalletManager.instance?.wallet?.validateAddress(
                                                        addressField
                                                    ) != true
                                            }
                                        } else {
                                            if (addressField.isEmpty())
                                                inValidAddress = null
                                        }

                                    }
                                    .focusable()
                            )
                        },
                    )

                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Amount",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        supportingContent = {
                            OutlinedTextField(
                                value = amountField,
                                shape = MaterialTheme.shapes.medium,
                                placeholder = {
                                    Text(
                                        text = "",
                                        color = labelColor
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Number
                                ),
                                onValueChange = {
                                    amountField = it.trim()
                                },

                                )
                        },
                    )
                    Text(
                        if (sweep) "Sweeping balance: $unLockedAmount (minus fees)" else "Available balance: $unLockedAmount ",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                amountField = if (!sweep) {
                                    sendViewModel.setSpendType(SpendType.SWEEP)
                                    unLockedAmount
                                } else {
                                    sendViewModel.setSpendType(SpendType.NORMAL)
                                    ""
                                }
                            },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium
                    )
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            showScanner = true
                        }
                    ) {
                        Icon(AnonIcons.Scan, contentDescription = "")
                    }
                }

                OutlinedButton(
                    enabled = validSpend,
                    onClick = {
                        scope.launch {
                            try {
                                preparingTx = true
                                val pendingTx =
                                    sendViewModel.prepareTransaction(addressField, amountField)
                                if (pendingTx != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstants.CONFIRM
                                        )
                                    }
                                    navigateToReview.invoke(ReviewTransactionRoute(addressField))
                                } else {
                                    //show error
                                }
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e)
                            } finally {
                                preparingTx = false
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
                    Text("NEXT")
                }
            }

        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun SendScreenPrev() {
    SendScreen(modifier = Modifier)
}