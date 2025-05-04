package io.anonero.ui.home.spend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sparrowwallet.hummingbird.ResultType
import com.sparrowwallet.hummingbird.URDecoder
import io.anonero.AnonConfig
import io.anonero.model.AnonUrRegistryTypes
import io.anonero.model.UnsignedTransaction
import io.anonero.model.WalletManager
import io.anonero.ui.components.scanner.QRScannerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


private const val TAG = "SpendScannerViewModel"

class SpendScannerViewModel : ViewModel() {

    fun processUrCode(
        urResult: URDecoder.Result,
        onSuccess: (path: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (urResult.type == ResultType.SUCCESS) {
                var destinationFile: File? = null
                val ur = urResult.ur
                when (ur.type) {
                    AnonUrRegistryTypes.XMR_TX_UNSIGNED.type -> {
                        destinationFile =
                            File(AnonConfig.context?.cacheDir, AnonConfig.IMPORT_UNSIGNED_TX_FILE)
                    }

                    AnonUrRegistryTypes.XMR_TX_SIGNED.type -> {
                        destinationFile =
                            File(AnonConfig.context?.cacheDir, AnonConfig.IMPORT_SIGNED_TX_FILE)
                    }

                    AnonUrRegistryTypes.XMR_OUTPUT.type -> {
                        destinationFile =
                            File(AnonConfig.context?.cacheDir, AnonConfig.IMPORT_OUTPUT_FILE)
                    }

                    AnonUrRegistryTypes.XMR_KEY_IMAGE.type -> {
                        destinationFile =
                            File(AnonConfig.context?.cacheDir, AnonConfig.IMPORT_KEY_IMAGE_FILE)
                    }

                    else -> {
                        Timber.tag(TAG).e("Unknown UR type")
                    }
                }
                destinationFile?.writeBytes(ur.toBytes())
                if (destinationFile != null) {
                    onSuccess(destinationFile.path)
                }
            }
        }
    }
}


class SpendScannerController {
    var showScannerCallBack: (Boolean) -> Unit = {}
    var showQRExchangeCallBack: (SpendQRExchangeParam) -> Unit = {}

    fun showQRExchange(spendQRExchangeParam: SpendQRExchangeParam) {
        showQRExchangeCallBack.invoke(spendQRExchangeParam)
    }

    fun showScanner() {
        showScannerCallBack.invoke(true)
    }

