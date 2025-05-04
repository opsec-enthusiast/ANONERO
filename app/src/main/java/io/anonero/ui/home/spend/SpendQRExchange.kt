package io.anonero.ui.home.spend

import AnonNeroTheme
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sparrowwallet.hummingbird.UR
import io.anonero.AnonConfig
import io.anonero.model.AnonUrRegistryTypes
import io.anonero.model.WalletManager
import io.anonero.ui.components.AnimatedQrCode
import io.anonero.ui.home.graph.routes.ReviewTransactionRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File


@Serializable
data class SpendQRExchangeParam(
    val exportType: ExportType,
    val title: String,
    val ctaText: String,
)

enum class ExportType {
    OUTPUT,
    IMAGE,
    SIGNED_TX,
    UN_SIGNED_TX,
}

class QRExchangeScreenViewModel : ViewModel() {

    private val _ur = MutableLiveData<UR?>()
    val ur: LiveData<UR?> = _ur

    fun loadKeyImages(exportType: ExportType) {
        viewModelScope.launch(Dispatchers.IO) {
            when (exportType) {
                ExportType.OUTPUT -> {
                    val file = File(AnonConfig.context?.cacheDir, AnonConfig.EXPORT_OUTPUT_FILE)
                    file.createNewFile()
                    val eo = WalletManager.instance?.wallet?.exportOutputs(file.absolutePath, true)
                    if (eo == true) {
                        setUR(file, AnonUrRegistryTypes.XMR_OUTPUT.type)
                    }
                }

                ExportType.IMAGE -> {
                    val file = File(AnonConfig.context?.cacheDir, AnonConfig.EXPORT_KEY_IMAGE_FILE)
                    file.createNewFile()
                    val eo =
                        WalletManager.instance?.wallet?.exportKeyImages(file.absolutePath, true)
                    if (eo == true) {
                        setUR(file, AnonUrRegistryTypes.XMR_KEY_IMAGE.type)
                    }
                }

                ExportType.SIGNED_TX -> {
                    val file = File(AnonConfig.context?.cacheDir, AnonConfig.EXPORT_SIGNED_TX_FILE)
                    if (file.exists()) {
                        setUR(file, AnonUrRegistryTypes.XMR_TX_SIGNED.type)
                    }
                }

                ExportType.UN_SIGNED_TX -> {
                    val file =
                        File(AnonConfig.context?.cacheDir, AnonConfig.EXPORT_UNSIGNED_TX_FILE)
                    if (file.exists()) {
                        setUR(file, AnonUrRegistryTypes.XMR_TX_UNSIGNED.type)
                    }
                }
            }

        }
    }

    fun setUR(file: File, type: String) {
        val content = file.readBytes()
        val ur = UR.fromBytes(type, content)
        _ur.postValue(ur)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRExchangeScreen(
    params: SpendQRExchangeParam,
    onCtaCalled: () -> Unit,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {

    val viewModel = viewModel<QRExchangeScreenViewModel>()
    val ur by viewModel.ur.observeAsState(null)


    val titleStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20.sp
    )

    LaunchedEffect(true) {
        viewModel.loadKeyImages(params.exportType)
    }

    BackHandler {
        onBackPressed()
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
                            onBackPressed.invoke()
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
            Column(
                modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = params.title,
                        style = titleStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    if (ur != null) {
                        AnimatedQrCode(
                            Ur = ur!!,
                            modifier = Modifier
                                .size(300.dp)
                                .align(Alignment.CenterHorizontally)
                                .padding(8.dp),
                            fps = 15,
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(
                        onClick = {

                        },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text("Export as file")
                    }
                }
                Button(
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        1.dp,
                        color = MaterialTheme.colorScheme.onSecondary
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                    contentPadding = PaddingValues(
                        all = 12.dp
                    ),
                    modifier = Modifier.fillMaxWidth(0.85f),
                    onClick = {
                        onCtaCalled.invoke()
                    }) {
                    Text(
                        params.ctaText, style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondary.copy(
                                alpha = 0.8f
                            )
                        )
                    )
                }
            }

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRExchangeDialog(
    modifier: Modifier = Modifier,
    params: SpendQRExchangeParam?,
    show: Boolean,
    onDismiss: () -> Unit,
    onCtaCalled: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.Transparent,
            sheetState = sheetState,
            scrimColor = MaterialTheme.colorScheme.background,
            contentColor = Color.Transparent,
            contentWindowInsets = {
                WindowInsets(
                    left = 0.dp,
                    top = 0.dp,
                    right = 0.dp,
                    bottom = 0.dp
                )
            },
            shape = MaterialTheme.shapes.large,
            dragHandle = {
                Box(modifier = Modifier.height(0.dp))
            },
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = true,
            )
        ) {

            Box(Modifier.padding(0.dp)) {
                if (params != null)
                    QRExchangeScreen(
                        params = params,
                        onBackPressed = onDismiss,
                        onCtaCalled = onCtaCalled,
                        modifier = Modifier.fillMaxSize()
                    )
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