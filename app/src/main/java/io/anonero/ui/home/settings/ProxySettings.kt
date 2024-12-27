package io.anonero.ui.home.settings

import AnonNeroTheme
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.anonero.services.AnonWalletHandler
import io.anonero.ui.components.WalletProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.util.regex.Pattern

private const val TAG = "ProxySettings"


fun isNumericAddress(address: String): Boolean {
    val ipv4Pattern = Pattern.compile(
        "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
    )
    val ipv6Pattern = Pattern.compile(
        "^(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}$"
    )
    return ipv4Pattern.matcher(address).matches() || ipv6Pattern.matcher(address).matches()
}

class ProxySettingsViewModel(
    private val anonWalletHandler: AnonWalletHandler
) : ViewModel() {

    private val addressValidationError = MutableLiveData<String?>()
    private val _proxyLoading = MutableLiveData(false)
    private val _proxyAddress = MutableLiveData<Pair<String, Int>?>()
    val validationError: LiveData<String?> get() = addressValidationError
    val proxy: LiveData<Pair<String, Int>?> get() = _proxyAddress
    val loading: LiveData<Boolean> get() = _proxyLoading

    init {
        updateProxyState()
    }

    fun setProxy(proxy: String, port: Int) {
        _proxyLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            if (!isNumericAddress(proxy)) {
                addressValidationError.postValue("Invalid proxy address")
                return@launch
            }
            if (port > 65535) {
                addressValidationError.postValue("Invalid port")
                return@launch
            }
            anonWalletHandler.setProxy(proxy = proxy, port = port)
            updateProxyState()
        }.invokeOnCompletion {
            if(it!=null){
                Timber.tag(TAG).e(it)
            }
            _proxyLoading.postValue(false)
        }
    }

    fun disableProxy() {
        anonWalletHandler.setProxy(proxy = null, port = null)
        updateProxyState()
    }

    private fun updateProxyState() {
        _proxyAddress.postValue(anonWalletHandler.getProxy())
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxySettings(onBackPress: () -> Unit = {}) {
    var proxyAddress by remember { mutableStateOf("") }
    val proxyViewModel = koinViewModel<ProxySettingsViewModel>()
    val validationError by proxyViewModel.validationError.observeAsState(null)
    val proxy by proxyViewModel.proxy.observeAsState(null)
    val loading by proxyViewModel.loading.observeAsState(false)
    var proxyPort by remember { mutableStateOf<Int?>(null) }
    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)

    LaunchedEffect(proxy) {
        if (proxy != null) {
            proxyAddress = proxy?.first.toString();
            proxyPort = proxy?.second;
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPress
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        if (proxy == null)
                            TextButton(
                                onClick = {
                                    proxyViewModel.setProxy(
                                        proxy = proxyAddress,
                                        port = proxyPort ?: -1
                                    )
                                }
                            ) { Text("Set") }

                    },
                    title = {
                        Text("Proxy")
                    },
                )
                if(loading){
                    LinearProgressIndicator(
                        trackColor = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.2f
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    ) {
        if (proxy != null) {
            Card(
                modifier = Modifier
                    .padding(it)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(
                        alpha = .2f
                    ),
                )
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                    headlineContent = {
                        Text("Active Proxy")
                    },
                    supportingContent = {
                        Text("${proxy?.first}:${proxy?.second}")
                    },
                    trailingContent = {
                        TextButton(onClick = {
                            proxyViewModel.disableProxy()
                        }) {
                            Text("Disable")
                        }
                    }
                )
            }
        } else {
            Row(
                Modifier
                    .padding(it)
                    .padding(
                        horizontal = 8.dp,
                        vertical = 12.dp
                    ),
            ) {
                OutlinedTextField(
                    value = proxyAddress,
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null)
                            Text(validationError.toString())
                    },
                    shape = MaterialTheme.shapes.medium,
                    onValueChange = { text ->
                        proxyAddress = text
                        try {
                            Uri.parse(text)
                        } catch (_: Exception) {

                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                    ),
                    placeholder = {
                        Text(
                            text = "proxy address",
                            color = labelColor
                        )
                    },
                    modifier = Modifier
                        .weight(2.5f)
                        .padding(
                            horizontal = 8.dp
                        )
                )
                OutlinedTextField(
                    value = proxyPort?.toString() ?: "",
                    shape = MaterialTheme.shapes.medium,
                    onValueChange = { port ->
                        proxyPort = port.toInt()
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    placeholder = {
                        Text(
                            text = "port",
                            color = labelColor
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }

    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun ProxySettingsPreview() {
    AnonNeroTheme {
        ProxySettings()
    }
}