package io.anonero.di

import android.content.Context
import android.content.SharedPreferences
import io.anonero.services.AnonWalletHandler
import io.anonero.services.WalletRepo
import io.anonero.ui.onboard.OnboardViewModel
import io.anonero.ui.viewmodels.AppViewModel
import io.anonero.util.WALLET_PREFERENCES
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module


private fun provideWalletSharedPrefs(app: Context): SharedPreferences =
    app.getSharedPreferences("anonPref", Context.MODE_PRIVATE)

val appModule = module {
    single(named(WALLET_PREFERENCES)) { provideWalletSharedPrefs(androidApplication()) }
    single { WalletRepo() }
    single { AnonWalletHandler(get(named(WALLET_PREFERENCES)), get()) }
    single {
        AppViewModel(get())
    }
    viewModel { OnboardViewModel(get(named(WALLET_PREFERENCES))) }
}