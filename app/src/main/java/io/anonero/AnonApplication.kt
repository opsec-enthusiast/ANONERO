package io.anonero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.anonero.di.appModule
import io.anonero.model.WalletManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

const val FOREGROUND_CHANNEL = "anon_foreground"

class AnonApplication : Application() {

    private val TAG = "AnonApplication"
    override fun onCreate() {
        super.onCreate()
        initConfigs()
        AnonConfig.context = this
//        AnonConfig.getDefaultWalletDir(this).deleteRecursively()
        startKoin {
            androidContext(this@AnonApplication)
            modules(appModule)
        }
        Log.i(TAG, "BUILD_TYPE: ${BuildConfig.BUILD_TYPE}")
        Log.i(TAG, "FLAVOR: ${BuildConfig.FLAVOR}")
        Log.i(TAG, "APPLICATION_ID: ${BuildConfig.APPLICATION_ID}")
        Log.i(TAG, "VERSION: ${BuildConfig.VERSION_NAME} ${BuildConfig.VERSION_CODE}\n\n\n")
    }

    private fun initConfigs() {
        //initialize wallet manager
        WalletManager.instance?.init()
        initNotificationChannels()
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

}
