package io.anonero.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import io.anonero.FOREGROUND_CHANNEL
import io.anonero.R
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

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
                delay(2000)
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
                            "Synced: ${wallet.getBlockChainHeight()}"
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    mNotificationManager.notify(
                        NOTIFICATION_ID,
                        foregroundNotification(notificationMessage)
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

        val mainActivityIndent = Intent(this, MainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification", true)
            }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(mainActivityIndent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

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
            .setContentIntent(resultPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}