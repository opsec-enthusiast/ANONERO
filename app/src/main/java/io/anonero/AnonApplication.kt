package io.anonero

import android.app.Application
import io.anonero.di.appModule
import io.anonero.model.WalletManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class AnonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initConfigs()
        startKoin {
            androidContext(this@AnonApplication)
            modules(appModule)
        }
    }

    private fun initConfigs() {
        //initialize wallet manager
        WalletManager.instance?.init();
    }
}
