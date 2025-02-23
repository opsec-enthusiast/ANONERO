package io.anonero.ui.onboard

import AnonNeroTheme
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.anonero.AnonConfig
import io.anonero.R
import io.anonero.model.node.Node
import io.anonero.model.node.NodeFields
import io.anonero.store.NodesRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.compose.koinInject


@Composable
fun SetupNodeComposable(
    onBackPressed: () -> Unit = {},
    oNextPressed: () -> Unit = {},
) {
    var rpcHost by remember { mutableStateOf("") }
    var rpcUsername by remember { mutableStateOf("") }
    var rpcPassPhrase by remember { mutableStateOf("") }
    val localContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val nodesRepository = koinInject<NodesRepository>()
    val nodes by nodesRepository.nodesFlow.collectAsState(listOf())

    LaunchedEffect(true) {
        val prefs = localContext.getSharedPreferences(AnonConfig.PREFS, Context.MODE_PRIVATE)
        rpcHost = prefs.getString(NodeFields.RPC_HOST.value, "") ?: ""
        rpcUsername = prefs.getString(NodeFields.RPC_USERNAME.value, "") ?: ""
        rpcPassPhrase = prefs.getString(NodeFields.RPC_PASSWORD.value, "") ?: ""
        val rpcPort = prefs.getString(NodeFields.RPC_PORT.value, "")
        if ((rpcPort ?: "").isNotEmpty()) {
            rpcHost = "${rpcHost}:${rpcPort}"
        }
    }


    val labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    modifier = Modifier
                        .size(24.dp),
                    onClick = {
                        onBackPressed()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_anon),
                    contentDescription = "Anon nero icon",
                    modifier = Modifier
                        .size(120.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "NODE CONNECTION",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .weight(.8f)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ListItem(
                    headlineContent = {
                        Text(
                            text = "NODE",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    supportingContent = {
                        OutlinedTextField(
                            value = rpcHost,
                            shape = MaterialTheme.shapes.medium,
                            onValueChange = {
                                rpcHost = it
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                            ),
                            placeholder = {
                                Text(
                                    text = "http://address.onion:port",
                                    color = labelColor
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusable()
                        )
                    },
                )

                ListItem(
                    headlineContent = {
                        Text(
                            text = "Username",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    supportingContent = {
                        OutlinedTextField(
                            value = rpcUsername,
                            shape = MaterialTheme.shapes.medium,
                            placeholder = {
                                Text(
                                    text = "(Optional)",
                                    color = labelColor
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                            ),
                            onValueChange = {
                                rpcUsername = it
                            },

                            )
                    },
                )
                ListItem(
                    headlineContent = {
                        Text(
                            text = "PASSWORD",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    },
                    supportingContent = {
                        OutlinedTextField(
                            value = rpcPassPhrase,
                            shape = MaterialTheme.shapes.medium,
                            onValueChange = {
                                rpcPassPhrase = it
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                            ),
                            placeholder = {
                                Text(
                                    text = "(Optional)",
                                    color = labelColor
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                )
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val prefs = localContext.getSharedPreferences(
                            AnonConfig.PREFS,
                            Context.MODE_PRIVATE
                        )
                        val editor = prefs.edit()

                        val rpcPort = Node.defaultRpcPort.toString()
                        try {
                            val cleanURL = rpcHost.trim().lowercase()
                            val urlForParsing = if (cleanURL.startsWith("http")) {
                                cleanURL
                            } else {
                                if (cleanURL.startsWith("https")) {
                                    cleanURL
                                } else {
                                    "http://$cleanURL"
                                }
                            }
                            val validatedUrl = Uri.parse(urlForParsing)
                            editor.putString(NodeFields.RPC_HOST.value, validatedUrl.host)
                            editor.putString(NodeFields.RPC_PORT.value, rpcPort)
                            editor.putString(NodeFields.RPC_USERNAME.value, rpcUsername)
                            editor.putString(NodeFields.RPC_PASSWORD.value, rpcPassPhrase)
                            editor.apply()
                            val nodeObj = JSONObject()
                                .apply {
                                    put(NodeFields.RPC_HOST.value, validatedUrl.host)
                                    put(NodeFields.RPC_PORT.value, rpcPort)
                                    put(NodeFields.RPC_USERNAME.value, rpcUsername)
                                    put(NodeFields.RPC_PASSWORD.value, rpcPassPhrase)
                                    put(
                                        NodeFields.RPC_NETWORK.value,
                                        AnonConfig.getNetworkType().toString()
                                    )
                                    put(NodeFields.NODE_NAME.value, validatedUrl.host)
                                }
                            Node.fromJson(nodeObj)?.let {
                                nodesRepository.addItem(it)
                            }
                            oNextPressed()
                        } catch (_: Exception) {
                        }

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp
                    ),

                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(12.dp)
            ) {
                Text("NEXT")
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
private fun SetupNodeComposablePreview() {
    AnonNeroTheme {
        SetupNodeComposable()
    }
}