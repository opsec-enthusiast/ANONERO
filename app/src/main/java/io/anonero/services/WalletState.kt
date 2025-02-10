package io.anonero.services

import io.anonero.model.Subaddress
import io.anonero.model.TransactionInfo
import io.anonero.model.Wallet
import io.anonero.model.WalletManager
import io.anonero.model.node.DaemonInfo
import io.anonero.ui.util.getAllUsedSubAddresses
import io.anonero.ui.util.getLatestSubAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class SyncProgress(val progress: Float, val left: Long)

private const val TAG = "WalletState"

class WalletState {
    private val _wallet = MutableStateFlow<Wallet?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _transactions = MutableStateFlow<List<TransactionInfo>>(listOf())
    private val _subAddresses = MutableStateFlow<List<Subaddress>>(listOf())
    private val _balanceInfo = MutableStateFlow<Long?>(null)
    private val _unLockedBalance = MutableStateFlow<Long?>(null)
    private val _walletStatus = MutableStateFlow<Wallet.Status?>(null)
    private val _nextAddress = MutableStateFlow<Subaddress?>(null)
    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    private val _connectedDaemon = MutableStateFlow<DaemonInfo?>(null)

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

    val daemonInfo: Flow<DaemonInfo?> = _connectedDaemon

    fun update() {
        getWallet?.let { wallet ->
            if (wallet.isInitialized) {
                _balanceInfo.update { wallet.balance }
                _unLockedBalance.update { wallet.unlockedBalance }
                _walletStatus.update { wallet.fullStatus }
                val address = try {
                    WalletManager.instance?.getDaemonAddress()
                } catch (_: Exception) {
                    null
                }
                address?.let {
                    _connectedDaemon.update {
                        DaemonInfo(
                            address,
                            connectionStatus = wallet.fullStatus.connectionStatus
                                ?: Wallet.ConnectionStatus.ConnectionStatus_Disconnected,
                            WalletManager.instance?.getBlockchainHeight() ?: -1L
                        )
                    }
                }
            }
            _transactions.update {
                (wallet.history?.all?.sortedWith(comparator = { o1, o2 ->
                    o2.timestamp.compareTo(
                        o1.timestamp
                    )
                }) ?: listOf())
            }
            _nextAddress.update { (wallet.getLatestSubAddress()) }
            _subAddresses.update { (wallet.getAllUsedSubAddresses().reversed()) }

        }
    }

    fun setLoading(b: Boolean) {
        this._isLoading.update { b }
    }

    fun updateDaemon(daemonInfo: DaemonInfo) {
        this._connectedDaemon.update { daemonInfo }
    }

    fun syncUpdate(syncProgress: SyncProgress) {
        _syncProgress.update { syncProgress }
    }

    private val getWallet get() = WalletManager.instance?.wallet
}