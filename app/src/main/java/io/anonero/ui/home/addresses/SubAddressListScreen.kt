package io.anonero.ui.home.addresses

import AnonNeroTheme
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.services.WalletState
import io.anonero.util.Formats
import org.koin.java.KoinJavaComponent.inject


class SubAddressListViewModel : ViewModel() {
    private val walletState: WalletState by inject(WalletState::class.java)

    val subAddresses = walletState.subAddresses.asLiveData()

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubAddressesScreen(
    modifier: Modifier = Modifier,
    onBackPress: () -> Unit = {}
) {
    BackHandler {
        onBackPress()
    }
    val vm = viewModel<SubAddressListViewModel>()
    val addresses by vm.subAddresses.observeAsState(listOf())
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
                title = {},
                actions = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(addresses.size) {
                val address = addresses[it]
                ListItem(
                    modifier = Modifier.padding(
                        horizontal = 4.dp,
                        vertical = 6.dp
                    ),
                    headlineContent = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                address.displayLabel, color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                Formats.getDisplayAmount(address.totalAmount),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    supportingContent = {
                        Text(
                            address.address,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Justify
                        )
                    },
                )
            }
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
private fun SubAddressScreenPrev() {
    AnonNeroTheme {
        SubAddressesScreen()
    }
}