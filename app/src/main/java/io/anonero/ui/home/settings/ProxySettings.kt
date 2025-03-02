package io.anonero.ui.home.settings

import AnonNeroTheme
import android.content.SharedPreferences
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.anonero.services.AnonWalletHandler
import io.anonero.services.TorService
import io.anonero.util.WALLET_USE_TOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.runningFold
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
    private val anonWalletHandler: AnonWalletHandler,
    private val torService: TorService,
    private val anonPrefs: SharedPreferences,
) : ViewModel() {

    private val addressValidationError = MutableLiveData<String?>()
    private val _proxyLoading = MutableLiveData(false)
    private val _proxyAddress = MutableLiveData<Pair<String, Int>?>()
    private val _useTor = MutableLiveData(false)
    val validationError: LiveData<String?> get() = addressValidationError
    val proxy: LiveData<Pair<String, Int>?> get() = _proxyAddress
    val loading: LiveData<Boolean> get() = _proxyLoading
    val useTor: LiveData<Boolean> get() = _useTor
    val logs: LiveData<List<String>> = torService.torLogs
        .runningFold<String, List<String>>(emptyList()) { accumulator, value ->
            accumulator + value
        }
        .asLiveData()


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
            if (it != null) {
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
        _useTor.postValue(anonPrefs.getBoolean(WALLET_USE_TOR, true))
    }

    fun enableTor(enable: Boolean) {
        val socks = torService.socks
        if (enable && socks != null) {
            _useTor.postValue(true)
            setProxy(socks.address.value, socks.port.value)
            anonPrefs.edit().putBoolean(WALLET_USE_TOR, true).apply()
        } else {
            disableProxy()
            _useTor.postValue(false)
            anonPrefs.edit().putBoolean(WALLET_USE_TOR, false).apply()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxySettings(onBackPress: () -> Unit = {}) {
    var proxyAddress by remember { mutableStateOf("") }
    var showLogs by remember { mutableStateOf(false) }
    val proxyViewModel = koinViewModel<ProxySettingsViewModel>()
    val validationError by proxyViewModel.validationError.observeAsState(null)
    val proxy by proxyViewModel.proxy.observeAsState(null)
    val loading by proxyViewModel.loading.observeAsState(false)
    val useTor by proxyViewModel.useTor.observeAsState(false)
    var proxyPort by remember { mutableStateOf<Int?>(null) }
    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)

    val sheetState = rememberModalBottomSheetState()

    val view = LocalView.current
    LaunchedEffect(proxy) {
        if (proxy != null) {
            proxyAddress = proxy?.first.toString()
            proxyPort = proxy?.second
        }
    }

    if (showLogs) {
        ModalBottomSheet(
            onDismissRequest = {
                showLogs = false
            },
            containerColor = Color.Black,
            sheetState = sheetState,
            contentColor = Color.White,
            contentWindowInsets = {
                WindowInsets(
                    left = 0.dp,
                    top = 0.dp,
                    right = 0.dp,
                    bottom = 0.dp
                )
            },
            shape = MaterialTheme.shapes.large,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = true,
            ),
        ) {
            TorLogs()
        }

    }

    Scaffold(
        modifier = Modifier.animateContentSize(),
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
                        IconButton(
                            onClick = {
                                showLogs = !showLogs
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null)
                        }
                    },
                    title = {
                        Text("Proxy")
                    },
                )
                Box(modifier = Modifier.height(4.dp)){
                    this@Column.AnimatedVisibility(visible = loading) {
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
        }
    ) {
        Column(
            modifier = Modifier.padding(it),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                modifier = Modifier
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
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
                        Text("Tor Proxy")
                    },
                    trailingContent = {
                        Switch(checked = useTor,
                            thumbContent = {
                                Text(if(useTor) "ON" else "OFF")
                            },

                            onCheckedChange = {
                                proxyViewModel.enableTor(!useTor)
                                view.performHapticFeedback(
                                    HapticFeedbackConstants.KEYBOARD_TAP
                                )
                            })
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Row(
                Modifier
                    .padding(
                        horizontal = 8.dp,
                    ),
            ) {
                OutlinedTextField(
                    value = proxyAddress,
                    enabled = !useTor,
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
                    enabled = !useTor,
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
            if (!useTor)
                OutlinedButton(
                    enabled = proxyAddress.isNotEmpty() && proxyPort != null,
                    onClick = {
                        if (proxy != null && proxy!!.first == proxyAddress && proxy!!.second == proxyPort) {
                            proxyViewModel.disableProxy()
                        } else {
                            if (proxyAddress.isNotEmpty() && proxyPort != null)
                                proxyViewModel.setProxy(
                                    proxyAddress, proxyPort!!
                                )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),

                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(if (proxy != null) "Disable Proxy" else "Set External Proxy")
                }
        }

    }
}

@Composable
fun TorLogs() {
    val listState = rememberLazyListState()
    val proxyViewModel = koinViewModel<ProxySettingsViewModel>()
    val logs by proxyViewModel.logs.observeAsState(initial = emptyList())

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(
                horizontal = 8.dp
            )
            .navigationBarsPadding()
            .fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        item {
            Text(
                "Tor Logs", textAlign = TextAlign.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 12.dp,
                    )
            )
        }
        items(logs.size) { index ->
            Text(
                logs[index],
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                maxLines = 30,
                lineHeight = 12.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 4.dp
                    )
                    .background(Color.Transparent),
            )
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