/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.anonero.services

import android.util.Log
import io.anonero.model.PendingTransaction
import io.anonero.model.Wallet
import io.anonero.model.WalletListener
import java.security.SecureRandom

/**
 * Handy class for starting a new thread that has a looper. The looper can then be
 * used to create handler classes. Note that start() must still be called.
 * The started Thread has a stck size of STACK_SIZE (=5MB)
 */
class MoneroHandlerThread(name: String, val listener: Listener?, private val wallet: Wallet) :
    Thread(null, null, name, THREAD_STACK_SIZE), WalletListener {

    @Synchronized
    override fun start() {
        super.start()
        listener?.onRefresh(false)
    }

    override fun run() {

    }

    override fun moneySpent(txId: String?, amount: Long) {

    }
    override fun moneyReceived(txId: String?, amount: Long) {
        Log.i(name, "moneyReceived: $amount")
    }
    override fun unconfirmedMoneyReceived(txId: String?, amount: Long) {}
    override fun newBlock(height: Long) {
        refresh(false)
        val newHeight =    if (wallet.isSynchronized) height else 0 // when 0 it fetches from C++
        Log.i("moneroHandler", "newBlock: $height")
        listener?.onNewBlockFound(height);
    }

    override fun updated() {
        refresh(false)
        Log.i(name, "updated()")
    }

    override fun refreshed() {
        val status = wallet.fullStatus.connectionStatus
        val daemonHeight = wallet.getDaemonBlockChainHeight()
        val chainHeight = wallet.getBlockChainHeight()
        /// height
        Log.i(name, "refreshed() status: $status daemonHeight: $daemonHeight chainHeight: $chainHeight isSynchronized:${wallet.isSynchronized}  connectionStatus:${wallet.status.connectionStatus}")
        if (status === Wallet.ConnectionStatus.ConnectionStatus_Disconnected || status == null) {
            tryRestartConnection()
        } else {
            val heightDiff = daemonHeight - chainHeight
            if (heightDiff >= 2) {
                tryRestartConnection()
            } else {
                wallet.setSynchronized()
                wallet.store()
                refresh(true)
            }

        }
    }

    private fun tryRestartConnection() {
        Log.d("MoneroHandlerThread.kt", "refreshed() Starting connection retry")
        listener?.onConnectionFail()
        wallet.init(0)
        wallet.startRefresh()
    }

    private fun refresh(walletSynced: Boolean) {
        wallet.refreshHistory()
        if (walletSynced) {
            wallet.refreshCoins()
        }
        listener?.onRefresh(walletSynced)
    }

    fun sendTx(pendingTx: PendingTransaction): Boolean {
        return pendingTx.commit("", true)
    }

    interface Listener {
        fun onRefresh(walletSynced: Boolean)
        fun onConnectionFail()
        fun onNewBlockFound(block:Long)
    }

    companion object {
        // from src/cryptonote_config.h
        const val THREAD_STACK_SIZE = (5 * 1024 * 1024).toLong()
    }
}