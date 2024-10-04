package io.anonero.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.anonero.FOREGROUND_CHANNEL
import io.anonero.R
import io.anonero.ui.walletSyncFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch

const val NOTIFICATION_ID = 2

class AnonNeroService : Service() {

    private val TAG: String = AnonNeroService::class.java.simpleName
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "start") {
            start()
        }
        if (intent?.action == "stop") {
            stop()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    foregroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                );
            } else {
                startForeground(NOTIFICATION_ID, foregroundNotification());
            }
        }
        return START_STICKY
    }

    private fun start() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        startForeground(6, foregroundNotification())
        scope.launch {
            walletSyncFlow
                .collect {
                    mNotificationManager.notify(NOTIFICATION_ID, foregroundNotification(content = it))
                }
        }
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun foregroundNotification(
        content: String = "Deamon Service Running....",
        title: String = " "
    ): Notification {
        return NotificationCompat.Builder(this.applicationContext, FOREGROUND_CHANNEL)
            .setSmallIcon(R.drawable.baseline_content_copy_24)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setContentText(content)
            .setGroup("BackgroundService")
            .build()

    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: Called")
        job.cancel()
        super.onDestroy()
    }
}