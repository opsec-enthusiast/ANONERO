//package io.anonero.ui
//
//import AnonNeroTheme
//import android.Manifest
//import android.content.Context
//import android.content.ContextWrapper
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.compose.animation.animateContentSize
//import androidx.compose.animation.core.LinearOutSlowInEasing
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.gestures.Orientation
//import androidx.compose.foundation.gestures.scrollable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
//import androidx.compose.material.icons.outlined.Create
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.ListItemDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TextField
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.platform.LocalClipboardManager
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import io.anonero.AnonConfig
//import io.anonero.BuildConfig
//import io.anonero.R
//import io.anonero.model.node.Node
//import io.anonero.model.PendingTransaction
//import io.anonero.model.Wallet
//import io.anonero.model.WalletManager
//import io.anonero.services.AnonNeroService
//import io.anonero.services.MoneroHandlerThread
//import io.anonero.util.Formats
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.json.JSONObject
//import timber.log.Timber
//import java.util.Locale
//
//
//enum class BroadcastStatus(val status: String?) {
//    Staging(status = null),
//    Loading(status = null),
//    Success(status = null),
//    Error(status = "")
//}
//
//fun Context.getActivity(): ComponentActivity? = when (this) {
//    is ComponentActivity -> this
//    is ContextWrapper -> baseContext.getActivity()
//    else -> null
//}
//
//const val PREFS = "anonPref"
//
// val walletSyncFlow : MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
//
//@Suppress("UNUSED_EXPRESSION")
//class WalletTestViewModel : ViewModel(), MoneroHandlerThread.Listener {
//
//
//    var isWalletCreated: MutableLiveData<Boolean> = MutableLiveData(false)
//    var walletOpened: MutableLiveData<Boolean> = MutableLiveData(false)
//    var inProgress = MutableLiveData(false)
//    var closingWallet = MutableLiveData(false)
//    var seed = MutableLiveData(arrayOf<String>())
//    var seedLegacy = MutableLiveData(arrayOf<String>())
//    var broadcastStatus = MutableLiveData(BroadcastStatus.Staging)
//    var subaddress = MutableLiveData(arrayOf<String?>())
//    var balance = MutableLiveData(0L)
//    var coins = MutableLiveData(0)
//    var walletDeubug = MutableLiveData("")
//    var address = MutableLiveData("")
//    var currentBlockProgress = MutableLiveData(1L)
//    var daemonBlockChainTargetHeightLive = MutableLiveData(1L)
//    var handler: MoneroHandlerThread? = null
//    var index = 1
//
//    fun checkWallet(context: Context) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                val walletFile = AnonConfig.getDefaultWalletFile(context)
//                isWalletCreated.postValue(walletFile.exists())
//            }
//        }
//    }
//
//    fun createWallet(context: Context, passPhrase: String, pin: String) {
//        viewModelScope.launch {
//            inProgress.postValue(true)
//            withContext(Dispatchers.IO) {
//                context.applicationContext.filesDir.deleteRecursively()
//                val walletFile = AnonConfig.getDefaultWalletFile(context)
//                val anonWallet = WalletManager.instance?.createWallet(
//                    walletFile,
//                    pin,
//                    passPhrase,
//                    "English",
//                    1,
//                )
//                Timber.tag("AnonMain")
//                    .d("Creating wallet Seed        : %s", anonWallet?.getSeed(passPhrase))
//                Timber.tag("AnonMain")
//                    .d("Creating wallet Legacy Seed : %s", anonWallet?.getLegacySeed(passPhrase))
//                anonWallet?.store()
//                inProgress.postValue(false)
//                anonWallet?.status?.let {
//                    walletDeubug.postValue("${walletDeubug.value}\n$it")
//                    isWalletCreated.postValue(it.isOk)
//                    walletOpened.postValue(it.isOk)
//                    showSeedAndAddress(anonWallet, passPhrase)
//                    withContext(Dispatchers.IO) {
////                        handler = MoneroHandlerThread(
////                            "WalletService",
////                            WalletRepo(),
////                            listener = this@WalletTestViewModel,
////                            anonWallet
////                        )
//                        WalletManager.instance?.wallet?.let {
//                            wallet?.setListener(handler!!)
//                            handler?.start()
//                            startWalletService(context = context)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun startWalletService(context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
//            val host = prefs.getString("host", "stagenet.community.rino.io")
//            val rpcPort = prefs.getString("rpcPort", "38081")
//            Log.i("TAG", "startWalletService: $host $rpcPort")
//            if (host == null || rpcPort == null) {
//                return@launch
//            }
//            val node = Node.fromJson(
//                JSONObject()
//                    .apply {
//                        put("host", host)
//                        put("rpcPort", rpcPort)
//                        put("network", AnonConfig.getNetworkType().toString())
//                        put("name", "anon")
//                    }
//            )
//            postDebug("Setting daemon to ${node?.host}:${node?.rpcPort}")
//            WalletManager.instance?.setDaemon(node)
//            WalletManager.instance?.wallet?.init(0)
//            WalletManager.instance?.wallet?.setTrustedDaemon(true)
//            WalletManager.instance?.wallet?.startRefresh()
//        }
//    }
//
//
//    fun generateAddress() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val add = arrayListOf<String?>()
//            add.addAll(subaddress.value ?: arrayOf())
//            add.add(WalletManager.instance?.wallet?.getSubaddress(0, index))
//            subaddress.postValue(
//                add.toTypedArray()
//            )
//            index += 1
//        }
//        Log.i("TAG", "generateAddress: ${this.hashCode()} ${index}")
//    }
//
//    fun isWalletSync(): Boolean {
//        return WalletManager.instance?.wallet?.isSynchronized ?: false
//    }
//
//    fun openWallet(localContext: Context, passphrase: String, pin: String) {
//        viewModelScope.launch {
//
//            withContext(Dispatchers.IO) {
//                inProgress.postValue(true)
//                val walletFile = AnonConfig.getDefaultWalletFile(localContext)
////                WalletManager.instance?.setDaemon()
//                val anonWallet = WalletManager.instance?.openWallet(
//                    walletFile.path,
//                    pin,
//                )
//                if (anonWallet?.status?.isOk != true) {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            localContext,
//                            "Failed to open wallet: ${anonWallet?.status?.errorString ?: ""}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    inProgress.postValue(false)
//                    return@withContext
//                }
//                Log.d(
//                    "AnonMain",
//                    "Open wallet Seed        : ${anonWallet.getSeed(passphrase)}"
//                )
//                Log.d(
//                    "AnonMain",
//                    "Open wallet Legacy Seed : ${anonWallet.getLegacySeed(passphrase)}"
//                )
//
//                anonWallet.store()
//                showSeedAndAddress(anonWallet, passphrase)
//                anonWallet.status.let {
//                    isWalletCreated.postValue(it.isOk)
//                    walletOpened.postValue(it.isOk)
//                    address.postValue(anonWallet.getAddress(0))
//                    val wallet = WalletManager.instance?.wallet
//                    viewModelScope.launch {
//                        withContext(Dispatchers.IO) {
////                            handler = MoneroHandlerThread(
////                                "WalletService",
////                                WalletRepo(),
////
////                                listener = this@WalletTestViewModel,
////                                anonWallet
////                            )
//                            WalletManager.instance?.wallet?.let {
//                                wallet?.setListener(handler!!)
//                                handler?.start()
//                                startWalletService(context = localContext)
//                                Log.i("TAG", "openWallet: startWalletService")
//                            }
//                        }
//
//                    }
//                }
//                walletSyncFlow.emit("Wallet running...")
//                inProgress.postValue(false)
//            }
//        }
//    }
//
//    private fun showSeedAndAddress(anonWallet: Wallet, passphrase: String) {
//        seed.postValue((anonWallet.getSeed(passphrase) ?: "").split(" ").toTypedArray())
//        seedLegacy.postValue(
//            (anonWallet.getLegacySeed(passphrase) ?: "").split(" ").toTypedArray()
//        )
//        address.postValue(anonWallet.getAddress(0))
//        Log.i(
//            "Wallet", "\n\n" +
//                    "wallet address :${anonWallet.getAddress(1)}\n" +
//                    "wallet address :${anonWallet.getSubaddress(0, 2)}\n" +
//                    "wallet getSecretViewKey :${anonWallet.getSecretViewKey()}\n" +
//                    "wallet getSecretSpendKey :${anonWallet.getSecretSpendKey()}\n" +
//                    "wallet getRestoreHeight :${anonWallet.getRestoreHeight()}\n" +
//                    "wallet networkType :${anonWallet.networkType}\n" +
//                    "wallet getPath : ${anonWallet.getPath()}\n\n"
//        )
//    }
//
//    fun removeWallet(localContext: Context) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                WalletManager.instance?.wallet?.let {
//                    WalletManager.instance?.close(it)
//                }
//                AnonConfig.getDefaultWalletDir(localContext).deleteRecursively()
//                WalletManager.resetInstance()
//                isWalletCreated.postValue(false)
//                walletOpened.postValue(false)
//                seed.postValue(arrayOf())
//                seedLegacy.postValue(arrayOf())
//                address.postValue("")
//            }
//        }
//    }
//
//    fun restoreWallet(
//        context: Context,
//        seed: String,
//        passphrase: String,
//        pin: String,
//        height: Long
//    ) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                inProgress.postValue(true)
//                context.applicationContext.filesDir.deleteRecursively()
//                val walletFile = AnonConfig.getDefaultWalletFile(context)
//                val seedWords = seed.split(" ").toTypedArray()
//                val recoveredWallet: Wallet?
//                Log.i(
//                    "TAG", "restoreWallet: " +
//                            "passphrase : $passphrase\n" +
//                            "pin : $pin" +
//                            ""
//                )
//                when (seedWords.size) {
//                    25 -> {
//                        recoveredWallet = WalletManager.instance?.recoveryWallet(
//                            walletFile,
//                            password = pin,
//                            mnemonic = seedWords.joinToString { " " },
//                            offset = passphrase,
//                            restoreHeight = height
//                        )
//                    }
//
//                    16 -> {
//                        recoveredWallet = WalletManager.instance?.recoveryWalletPolyseed(
//                            walletFile,
//                            password = pin,
//                            mnemonic = seed,
//                            offset = passphrase,
//                        )
//                    }
//
//                    else -> {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(context, "Invalid seed", Toast.LENGTH_SHORT).show()
//                        }
//                        return@withContext
//                    }
//                }
//                if (recoveredWallet?.status?.isOk == true) {
////                    recoveredWallet.store()
////                    recoveredWallet.close()
//                    delay(1000)
//                    val reOpenedWallet = recoveredWallet
//                    Log.i("TAG", "restoreWallet networkType: ${reOpenedWallet.networkType}")
//                    if (reOpenedWallet.status?.isOk == true) {
//                        Log.i(
//                            "TAG", "restoreWallet: ${reOpenedWallet.getRestoreHeight()}\n" +
//                                    "getDaemonBlockChainHeight ${reOpenedWallet.getDaemonBlockChainHeight()}\n" +
//                                    "getBlockChainHeight ${reOpenedWallet.getBlockChainHeight()}" +
//                                    "getDaemonBlockChainTargetHeight ${reOpenedWallet.getDaemonBlockChainTargetHeight()}" +
//                                    "getApproximateBlockChainHeight ${reOpenedWallet.getApproximateBlockChainHeight()}"
//                        )
//
//                        isWalletCreated.postValue(true)
//                        walletOpened.postValue(true)
//                        showSeedAndAddress(reOpenedWallet, passphrase)
//                        viewModelScope.launch(Dispatchers.IO) {
//                            Log.i("TAG", "openWallet: startWalletService()")
////                            handler = MoneroHandlerThread(
////                                "WalletService",
////                                WalletRepo(),
////                                listener = this@WalletTestViewModel,
////                                reOpenedWallet
////                            )
//                            reOpenedWallet.let {
//                                reOpenedWallet.setListener(handler!!)
//                                handler?.start()
//                            }
//                            startWalletService(context)
//                        }
//                        inProgress.postValue(false)
//                        reOpenedWallet.store()
//                    } else {
//                        isWalletCreated.postValue(false)
//                        walletOpened.postValue(false)
//                        inProgress.postValue(false)
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(
//                                context,
//                                "Unable to restore wallet: ${recoveredWallet.status?.errorString}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        return@withContext
//                    }
//                } else {
//                    isWalletCreated.postValue(false)
//                    walletOpened.postValue(false)
//                    inProgress.postValue(false)
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            context,
//                            "Unable to restore wallet: ${recoveredWallet?.status?.errorString}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    return@withContext
//                }
//
//            }
//        }
//    }
//
//    fun closeWallet(context: Context) {
//
//        viewModelScope.launch {
//
//            withContext(Dispatchers.IO) {
//                WalletManager.instance?.wallet?.let {
//                    it.setListener(null)
//                    closingWallet.postValue(true)
//                    delay(500)
//                    it.store()
//                    delay(500)
//                    it.close()
//                    delay(500)
//                    WalletManager.resetInstance()
//                    closingWallet.postValue(false)
//                    isWalletCreated.postValue(true)
//                    walletOpened.postValue(false)
//                    seed.postValue(arrayOf())
//                    seedLegacy.postValue(arrayOf())
//                    address.postValue("")
//
////                    context.getActivity()?.finishAndRemoveTask()
//                }
//            }
//        }
//    }
//
//    override fun onRefresh(walletSynced: Boolean) {
//        postDebug("onWalletRefresh synced ? : $walletSynced")
//
//        Log.i(
//            Thread.currentThread().name,
//            "onRefresh getDaemonBlockChainHeight:  ${wallet?.getDaemonBlockChainHeight()}"
//        )
//        if (wallet != null) {
//            balance.postValue(wallet!!.getBalanceAll())
//            coins.postValue(wallet!!.coins?.getCount())
//
//        }
//        if(walletSynced){
//           viewModelScope.launch {
//                withContext(Dispatchers.IO){
//                    walletSyncFlow.emit("Wallet Sync Complete")
//                    AnonConfig.context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
//                        ?.edit()
//                        ?.putBoolean("sync", true)
//                        ?.apply()
//                }
//           }
//        }
//    }
//
//    private val wallet: Wallet? get() = WalletManager.instance?.wallet
//
//    override fun onNewBlockFound(block: Long) {
//        currentBlockProgress.postValue(block)
//        daemonBlockChainTargetHeightLive.postValue(wallet?.getRestoreHeight() ?: 1)
//        postDebug("New Block $block")
//        viewModelScope.launch {
//            walletSyncFlow.emit("Syncing: : Scanning Block $block")
//        }
//        Log.i("TAG", "onNewBlockFound balance: ${wallet?.getBalanceAll()}")
//
//    }
//
//    private fun postDebug(value: String) {
//        walletDeubug.postValue("${walletDeubug.value}\n$value")
//    }
//
//    override fun onConnectionFail() {
//        postDebug("Connection failed ${wallet?.status?.connectionStatus.toString()}")
//        Log.i("TAG", "onConnectionFail: ")
//    }
//
//    fun makeTransaction(inputAddress: String, amount: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                broadcastStatus.postValue(BroadcastStatus.Loading)
//                postDebug(
//                    "Create Tx : address: $inputAddress Amount : ${
//                        Wallet.getAmountFromString(
//                            amount
//                        )
//                    }"
//                )
//                val txHandle = WalletManager.instance?.wallet?.createTransactionJ(
//                    dstAddr = inputAddress,
//                    amount = Wallet.getAmountFromString(amount),
//                    priority = PendingTransaction.Priority.Priority_Medium.ordinal,
//                    accountIndex = 0,
//                    mixinCount = 0,
//                    paymentId = "",
//                    keyImages = arrayListOf()
//                )
//                postDebug("Create Tx : ${txHandle}")
//                if (txHandle != null) {
//                    val pending = PendingTransaction(txHandle)
//                    postDebug("Pending tx created ${pending.status}")
//                    val success = WalletManager.instance?.wallet?.send(pending)
//                    if (success == true) {
//                        WalletManager.instance?.wallet?.refreshHistory()
//                        WalletManager.instance?.wallet?.store()
//                        postDebug("Tx broadcast ? $success ")
//                        broadcastStatus.postValue(BroadcastStatus.Success)
//                    } else {
//                        postDebug("Tx broadcast failed ")
//                        broadcastStatus.postValue(BroadcastStatus.Error)
//                    }
//                } else {
//                    postDebug("unable to create tx $txHandle")
//                }
//            } catch (e: Exception) {
//                broadcastStatus.postValue(BroadcastStatus.Error)
//                postDebug("Create Tx Error: $e")
//            }
//        }
//    }
//}
//
//class WalletTest : ComponentActivity() {
//    private val walletViewModel: WalletTestViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            AnonNeroTheme {
//                TestWallet()
//            }
//        }
//        val requestPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted ->
//            if (isGranted) {
//                Intent(applicationContext, AnonNeroService::class.java)
//                    .also {
//                        it.action = "start"
//                        ContextCompat.startForegroundService(applicationContext,it)
//                    }
//            }
//            //handle and show dialog
//        }
//        // Check if the permission is already granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        } else {
//            Intent(applicationContext, AnonNeroService::class.java)
//                .also {
//                    it.action = "start"
//                    ContextCompat.startForegroundService(applicationContext,it)
//                }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        walletViewModel.checkWallet(this.applicationContext)
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TestWallet() {
//    val scope = rememberCoroutineScope()
//    val localContext = LocalContext.current
//    val vm: WalletTestViewModel = viewModel()
//    val walletCreated by vm.isWalletCreated.observeAsState(false)
//    val walletOpened by vm.walletOpened.observeAsState(false)
//    val inProgress by vm.inProgress.observeAsState(false)
//    var openCreateDialog by remember { mutableStateOf(false) }
//    var restoreWalletDialog by remember { mutableStateOf(false) }
//    var doCreateWallet by remember { mutableStateOf(false) }
//    val balance by vm.balance.observeAsState(0L)
//    val coins by vm.coins.observeAsState(0)
//    val closingWallet by vm.closingWallet.observeAsState(false)
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        when {
//            openCreateDialog -> {
//                GenerateWallet(
//                    openWallet = openCreateDialog,
//                    onClose = { openCreateDialog = false },
//                    onConfirmed = { pin, passphrase ->
//                        if (doCreateWallet) {
//                            vm.createWallet(localContext, passphrase, pin)
//                            openCreateDialog = false
//                        } else {
//                            vm.openWallet(localContext, passphrase, pin)
//                            openCreateDialog = false
//                        }
//                    })
//            }
//
//            restoreWalletDialog -> {
//                RecoverWallet(
//                    onClose = { restoreWalletDialog = false },
//                    onConfirmed = { seed, pin, passphrase, height ->
//                        vm.restoreWallet(localContext, seed, passphrase, pin, height ?: 0)
//                        restoreWalletDialog = false
//                    })
//            }
//        }
//        if (closingWallet) {
//            Box {
//                CircularProgressIndicator(
//                    modifier = Modifier
//                        .size(34.dp)
//                        .align(Alignment.Center),
//                    strokeWidth = 2.dp
//                )
//            }
//        }
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("Anonero")
//                        }
//                    },
//                )
//            },
//        ) {
//            Surface(
//                modifier = Modifier
//                    .padding(it)
//                    .padding(horizontal = 16.dp)
//            ) {
//                if (inProgress) {
//                    return@Surface Box(modifier = Modifier.fillMaxWidth(), content = {
//                        CircularProgressIndicator(
//                            modifier = Modifier
//                                .size(34.dp)
//                                .align(Alignment.Center),
//                            strokeWidth = 2.dp
//                        )
//                    })
//                }
//                if (walletCreated) {
//                    if (!walletOpened) {
//                        return@Surface Column(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                        ) {
//                            Button(
//                                onClick = {
//                                    openCreateDialog = true
//                                    doCreateWallet = false
//                                }, modifier = Modifier
//                                    .fillMaxWidth(0.6f)
//                            )
//                            {
//                                Text("Open Wallet")
//                            }
//                            NetWorkSetup()
//                        }
//                    } else {
//
//                        Scaffold(
//                            bottomBar = {
//                                Column(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    Arrangement.Center,
//                                    Alignment.CenterHorizontally
//                                ) {
//                                    Button(
//                                        onClick = {
//                                            vm.closeWallet(localContext)
//                                        }, modifier = Modifier
//                                            .fillMaxWidth(0.6f)
//                                            .padding(
//                                                8.dp
//                                            ),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = Color.Transparent,
//                                            contentColor = Color.White
//                                        )
//                                    )
//                                    {
//                                        Text("Close Wallet")
//                                    }
//                                    Button(
//                                        onClick = {
//                                            vm.removeWallet(localContext)
//                                        }, modifier = Modifier
//                                            .fillMaxWidth(0.6f)
//                                            .padding(
//                                                8.dp
//                                            ),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = Color.Red,
//                                            contentColor = Color.White
//                                        )
//                                    )
//                                    {
//                                        Text("Remove Wallet")
//                                    }
//                                }
//                            }
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(it),
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                LazyColumn {
//                                    item {
//                                        Sync()
//                                    }
//                                    item {
//                                        ListItem(
//                                            headlineContent = {
//                                                Text(
//                                                    "Balance",
//                                                    style = MaterialTheme.typography.bodyLarge
//                                                )
//                                            },
//                                            supportingContent = {
//                                                Text(
//                                                    "${Formats.getDisplayAmount(balance, 6)} XMR",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            },
//                                            trailingContent = {
//                                                Text(text = "Coins : $coins ")
//                                            }
//                                        )
//                                    }
//                                    item {
//                                        ListItem(
//                                            headlineContent = {
//                                                Text(
//                                                    "Seed",
//                                                    style = MaterialTheme.typography.bodyLarge
//                                                )
//                                            },
//                                            supportingContent = {
//                                                Text(
//                                                    "${vm.seed.value?.joinToString(" ")}",
//                                                    style = MaterialTheme.typography.bodyMedium
//                                                )
//                                            },
//                                        )
//                                    }
//                                    item {
//                                        Address()
//                                    }
//                                    item {
//                                        Spend()
//                                    }
//                                    item {
//                                        ListItem(
//                                            headlineContent = {
//                                                Text(text = "Wallet Debug Logs")
//                                            },
//                                            supportingContent = {
//                                                DebugInfo()
//                                            },
//                                        )
//                                    }
//
//                                }
//
//                            }
//                        }
//                    }
//                } else {
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(24.dp)
//                    ) {
//                        Button(
//                            onClick = {
//                                openCreateDialog = true
//                                doCreateWallet = true
//                            }, modifier = Modifier
//                                .fillMaxWidth(0.6f)
//
//                        )
//                        {
//                            Text("Create Wallet")
//                        }
//                        Button(
//                            onClick = {
//                                restoreWalletDialog = true
//                                doCreateWallet = false
//                            }, modifier = Modifier
//                                .fillMaxWidth(0.6f)
//                        )
//                        {
//                            Text("Recover Wallet")
//                        }
//                        NetWorkSetup()
//                    }
//                }
//            }
//        }
//
//    }
//}
//
//@Composable
//fun DebugInfo() {
//    val vm: WalletTestViewModel = viewModel()
//    val scrollState = rememberLazyListState()
//    val debugString = vm.walletDeubug.observeAsState("")
//    val strings = debugString.value.split("\n")
//
//    LaunchedEffect(key1 = strings.size) {
//        scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount)
//    }
//
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(220.dp)
//            .background(Color(0xFF2C2C2C))
//            .padding(
//                horizontal = 6.dp,
//                vertical = 8.dp
//            ),
//        state = scrollState
//    ) {
//        strings.forEach {
//            item {
//                Text(
//                    text = it,
//                    fontSize = 9.sp,
//                    lineHeight = 12.sp,
//                    color = Color.White,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .scrollable(state = scrollState, orientation = Orientation.Horizontal)
//
//                )
//            }
//        }
//    }
//}
//
//
//@Composable
//fun Spend(modifier: Modifier = Modifier) {
//    var inputAddress by remember { mutableStateOf("") }
//    var amount by remember { mutableStateOf("") }
//    var expandedState by remember { mutableStateOf(false) }
//    val rotate: Float by animateFloatAsState(if (expandedState) 90.0f else 0.0f, label = "anim")
//
//    val vm: WalletTestViewModel = viewModel()
//    val broadCastStatus by vm.broadcastStatus.observeAsState()
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .animateContentSize(
//                animationSpec = tween(
//                    durationMillis = 300,
//                    easing = LinearOutSlowInEasing
//                )
//            ),
//        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.outlinedCardColors(
//
//        ),
//        onClick = {
//            expandedState = !expandedState
//        }
//    ) {
//        ListItem(headlineContent = {
//            Text(text = "Spend")
//        }, trailingContent = {
//            Icon(
//                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
//                modifier = Modifier.rotate(
//                    rotate
//                ),
//                contentDescription = ""
//            )
//        }, colors = ListItemDefaults.colors(
//            containerColor = Color.Transparent
//        )
//        )
//        if (!expandedState) {
//            Box {
//
//            }
//        } else {
//
//            Box(
//                modifier = Modifier.animateContentSize(
//                    animationSpec = tween(
//                        durationMillis = 400
//                    )
//                )
//            ) {
//                when (broadCastStatus) {
//                    BroadcastStatus.Staging -> {
//                        Column(
//                            modifier = modifier
//                                .fillMaxWidth()
//                                .padding(12.dp)
//                        ) {
//                            OutlinedTextField(
//                                value = inputAddress,
//                                onValueChange = {
//                                    inputAddress = it
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(8.dp),
//                                placeholder = {
//                                    Text(text = "Address")
//                                },
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(value = amount,
//                                placeholder = {
//                                    Text(text = "Amount")
//                                },
//                                shape = RoundedCornerShape(8.dp),
//                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                singleLine = true,
//                                modifier = Modifier.fillMaxWidth(),
//                                onValueChange = {
//                                    amount = it
//                                })
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedButton(
//                                onClick = {
//                                    Log.i("TAG", "Spend:  $inputAddress $amount")
//                                    vm.makeTransaction(inputAddress, amount)
//                                },
//                            ) {
//                                Text("Send")
//                            }
//                        }
//                    }
//
//                    BroadcastStatus.Loading -> {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .align(Alignment.Center)
//                                .height(84.dp)
//                        ) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.align(Alignment.Center)
//                            )
//                        }
//                    }
//
//                    BroadcastStatus.Success -> {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .align(Alignment.Center),
//                            Arrangement.Center,
//                            Alignment.CenterHorizontally
//                        ) {
//                            Text("Broadcast success", style = MaterialTheme.typography.bodyMedium)
//                            Spacer(modifier = Modifier.padding(16.dp))
//                            OutlinedButton(onClick = {
//                                inputAddress = ""
//                                amount = ""
//                                vm.broadcastStatus.postValue(BroadcastStatus.Staging)
//                            }) {
//                                Text("Exit")
//                            }
//                        }
//                    }
//
//                    BroadcastStatus.Error -> {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .align(Alignment.Center),
//                            Arrangement.Center,
//                            Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                "Broadcast Error check logs...",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = Color.Red
//                            )
//                            Spacer(modifier = Modifier.padding(16.dp))
//                            OutlinedButton(onClick = {
//                                inputAddress = ""
//                                amount = ""
//                                vm.broadcastStatus.postValue(BroadcastStatus.Staging)
//                            }) {
//                                Text("Ok")
//                            }
//                        }
//                    }
//
//                    null -> {
//
//                        Text("Error")
//                    }
//                }
//
//            }
//
//        }
//    }
//
//
//}
//
//
//@Composable
//fun Address(modifier: Modifier = Modifier) {
//    val vm: WalletTestViewModel = viewModel()
//    var expandedState by remember { mutableStateOf(false) }
//    val rotate: Float by animateFloatAsState(if (expandedState) 90.0f else 0.0f, label = "anim")
//    val addresses by vm.subaddress.observeAsState(initial = arrayOf())
//    val address by vm.address.observeAsState(initial = "")
//    val clipboardManager = LocalClipboardManager.current
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .animateContentSize(
//                animationSpec = tween(
//                    durationMillis = 300,
//                    easing = LinearOutSlowInEasing
//                )
//            ),
//        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.outlinedCardColors(
//            containerColor = Color.Transparent
//        ),
//        onClick = {
//            expandedState = !expandedState
//        }
//    ) {
//        ListItem(
//            colors = ListItemDefaults.colors(
//                containerColor = Color.Transparent
//            ),
//            headlineContent = {
//                Text(text = "Address")
//            },
//            trailingContent = {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
//                    modifier = Modifier.rotate(
//                        rotate
//                    ),
//                    contentDescription = ""
//                )
//            },
//        )
//        if (!expandedState) {
//            Box {
//
//            }
//        } else {
//            Column {
//                ListItem(
//                    headlineContent = {
//                        Text(text = "Primary address", fontSize = 11.sp)
//                    },
//                    supportingContent = {
//                        Text(text = address ?: "", fontSize = 9.sp)
//                    },
//                    trailingContent = {
//                        IconButton(
//                            onClick = {
//                                clipboardManager.setText(
//                                    AnnotatedString(
//                                        text = address ?: "",
//                                    )
//                                )
//                                scope.launch {
//                                    snackbarHostState.showSnackbar("Copied address")
//                                }
//                            },
//                            content = {
//                                Icon(
//                                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
//                                    contentDescription = ""
//                                )
//                            },
//                        )
//                    }, colors = ListItemDefaults.colors(
//                        containerColor = Color.Transparent
//                    )
//                )
//                for (addr in addresses) {
//                    ListItem(
//                        headlineContent = {
//                            Text(
//                                text = "Subaddress ${addresses.indexOf(addr) + 1}",
//                                fontSize = 11.sp
//                            )
//                        },
//                        supportingContent = {
//                            Text(text = addr ?: "", fontSize = 9.sp)
//                        },
//                        trailingContent = {
//                            IconButton(
//                                onClick = {
//                                    clipboardManager.setText(
//                                        AnnotatedString(
//                                            text = addr ?: "",
//                                        )
//                                    )
//                                    scope.launch {
//                                        snackbarHostState.showSnackbar("Copied address")
//                                    }
//                                },
//                                content = {
//                                    Icon(
//                                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
//                                        contentDescription = ""
//                                    )
//                                },
//                            )
//                        }, colors = ListItemDefaults.colors(
//                            containerColor = Color.Transparent
//                        )
//                    )
//                }
//                OutlinedButton(onClick = {
//                    vm.generateAddress()
//                }, modifier = Modifier.padding(12.dp)) {
//                    Text("Generate next address")
//                }
//            }
//
//        }
//    }
//
//
//}
//
//
//@Composable
//fun NetWorkSetup() {
//    val vm: WalletTestViewModel = viewModel()
//    val scope = rememberCoroutineScope()
//    var host by remember { mutableStateOf("") }
//    var rpcPort by remember { mutableStateOf("") }
//    var expandedState by remember { mutableStateOf(false) }
//    val localContext = LocalContext.current
//    val rotate: Float by animateFloatAsState(if (expandedState) 90.0f else 0.0f, label = "anim")
//
//    LaunchedEffect(true) {
//        val prefs = localContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
//        val hostPref = prefs.getString("host", "stagenet.community.rino.io")
//        val rpcPortPref = prefs.getString("rpcPort", "38081")
//        host = hostPref ?: ""
//        rpcPort = rpcPortPref ?: ""
//    }
//
//    Column(
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(
//                    vertical = 24.dp,
//                )
//                .animateContentSize(
//                    animationSpec = tween(
//                        durationMillis = 300,
//                        easing = LinearOutSlowInEasing
//                    )
//                ),
//            shape = RoundedCornerShape(8.dp),
//            colors = CardDefaults.outlinedCardColors(
//                containerColor = Color.Transparent
//            ),
//            onClick = {
//                expandedState = !expandedState
//            }
//        ) {
//            ListItem(headlineContent = {
//                Text(text = "Network Setup")
//            }, trailingContent = {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
//                    modifier = Modifier.rotate(
//                        rotate
//                    ),
//                    contentDescription = ""
//                )
//            }, colors = ListItemDefaults.colors(
//                containerColor = Color.Transparent
//            )
//            )
//
//            if (!expandedState) {
//                Box {
//
//                }
//            } else {
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    OutlinedTextField(
//                        value = host,
//                        onValueChange = {
//                            host = it
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(
//                                vertical = 4.dp
//                            ),
//                        shape = RoundedCornerShape(8.dp),
//                        placeholder = {
//                            Text(text = "Host")
//                        },
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    OutlinedTextField(value = rpcPort,
//                        placeholder = {
//                            Text(text = "Port")
//                        },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        shape = RoundedCornerShape(8.dp),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(
//                                vertical = 4.dp
//                            ),
//                        onValueChange = {
//                            rpcPort = it
//                        })
//                    OutlinedButton(
//                        onClick = {
//                            scope.launch(Dispatchers.IO) {
//                                val prefs =
//                                    localContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
//                                prefs.edit()
//                                    .putString("host", host)
//                                    .putString("rpcPort", rpcPort)
//                                    .apply()
//                                expandedState = false
//                                withContext(Dispatchers.Main) {
//                                    Toast.makeText(localContext, "Saved", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        },
//                    ) {
//                        Text("Save")
//                    }
//                }
//            }
//        }
//
//        Text(
//            text = "Network: ${AnonConfig.getNetworkType().toString()
//                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}\n" +
//                    "Flavour : ${BuildConfig.FLAVOR} \n" +
//                    "Build Type: ${BuildConfig.BUILD_TYPE}\n" +
//                    "Build Version: ${BuildConfig.VERSION_NAME}\n" +
//                    "", style = MaterialTheme.typography.labelSmall,
//            textAlign = TextAlign.Center,
//            fontFamily = FontFamily.Monospace,
//            fontWeight = FontWeight.SemiBold,
//            lineHeight = 24.sp
//        )
//    }
//}
//
//@Preview
//@Composable
//private fun DebugInfoPreview() {
//    AnonNeroTheme {
//        Scaffold {
//            Box(modifier = Modifier.padding(it)) {
//                Spend()
//            }
//        }
//    }
//}
//
//@Composable
//fun GenerateWallet(
//    modifier: Modifier = Modifier,
//    onClose: () -> Unit,
//    onConfirmed: (pin: String, passphrase: String) -> Unit,
//    openWallet: Boolean
//) {
//    var passphrase by remember { mutableStateOf("test") }
//    var pin by remember { mutableStateOf("12345") }
//    val vm: WalletTestViewModel = viewModel()
//    val context = LocalContext.current
//    return AlertDialog(
//        text = {
//            Column(
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                content = {
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        singleLine = true,
//                        value = passphrase,
//                        onValueChange = { passphrase = it },
//                        label = { Text("test") }
//                    )
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        value = pin,
//                        onValueChange = { pin = it },
//                        label = { Text("pin") }
//                    )
//                }
//            )
//        },
//        onDismissRequest = {
//            onClose.invoke()
//        },
//        icon = { Icons.Outlined.Create },
//        title = { Text(if (openWallet) "Open Wallet" else "Create Wallet") },
//        dismissButton = {
//            TextButton(
//                onClick = {
//                    onClose.invoke()
//                },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text("Cancel")
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    onConfirmed.invoke(pin, passphrase)
//                },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text(if (openWallet) "Open Wallet" else "Create Wallet")
//            }
//        },
//        shape = MaterialTheme.shapes.medium
//    )
//
//}
//
//
//@Composable
//fun RecoverWallet(
//    modifier: Modifier = Modifier,
//    onClose: () -> Unit,
//    onConfirmed: (seed: String, pin: String, passphrase: String, height: Long?) -> Unit,
//) {
//    var seedWords by remember { mutableStateOf("") }
//    var pin by remember { mutableStateOf("12345") }
//    var passphrase by remember { mutableStateOf("test") }
//    var height by remember { mutableStateOf("") }
//    val vm: WalletTestViewModel = viewModel()
//    val context = LocalContext.current
//    return AlertDialog(
//        text = {
//            Column(
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                content = {
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        singleLine = false,
//                        minLines = 4,
//                        value = seedWords,
//                        onValueChange = { seedWords = it },
//                        label = { Text("Seed") }
//                    )
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        value = passphrase,
//                        onValueChange = { passphrase = it },
//                        label = { Text("test") }
//                    )
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        value = pin,
//                        onValueChange = { pin = it },
//                        label = { Text("pin") }
//                    )
//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        value = height,
//                        onValueChange = { height = it },
//                        label = { Text("Restore Height") }
//                    )
//
//                }
//            )
//        },
//        onDismissRequest = {
//            onClose.invoke()
//        },
//        icon = { Icons.Outlined.Create },
//        title = { Text("Recover wallet") },
//        dismissButton = {
//            TextButton(
//                onClick = {
//                    onClose.invoke()
//                },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text("Cancel")
//            }
//        },
//        confirmButton = {
//            TextButton(
//                onClick = {
//                    onConfirmed.invoke(seedWords, pin, passphrase, height.toLongOrNull())
//                },
//                modifier = Modifier.padding(8.dp)
//            ) {
//                Text("Recover")
//            }
//        },
//        shape = MaterialTheme.shapes.medium
//    )
//
//}
//
//
//@Composable
//fun Sync(modifier: Modifier = Modifier) {
//    val vm: WalletTestViewModel = viewModel()
//    val daemonHeight by vm.currentBlockProgress.observeAsState(0L)
//    val targetHeight by vm.daemonBlockChainTargetHeightLive.observeAsState(0L)
//
//    val percentage = calculateSyncPercentage(daemonHeight.toInt(), targetHeight.toInt())
//        .coerceIn(
//            maximumValue = 1f,
//            minimumValue = 0f
//        )
//
//    if (percentage != 0.0f && percentage <= 0.95f && !vm.isWalletSync()) {
//        Column(
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                "Wallet Sync Progress",
//                style = MaterialTheme.typography.bodyMedium,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(
//                    horizontal = 12.dp
//                )
//            )
//            LinearProgressIndicator(
//                color = Color.Green,
//                trackColor = Color.Gray,
//                progress = { percentage },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        horizontal = 12.dp,
//                        vertical = 8.dp
//                    ),
//                strokeCap = StrokeCap.Round
//            )
//            Text(
//                text = "${(percentage * 100).toInt()}%", textAlign = TextAlign.Center,
//                fontSize = 11.sp,
//                modifier = Modifier
//                    .padding(
//                        horizontal = 8.dp
//                    )
//                    .align(Alignment.CenterHorizontally)
//            )
//        }
//    }
//
//}
//
//fun calculateSyncPercentage(daemonHeight: Int, targetHeight: Int): Float {
//    if (targetHeight == 0) {
//        return 0.0f // Avoid division by zero
//    }
//    val percentage = (daemonHeight.toDouble() / targetHeight.toDouble())
//    return percentage.toFloat()
//}
//
//
//@Preview(heightDp = 640, showBackground = true)
//@Composable
//private fun AnonMainPre() {
//    AnonNeroTheme {
//        TestWallet()
//    }
//}