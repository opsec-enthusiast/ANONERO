package io.anonero.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import io.anonero.services.WalletState
import io.anonero.util.Formats
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletProgressIndicator(modifier: Modifier = Modifier, refreshIndicatorProgress: Float = 0.0f) {
    val walletState: WalletState by inject(WalletState::class.java)

    val showIndefiniteLoading by walletState.isLoading.asLiveData().observeAsState(false)
    val syncProgress by walletState.syncProgress.asLiveData().observeAsState(null)
    AnimatedVisibility(
        (showIndefiniteLoading || syncProgress != null || refreshIndicatorProgress != 0.0f),
        modifier = modifier
            .animateContentSize()
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
                if (syncProgress!!.left > 50) {
                    Text(
                        "${
                            Formats.convertNumber(
                                syncProgress?.left!!,
                                Locale.getDefault()
                            )
                        } blocks left",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                }
            }
        } else {
            val showRefreshState =
                refreshIndicatorProgress.toFloat() != 0.0f || refreshIndicatorProgress.toFloat() != 0.0f;
            if (showIndefiniteLoading )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    trackColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    ),
                )
            if(showRefreshState && !showIndefiniteLoading){
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    progress = {
                        refreshIndicatorProgress.toFloat()
                    },
                    trackColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    ),
                )
            }
        }

    }

}