package io.anonero.ui

import android.content.SharedPreferences
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import io.anonero.AnonConfig
import io.anonero.R
import io.anonero.services.TorService
import io.anonero.util.WALLET_PREFERENCES
import io.anonero.util.WALLET_USE_TOR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun TorSplash(
    modifier: Modifier = Modifier,
    enableTor: Boolean,
    content: @Composable () -> Unit = {}
) {
    var walletExist by remember { mutableStateOf(AnonConfig.isWalletFileExist()) }
    var anonPrefs: SharedPreferences = koinInject<SharedPreferences>(named(WALLET_PREFERENCES))
    val torService: TorService = koinInject<TorService>()
    val socks by torService.socksFlow.asLiveData().observeAsState(null)
    val scope = rememberCoroutineScope()
    var isAppReady by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val progressAnim by animateFloatAsState(
        targetValue = if (socks != null) .98f else 0.4f, label = "progressAnim"
    )

    LaunchedEffect(key1 = walletExist, socks) {
        //if user disabled tor and uses manual proxy, skip tor splash
        if(enableTor.not()){
            return@LaunchedEffect
        }
        scope.launch(Dispatchers.IO) {
            walletExist = AnonConfig.getDefaultWalletFile(context).exists()
            //if user disabled tor and uses manual proxy, skip tor splash
            if (!anonPrefs.getBoolean(WALLET_USE_TOR, true)) {
                isAppReady = true
            } else {
                if ((socks != null)) scope.launch(Dispatchers.IO) {
                    isAppReady = true
                }
            }
        }
    }

    //no need to show tor splash if user disabled tor
    if (!enableTor) {
        return content();
    }
    AnimatedContent(targetState = isAppReady) {
        if (it) {
            content()
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_anon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp, vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(
                                horizontal = 16.dp, vertical = 16.dp
                            ),
                        strokeCap = StrokeCap.Round,
                        color = Color.White,
                        drawStopIndicator = {},
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "INITIALIZING TOR ...", color = Color.White
                    )
                }
            }
        }
    }
}
