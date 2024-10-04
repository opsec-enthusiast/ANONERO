package io.anonero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import io.anonero.di.appModule
import io.anonero.model.WalletManager
import io.anonero.services.AnonNeroService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

const val FOREGROUND_CHANNEL = "anon_foreground"

class AnonApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initConfigs()
        AnonConfig.context = this
        startKoin {
            androidContext(this@AnonApplication)
            modules(appModule)
        }
    }

    private fun initConfigs() {
        //initialize wallet manager
        WalletManager.instance?.init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initNotificationChannels()
        }
        Intent(this.applicationContext,AnonNeroService::class.java).apply {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificationChannels() {
        val foregroundChannel = NotificationChannel(
            FOREGROUND_CHANNEL,
            "AnonNero Service",
            NotificationManager.IMPORTANCE_HIGH,
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(foregroundChannel)
    }

    override fun onTerminate() {
        Intent(applicationContext, AnonNeroService::class.java)
            .also {
                it.action = "stop"
                startService(it)
            }
        super.onTerminate()
    }
}
