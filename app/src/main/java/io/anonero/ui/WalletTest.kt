package io.anonero.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.anonero.AnonConfig
import io.anonero.model.WalletManager
import io.anonero.ui.theme.AnonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WalletTestViewModel : ViewModel() {

    var isWalletCreated: MutableLiveData<Boolean> = MutableLiveData(false)
    var walletOpened: MutableLiveData<Boolean> = MutableLiveData(false)
    var inProgress = MutableLiveData(false)
    var seed = MutableLiveData(arrayOf<String>())
    var seedLegacy = MutableLiveData(arrayOf<String>())
    var address = MutableLiveData("")

    fun checkWallet(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val walletFile = AnonConfig.getDefaultWalletFile(context)
                isWalletCreated.postValue(walletFile.exists())
            }
        }
    }

    fun createWallet(context: Context, passPhrase: String, pin: String) {
        viewModelScope.launch {
            inProgress.postValue(true);
            withContext(Dispatchers.IO) {
                context.applicationContext.filesDir.deleteRecursively()
                val walletFile = AnonConfig.getDefaultWalletFile(context)
                val anonWallet = WalletManager.instance?.createWallet(
                    walletFile,
                    pin,
                    passPhrase,
                    "English",
                    1,
                )
                Log.d(
                    "AnonMain",
                    "Creating wallet Seed        : ${anonWallet?.getSeed(passPhrase)}"
                )
                Log.d(
                    "AnonMain",
                    "Creating wallet Legacy Seed : ${anonWallet?.getLegacySeed(passPhrase)}"
                )
                anonWallet?.store()
                inProgress.postValue(false);
                seed.postValue((anonWallet?.getSeed(passPhrase) ?: "").split(" ").toTypedArray())
                seedLegacy.postValue(
                    (anonWallet?.getLegacySeed(passPhrase) ?: "").split(" ").toTypedArray()
                )
                anonWallet?.status?.let {
                    isWalletCreated.postValue(it.isOk)
                    walletOpened.postValue(it.isOk)
                    address.postValue(anonWallet.getAddress(0))
                }
            }
        }
    }

    fun openWallet(localContext: Context, passphrase: String, pin: String) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                inProgress.postValue(true);
                val walletFile = AnonConfig.getDefaultWalletFile(localContext)
                val anonWallet = WalletManager.instance?.openWallet(
                    walletFile.path,
                    pin,
                );
                Log.i("TAG", "openWallet: ${anonWallet?.status}")
                if(anonWallet?.status?.isOk != true){
                    withContext(Dispatchers.Main){
                        Toast.makeText(localContext, "Failed to open wallet: ${anonWallet?.status?.errorString ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                    inProgress.postValue(false)
                    return@withContext
                }
                Log.d(
                    "AnonMain",
                    "Open wallet Seed        : ${anonWallet?.getSeed(passphrase)}"
                )
                Log.d(
                    "AnonMain",
                    "Open wallet Legacy Seed : ${anonWallet?.getLegacySeed(passphrase)}"
                )

                anonWallet.store()
                seed.postValue((anonWallet.getSeed(passphrase) ?: "").split(" ").toTypedArray())
                seedLegacy.postValue(
                    (anonWallet.getLegacySeed(passphrase) ?: "").split(" ").toTypedArray()
                )
                anonWallet.status.let {
                    isWalletCreated.postValue(it.isOk)
                    walletOpened.postValue(it.isOk)
                    address.postValue(anonWallet.getAddress(0))
                }
                inProgress.postValue(false);
            }
        }

    }

    fun removeWallet(localContext: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AnonConfig.getDefaultWalletDir(localContext).deleteRecursively()
                isWalletCreated.postValue(false)
                walletOpened.postValue(false)
                seed.postValue(arrayOf())
                seedLegacy.postValue(arrayOf())
                address.postValue("")
            }
        }
    }

}

class WalletTest : ComponentActivity() {
    private val walletViewModel: WalletTestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnonTheme {
                TestWallet()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        walletViewModel.checkWallet(this.applicationContext)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestWallet() {
    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current
    val vm: WalletTestViewModel = viewModel();
    val walletCreated by vm.isWalletCreated.observeAsState(false)
    val walletOpened by vm.walletOpened.observeAsState(false)
    val inProgress by vm.inProgress.observeAsState(false)
    var openCreateDialog by remember { mutableStateOf(false) }
    var doCreateWallet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            openCreateDialog -> {
                GenerateWallet(
                    openWallet = openCreateDialog,
                    onClose = { openCreateDialog = false },
                    onConfirmed = { pin, passphrase ->
                        if (doCreateWallet) {
                            vm.createWallet(localContext, passphrase, pin)
                            openCreateDialog = false
                        } else {
                            vm.openWallet(localContext, passphrase, pin)
                            openCreateDialog = false
                        }
                    })
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Anonero")
                        }
                    },
                )
            },
        ) {
            Surface(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp)
            ) {
                if (inProgress) {
                    return@Surface Box(modifier = Modifier.fillMaxWidth(), content = {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(34.dp)
                                .align(Alignment.Center),
                            strokeWidth = 2.dp
                        )
                    })
                }
                if (walletCreated) {
                    if (!walletOpened) {
                        return@Surface   Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button(onClick = {
                                openCreateDialog = true
                                doCreateWallet = false
                            }, modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .align(Alignment.Center))
                            {
                                Text("Open Wallet")
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Seed",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        "${vm.seed.value?.joinToString(" ")}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                            )
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Address",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        "${vm.address.value}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                            )

                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button(
                                onClick = {
                                    vm.removeWallet(localContext)
                                }, modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .padding(
                                        8.dp
                                    )
                                    .align(Alignment.Center),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red,
                                    contentColor = Color.White)
                            )
                            {
                                Text("Remove Wallet")
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Button(onClick = {
                            openCreateDialog = true
                            doCreateWallet = true
                        }, modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .align(Alignment.Center))
                        {
                            Text("Create Wallet")
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun GenerateWallet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onConfirmed: (pin: String, passphrase: String) -> Unit,
    openWallet: Boolean
) {
    var passphrase by remember { mutableStateOf("passphrase") }
    var pin by remember { mutableStateOf("12345") }
    val vm: WalletTestViewModel = viewModel();
    val context = LocalContext.current;
    return AlertDialog(
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                content = {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true,
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text("Passphrase") }
                    )
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("pin") }
                    )
                }
            )
        },
        onDismissRequest = {
            onClose.invoke()
        },
        icon = { Icons.Outlined.Create },
        title = { Text(if (openWallet) "Open Wallet" else "Create Wallet") },
        dismissButton = {
            TextButton(
                onClick = {
                    onClose.invoke()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmed.invoke(pin, passphrase)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (openWallet) "Open Wallet" else "Create Wallet")
            }
        },
        shape = MaterialTheme.shapes.medium
    )

}

@Preview(heightDp = 640, showBackground = true)
@Composable
private fun AnonMainPre() {
    AnonTheme {
        TestWallet()
    }
}