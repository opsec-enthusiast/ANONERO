package io.anonero.util

import io.anonero.AnonConfig
import java.math.BigDecimal
import java.math.RoundingMode

object Formats {

    fun getDisplayAmount(amount: Long, maxDecimals: Int): String {

        // a Java bug does not strip zeros properly if the value is 0
        if (amount == 0L) return "0.00"
        var d = BigDecimal(amount).scaleByPowerOfTen(-AnonConfig.XMR_DECIMALS)
            .setScale(maxDecimals, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        if (d.scale() < 2) d = d.setScale(2, RoundingMode.UNNECESSARY)
        return d.toPlainString()
    }

}