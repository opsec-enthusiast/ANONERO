package io.anonero.ui.home.spend

import AnonNeroTheme
import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.rememberCoroutineScope
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
import io.anonero.model.PendingTransaction
import io.anonero.model.WalletManager
import io.anonero.ui.home.graph.ReviewTransactionRoute
import io.anonero.ui.theme.DangerColor
import io.anonero.ui.theme.SuccessColor
import io.anonero.util.Formats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BroadcastState {
    STAGING,
    IN_PROGRESS,
    SUCCESS,
    ERROR
}

class ReviewTransactionViewModel : ViewModel() {
    private val pendingTransaction = MutableLiveData<PendingTransaction?>(null)
    val pendingTransactionLive: LiveData<PendingTransaction?> = pendingTransaction;
    val broadcastingTx = MutableLiveData(BroadcastState.STAGING)
    val broadcastingTxState: LiveData<BroadcastState> = broadcastingTx
    private var _brodcastError: Exception? = null

    val broadCastError get() = _brodcastError

    init {
        WalletManager.instance?.wallet?.getPendingTx().let {
            pendingTransaction.postValue(it)
        }
    }

    fun broadCast(): Job? {
        if (broadcastingTx.value == BroadcastState.IN_PROGRESS) {
            return null
        }
        broadcastingTx.postValue(BroadcastState.IN_PROGRESS)
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(2000)
                pendingTransaction.value?.let { WalletManager.instance?.wallet?.send(it) }
                WalletManager.instance?.wallet?.refreshHistory()
                WalletManager.instance?.wallet?.store()
                broadcastingTx.postValue(BroadcastState.SUCCESS)
            } catch (ex: Exception) {
                _brodcastError = ex
                broadcastingTx.postValue(BroadcastState.ERROR)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewTransactionScreen(reviewParams: ReviewTransactionRoute,onFinished:() -> Unit = {}, onBackPressed: () -> Unit = {}) {
//
    val viewModel = viewModel<ReviewTransactionViewModel>()
    val pendingTransaction :PendingTransaction?= null;
//    val pendingTransaction by viewModel.pendingTransactionLive.observeAsState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
//
    val broadcastState = BroadcastState.STAGING
//    val broadcastState by viewModel.broadcastingTxState.observeAsState(BroadcastState.STAGING)

    val titleStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp
    )
    val subTitleStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 14.sp

    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(broadcastState == BroadcastState.SUCCESS){
                                onFinished.invoke()
                            }else{
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
            modifier = Modifier.padding(it)
        ) {
            if (pendingTransaction != null) {
                AnimatedVisibility(visible = broadcastState == BroadcastState.ERROR, enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Unable to broadcast transaction\n ${viewModel.broadCastError?.message}", style = MaterialTheme.typography.bodyMedium.copy(
                            color = DangerColor
                        ), textAlign = TextAlign.Center)
                    }
                }
                AnimatedVisibility(visible = broadcastState == BroadcastState.SUCCESS,enter = fadeIn(), exit = fadeOut()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Check" , tint = SuccessColor, modifier = Modifier.size(44.dp))
                        Text("Success")
                    }
                }
                AnimatedVisibility(visible = broadcastState == BroadcastState.IN_PROGRESS,enter = fadeIn(), exit = fadeOut()) {
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
                AnimatedVisibility(visible = broadcastState == BroadcastState.STAGING,enter = fadeIn(), exit = fadeOut()) {
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
                                            reviewParams.toAddress,
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
                                                pendingTransaction!!.getAmount(),
                                                8
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
                                                pendingTransaction!!.getFee(),
                                                8
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
                                                pendingTransaction!!.getFee() + pendingTransaction!!.getAmount(),
                                                8
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
                                viewModel.broadCast()?.invokeOnCompletion { error->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        if (error == null) {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstants.CONFIRM
                                            )
                                        }else{
                                            view.performHapticFeedback(
                                                HapticFeedbackConstants.REJECT
                                            )
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
                            Text("CONFIRM")
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
        ReviewTransactionScreen(ReviewTransactionRoute("address"))
    }
}