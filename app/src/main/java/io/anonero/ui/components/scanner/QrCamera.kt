package io.anonero.ui.components.scanner

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import io.anonero.icons.AnonIcons
import java.util.concurrent.Executors

private const val TAG = "QrCamera"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    onDismiss: () -> Unit,
    onQRCodeScanned: (String) -> Unit,
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
                QRScanner(
                    onDismiss = onDismiss,
                    onQRCodeScanned = {
                        onDismiss.invoke()
                        onQRCodeScanned.invoke(it)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScanner(
    onQRCodeScanned: (String) -> Unit, modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var isScanned by remember { mutableStateOf(false) }
    val widthInPx: Float
    val heightInPx: Float
    val radiusInPx: Float

    with(LocalDensity.current) {
        widthInPx = 240.dp.toPx()
        heightInPx = 240.dp.toPx()
        radiusInPx = 16.dp.toPx()
    }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(true) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when (val status = cameraPermissionState.status) {
            is PermissionStatus.Denied -> {
                if (!status.shouldShowRationale) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            AnonIcons.ScanQrCode,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(84.dp)
                                .padding(
                                    bottom = 8.dp
                                )
                        )
                        Text(

                            "Allow camera access to scan QR code",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 16.dp
                            )
                        )
                        Button(
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,

                                ),
                            onClick = {
                                cameraPermissionState.launchPermissionRequest()
                            }) {
                            Text("Allow")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            AnonIcons.ScanQrCode,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(84.dp)
                                .padding(
                                    bottom = 8.dp
                                )
                        )
                        Text(
                            "Camera permission is required to scan QR codes. Please enable it in app settings.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 16.dp
                            )
                        )
                        Button(
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                            ),
                            onClick = {
                                // Open app settings to manually enable the permission
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                context.startActivity(intent)
                            }) {
                            Text("Open App Settings")
                        }
                    }
                }
            }

            PermissionStatus.Granted -> {
                Box {
                    AndroidView(modifier = modifier.fillMaxSize(),
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }

                            // Set up CameraX
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                cameraProvider = cameraProviderFuture.get()

                                // Preview use case
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                // ImageAnalysis use case
                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(
                                            cameraExecutor,
                                            QrCodeAnalyzerBoofcv(onQrCodesDetected = {
                                                if (!isScanned)
                                                    onQRCodeScanned.invoke(it.text)
                                                isScanned = true
                                            })
                                        )
                                    }

                                try {
                                    // Unbind previous use cases before rebinding
                                    cameraProvider?.unbindAll()
                                    cameraProvider?.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalysis
                                    )
                                } catch (exc: Exception) {
                                    exc.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        update = { })
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()

                    ) {
                         drawRect(
                            color = Color.Black.copy(alpha = 0.4f))
                    }
                    Canvas(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .border(1.dp, Color.White, RoundedCornerShape(16.dp))
                            .onGloballyPositioned { layoutCoordinates ->
                                canvasSize = layoutCoordinates.size
                            }
                    ) {
                        if (canvasSize != IntSize.Zero) {
                            val offset = Offset(
                                x = (canvasSize.width - widthInPx) / 2,
                                y = (canvasSize.height - heightInPx) / 2
                            )

                            val cutoutRect = Rect(offset, Size(widthInPx, heightInPx))
                            drawRoundRect(
                                topLeft = Offset(
                                    (canvasSize.width - cutoutRect.width) / 2,
                                    (canvasSize.height - cutoutRect.height) / 2
                                ),
                                size = cutoutRect.size,
                                cornerRadius = CornerRadius(radiusInPx, radiusInPx),
                                color = Color.Transparent,
                                blendMode = BlendMode.Clear
                            )
                        }
                    }
                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        shape = MaterialTheme.shapes.extraSmall.copy(
                            all = CornerSize(12.dp)
                        ),
                        border = BorderStroke(
                            1.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        onClick = {
                            onDismiss.invoke()
                        }) {
                        Row {
                            Icon(Icons.Default.Close, contentDescription = "")
                            Spacer(Modifier.padding(8.dp))
                            Text("Close")
                            Spacer(Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }

}
