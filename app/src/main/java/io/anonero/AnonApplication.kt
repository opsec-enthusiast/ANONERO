package io.anonero

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import io.anonero.di.appModule
import io.anonero.model.WalletManager
import io.anonero.store.LogRepository
import io.anonero.ui.util.AnonLogTree
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.io.File


const val FOREGROUND_CHANNEL = "anon_foreground"

class AnonApplication : Application() {

    lateinit var anonLogTree: AnonLogTree

    private val TAG = "AnonApplication"
    override fun onCreate() {
        super.onCreate()
        AnonConfig.context = this
        startKoin {
            androidContext(this@AnonApplication)
            modules(appModule)
        }
        initConfigs()
        plantLog()

    }

    private fun plantLog() {

        anonLogTree = AnonLogTree(AnonConfig.getLogFile(applicationContext), get<LogRepository>())

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            Timber.plant(anonLogTree)
        } else {
            Timber.plant(anonLogTree)
        }
        Timber.tag(TAG).i("BUILD_TYPE: %s", BuildConfig.BUILD_TYPE)
        Timber.tag(TAG).i("FLAVOR: %s", BuildConfig.FLAVOR)
        Timber.tag(TAG).i("APPLICATION_ID: %s", BuildConfig.APPLICATION_ID)
        Timber.tag(TAG)
            .i("VERSION: ${BuildConfig.VERSION_NAME} ${BuildConfig.VERSION_CODE}\n\n")
    }

    private fun initConfigs() {
        //initialize wallet manager
        WalletManager.instance?.init()
        initNotificationChannels()
    }

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
        super.onTerminate()
        anonLogTree.cleanup()
    }
}
