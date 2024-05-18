package io.anonero

import android.app.Application
import io.anonero.model.WalletManager

class AnonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initConfigs()
    }

    private fun initConfigs() {
        //initialize wallet manager
        WalletManager.instance?.init();
    }
}
