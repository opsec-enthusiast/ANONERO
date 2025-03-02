package io.anonero.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import io.anonero.FOREGROUND_CHANNEL
import io.anonero.R
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.util.WALLET_PREFERENCES
import io.anonero.util.WALLET_USE_TOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject

const val NOTIFICATION_ID = 2

fun startAnonService(context: Context) {
    val intent = Intent(context, AnonNeroService::class.java).apply {
        action = "start"
    }
    context.startForegroundService(intent)
}

class AnonNeroService : Service() {

    private val TAG: String = AnonNeroService::class.java.simpleName
    private val job = SupervisorJob()

    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val walletState: WalletState by inject(WalletState::class.java)
    private val torService: TorService by inject(TorService::class.java)
    private val prefs: SharedPreferences by inject(named(WALLET_PREFERENCES))

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "start") {
            start()
        }
        if (intent?.action == "stop") {
            stopForeground(STOP_FOREGROUND_REMOVE) // Properly removes the notification
            stopSelf() // Stops the service completely
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun start() {
        startForeground(NOTIFICATION_ID, foregroundNotification())
        scope.launch {
            walletState.walletConnectionStatus.collect {
                updateNotificationState()
            }
            walletState.walletStatus.collect {
                updateNotificationState()
            }
        }
        // Update notification state every 2 seconds
        scope.launch {
            while (scope.isActive) {
                updateNotificationState()
                delay(1000)
            }
        }
        scope.launch {
            walletState.syncProgress.collect {
                if (it != null) {
                    withContext(Dispatchers.Main) {
                        showProgress(it)
                    }
                }
            }
        }
    }

    private suspend fun updateNotificationState() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val wallet = WalletManager.instance?.wallet
        val torSate = if (torService.socks != null && prefs.getBoolean(WALLET_USE_TOR, true)) {
            " | Using Tor Proxy: ${torService.socks?.port.toString()}"
        } else {
            ""
        }
        if (wallet != null) {
            val isSyncing = walletState.isSyncing
            if (!isSyncing) {
                val notificationMessage = if (walletState.backgroundSync) {
                    "Wallet Locked: Synced: ${wallet.getBlockChainHeight()} "
                } else if (!wallet.isInitialized) {
                    "Loading wallet..."
                } else {
                    val connectionStatus = wallet.fullStatus.connectionStatus
                        ?: Wallet.ConnectionStatus.ConnectionStatus_Disconnected
                    when (connectionStatus) {
                        Wallet.ConnectionStatus.ConnectionStatus_Disconnected -> {
                            "Daemon Disconnected"
                        }

                        Wallet.ConnectionStatus.ConnectionStatus_WrongVersion -> {
                            "Wrong Version"
                        }

                        Wallet.ConnectionStatus.ConnectionStatus_Connected -> {
                            if (wallet.getBlockChainHeight() > 1) {
                                "Synced: ${wallet.getBlockChainHeight()}"
                            } else {
                                "Syncing..."
                            }
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    mNotificationManager.notify(
                        NOTIFICATION_ID,
                        foregroundNotification("$notificationMessage$torSate")
                    )
                }
            }
        }
    }

    private fun showProgress(it: SyncProgress) {
        val notification = foregroundNotification(
            content = if (it.left != 0L) "Syncing: ${it.left} blocks left" else "Syncing blocks completed",
            progress = it
        )
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private fun foregroundNotification(
        content: String = "Loading wallet...",
        title: String = "[ΛИ0ИΞR0]",
        progress: SyncProgress? = null
    ): Notification {

        //TODO: Add pending intent to open the app
//        val mainActivityIndent = Intent(this, MainActivity::class.java)
//            .apply {
//                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                putExtra("notification", true)
//            }
//
////        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
////            addNextIntentWithParentStack(mainActivityIndent)
////            getPendingIntent(
////                0,
////                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
////            )
////        }

        return NotificationCompat.Builder(this.applicationContext, FOREGROUND_CHANNEL)
            .setSmallIcon(R.drawable.anon_notification)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setAutoCancel(false)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentTitle(title)
            .setContentText(content)
            .apply {
                if (progress != null && progress.progress < 1) {
                    this.setProgress(100, (progress.progress * 100).toInt(), false)
                }
            }
            .setGroup("BackgroundService")
//            .setContentIntent(resultPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}