    fun hideScanner() {
        showScannerCallBack.invoke(false)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendScanner(
    spendScannerController: SpendScannerController,
    modifier: Modifier = Modifier,
    onKeyImagesImported: (Boolean) -> Unit,
    onUnsignedTransactionImported: (String) -> Unit,
    onSignedTransactionImported: (String) -> Unit,
    onOutputsImported: (String) -> Unit,
    onError: (String) -> Unit,
    onQRCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val sendViewModel = viewModel<SpendScannerViewModel>()
    var qrScannerParam by remember { mutableStateOf<SpendQRExchangeParam?>(null) }
    var showScanner by remember { mutableStateOf<Boolean>(false) }
    val scope = rememberCoroutineScope()
    var currentImportState by remember { mutableStateOf<AnonUrRegistryTypes?>(null) }


    spendScannerController.showScannerCallBack = {
        showScanner = it
    }
    spendScannerController.showQRExchangeCallBack = {
        qrScannerParam = it
    }

    Timber.tag(TAG).d("qrScannerParam: $qrScannerParam $showScanner")
    QRExchangeDialog(
        show = qrScannerParam != null,
        params = qrScannerParam,
        onCtaCalled = {
            if (qrScannerParam != null) {
                when (qrScannerParam!!.exportType) {
                    ExportType.IMAGE -> {
                        spendScannerController.showScanner()
                    }

                    ExportType.OUTPUT -> {
                        showScanner = true
                    }

                    ExportType.SIGNED_TX -> {
                        showScanner = true
                    }

                    ExportType.UN_SIGNED_TX -> {
                        showScanner = true
                    }
                }
                qrScannerParam = null

            }
            onDismiss.invoke()
        },
        onDismiss = {
            qrScannerParam = null
        },
    )



    if (currentImportState != null) {
        BasicAlertDialog(
            onDismissRequest = {

            },
            modifier = Modifier
                .border(
                    1.dp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(
                        alpha = .2f
                    ),
                    shape = MaterialTheme.shapes.medium,
                )
                .background(MaterialTheme.colorScheme.secondary),
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            ),
        ) {
            Column(
                modifier = Modifier
                    .size(148.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = when (currentImportState) {
                        AnonUrRegistryTypes.XMR_OUTPUT -> "Importing outputs"
                        AnonUrRegistryTypes.XMR_KEY_IMAGE -> "Importing key images"
                        AnonUrRegistryTypes.XMR_TX_SIGNED -> "Importing unsigned transaction"
                        AnonUrRegistryTypes.XMR_TX_UNSIGNED -> "Importing unsigned transaction"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.size(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(34.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }

    QRScannerDialog(
        show = showScanner,
        onQRCodeScanned = {
            qrScannerParam = null
            onQRCodeScanned.invoke(it)
        },
        onUrRusult = {
            onDismiss.invoke()
            sendViewModel.processUrCode(
                it,
                onSuccess = { filePath ->
                    showScanner = false
                    val wallet = WalletManager.instance?.wallet;
                    if (wallet == null) {
                        return@processUrCode
                    }
                    when (AnonUrRegistryTypes.fromUrTag(it.ur)) {
                        AnonUrRegistryTypes.XMR_OUTPUT -> {
                            scope.launch {
                                currentImportState = AnonUrRegistryTypes.XMR_OUTPUT
                                try {
                                    val status = wallet.importOutputs(filePath);
                                    if (status?.lowercase() == "imported") {
                                        qrScannerParam = SpendQRExchangeParam(
                                            exportType = ExportType.IMAGE,
                                            title = "KEY IMAGES",
                                            ctaText = "Scan unsigned transaction",
                                        )
                                        onOutputsImported.invoke(filePath)
                                    } else {
                                        onError.invoke("Failed to import outputs")
                                    }
                                } finally {
                                    currentImportState = null
                                }
                            }
                        }

                        AnonUrRegistryTypes.XMR_KEY_IMAGE -> {
                            qrScannerParam = null;
                            scope.launch {
                                currentImportState = AnonUrRegistryTypes.XMR_KEY_IMAGE
                                try {
                                    val status = wallet.importKeyImages(filePath);
                                    if (status) {
                                        onKeyImagesImported.invoke(true)
                                    } else {
                                        onError.invoke("Failed to import key images")
                                    }
                                } finally {
                                    currentImportState = null
                                }
                            }

                        }

                        AnonUrRegistryTypes.XMR_TX_UNSIGNED -> {
                            currentImportState = AnonUrRegistryTypes.XMR_TX_UNSIGNED
                            qrScannerParam = null;
                            scope.launch {
                                try {
                                    val unsignedTransaction =
                                        wallet.loadUnsignedTransaction(filePath)
                                    if (unsignedTransaction.status == UnsignedTransaction.Status.Status_Ok) {
                                        onUnsignedTransactionImported.invoke(filePath)
                                    } else {
                                        onError.invoke("Failed to import unsigned transaction")
                                    }
                                } finally {
                                    currentImportState = null
                                }
                            }
                        }

                        AnonUrRegistryTypes.XMR_TX_SIGNED -> {
                            currentImportState = null;
                            qrScannerParam = null;
                            scope.launch {
                                onSignedTransactionImported.invoke(filePath)
                            }
                        }

                        null -> {
                            Timber.tag(TAG).e("Unknown UR type")
                            onError.invoke("Unknown UR type")
                        }
                    }
                }
            )
        },
        onDismiss = {
            qrScannerParam = null
            onDismiss.invoke()
        }
    )
}