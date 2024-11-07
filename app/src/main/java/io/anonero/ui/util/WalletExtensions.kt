package io.anonero.ui.util

import io.anonero.model.Subaddress
import io.anonero.model.Wallet

fun Wallet.getLastUnusedIndex(): Int {
    var lastUsedSubaddress = 0
    val subaddress = arrayListOf<Subaddress>()
    for (i in 0 until this.numSubaddresses) {
        subaddress.add(this.getSubaddressObject(i))
    }
    subaddress.forEach {
        //Skip primary and find unused subaddress
        if (it.totalAmount == 0L && subaddress.indexOf(it) != 0) {
            return subaddress.indexOf(it)
        }
    }
    this.history?.let {
        for (info in it.all) {
            if (info.addressIndex > lastUsedSubaddress) lastUsedSubaddress = info.addressIndex
        }
    }
    return lastUsedSubaddress
}


fun Wallet.getLatestSubAddress(): Subaddress {
    val lastUsedSubAddress = getLastUnusedIndex()
    val address = this.getSubaddressObject(lastUsedSubAddress + 1)
    if (lastUsedSubAddress == this.numSubaddresses) {
        this.addSubaddress(getAccountIndex(), "Subaddress #${address.addressIndex}")
    }
    return address
}


fun Wallet.getAllUsedSubAddresses(): ArrayList<Subaddress> {
    val addresses = arrayListOf<Subaddress>()
    for (i in 0..this.numSubaddresses) {
        addresses.add(this.getSubaddressObject(i))
    }
    return addresses
}
