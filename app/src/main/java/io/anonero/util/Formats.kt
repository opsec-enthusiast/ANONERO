package io.anonero.util

import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import io.anonero.AnonConfig
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

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

    fun formatTransactionTime(timestamp: Long): String {
        val instant =
            Instant.ofEpochMilli(timestamp)

        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("HH:mm\nMM/dd")

        return dateTime.format(formatter)

    }
}