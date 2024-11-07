package io.anonero.di

import android.content.Context
import android.content.SharedPreferences
import io.anonero.services.AnonWalletHandler
import io.anonero.services.WalletRepo
import io.anonero.ui.viewmodels.AppViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module


private fun provideWalletSharedPrefs(app: Context): SharedPreferences =
    app.getSharedPreferences("anonPref", Context.MODE_PRIVATE)

val appModule = module {

    single(named("walletPref")) { provideWalletSharedPrefs(androidApplication()) }
    single { WalletRepo() }
    single { AnonWalletHandler(get(named("walletPref")), get()) }
    single {
        AppViewModel(get())
    }
}