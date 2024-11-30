package io.anonero.services

import android.util.Log
import io.anonero.model.BalanceInfo
import io.anonero.model.Subaddress
import io.anonero.model.TransactionInfo
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.ui.util.getAllUsedSubAddresses
import io.anonero.ui.util.getLatestSubAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class SyncProgress(val progress:Float,val left:Long)
class WalletRepo {
    private val _wallet = MutableStateFlow<Wallet?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _transactions = MutableStateFlow<List<TransactionInfo>>(listOf())
    private val _subAddresses = MutableStateFlow<List<Subaddress>>(listOf())
    private val _balanceInfo = MutableStateFlow<Long?>(null)
    private val _unLockedBalance = MutableStateFlow<Long?>(null)
    private val _walletStatus = MutableStateFlow<Wallet.Status?>(null)
    private val _nextAddress = MutableStateFlow<Subaddress?>(null)
    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)

    val transactions: Flow<List<TransactionInfo>> = _transactions

    val balanceInfo: Flow<Long?> = _balanceInfo
    val unLockedBalance: Flow<Long?> = _unLockedBalance
    val isLoading: Flow<Boolean> = _isLoading

    val walletStatus: Flow<Wallet.Status?> = _walletStatus
    val syncProgress: Flow<SyncProgress?> = _syncProgress

    val nextAddress: Flow<Subaddress?> = _nextAddress
    val subAddresses: Flow<List<Subaddress>> = _subAddresses

    val walletConnectionStatus: Flow<Wallet.ConnectionStatus?> = _walletStatus.map {
        it?.connectionStatus
    }


    fun update() {
        getWallet?.let { wallet ->
            if (wallet.isInitialized) {
                _balanceInfo.update { wallet.balance }
                _unLockedBalance.update { wallet.unlockedBalance }
                _walletStatus.update { wallet.fullStatus }
            }
            _transactions.update { (wallet.history?.all?.sortedWith(comparator = { o1, o2 -> o2.timestamp.compareTo(o1.timestamp) })?: listOf()) }
            _nextAddress.update { (wallet.getLatestSubAddress()) }
            _subAddresses.update { (wallet.getAllUsedSubAddresses().reversed()) }
        }
    }

    fun setLoading(b: Boolean) {
         this._isLoading.update { b }
    }

    fun syncUpdate(syncProgress: SyncProgress) {
        _syncProgress.update { syncProgress }
    }

    private val getWallet get() = WalletManager.instance?.wallet
}