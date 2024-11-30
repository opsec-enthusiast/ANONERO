package io.anonero.ui.home

import AnonNeroTheme
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.icons.AnonIcons
import io.anonero.model.TransactionInfo
import io.anonero.services.WalletRepo
import io.anonero.util.Formats
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.inject


class TransactionsViewModel : ViewModel() {
    private val walletRepo: WalletRepo by inject(WalletRepo::class.java)

    val balance = walletRepo.balanceInfo.map {
        it ?: 0L
    }.asLiveData()

    val showIndefiniteLoading = walletRepo.isLoading.asLiveData()
    val syncProgress = walletRepo.syncProgress.asLiveData()

    val transactions = walletRepo.transactions.asLiveData()

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionScreen(modifier: Modifier = Modifier) {

    val transactionsViewModel = viewModel<TransactionsViewModel>()
    val balance by transactionsViewModel.balance.observeAsState()
    val transactions by transactionsViewModel.transactions.observeAsState(listOf())
    val showIndefiniteLoading by transactionsViewModel.showIndefiniteLoading.observeAsState(false)
    val syncProgress by transactionsViewModel.syncProgress.observeAsState(null)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                    Text("[ANON]")
                },
                actions = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                stickyHeader {
                    AnimatedVisibility(
                        (showIndefiniteLoading || syncProgress != null),
                        modifier = Modifier.animateContentSize()
                    ) {
                        if (syncProgress != null && syncProgress!!.left != 0L) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(
                                    vertical = 12.dp
                                )
                            ) {
                                LinearProgressIndicator(
                                    progress = {
                                        syncProgress!!.progress
                                    },
                                    trackColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.2f
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                )
                                Text(
                                    "${syncProgress?.left} blocks left",
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            if (showIndefiniteLoading)
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    trackColor = MaterialTheme.colorScheme.primary.copy(
                                        alpha = 0.2f
                                    ),
                                )
                        }

                    }
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
                            "${Formats.getDisplayAmount(balance ?: 0, 6)} XMR",
                            style = MaterialTheme.typography
                                .displaySmall,
                            modifier = Modifier.fillParentMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                items(transactions.size) {
                    TransactionItem(transactions[it])
                }
            }
        }
    }
}


@Composable
fun TransactionItem(tx: TransactionInfo) {
    val isIncoming = tx.direction == TransactionInfo.Direction.Direction_In
    Log.i("TAG", "TransactionItem: ${tx.direction}")
    val amount = if (isIncoming) tx.amount else tx.amount * -1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
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
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            if (isIncoming) AnonIcons.ArrowDownLeft else AnonIcons.ArrowUpRight,
            modifier = Modifier.size(28.dp),
            tint = if (isIncoming) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            contentDescription = ""
        )
        Text(Formats.getDisplayAmount(amount, 3), style = MaterialTheme.typography.titleLarge)
        Text(
            Formats.formatTransactionTime(tx.timestamp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
private fun TransactionScreenReview() {
    AnonNeroTheme {
        TransactionScreen()
    }
}