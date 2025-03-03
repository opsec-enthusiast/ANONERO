package io.anonero.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import io.anonero.services.WalletState
import io.anonero.util.Formats
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

@Composable
fun WalletProgressIndicator(modifier: Modifier = Modifier) {
    val walletState: WalletState by inject(WalletState::class.java)

    val showIndefiniteLoading by walletState.isLoading.asLiveData().observeAsState(false)
    val syncProgress by walletState.syncProgress.asLiveData().observeAsState(null)
    AnimatedVisibility(
        (showIndefiniteLoading || syncProgress != null),
        modifier = modifier.animateContentSize()
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