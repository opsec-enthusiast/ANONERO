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
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

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

        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyy")

        return dateTime.format(formatter)

    }


    fun formatTransactionDetailTime(timestamp: Long): String {
        val instant =
            Instant.ofEpochMilli(timestamp)

        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("HH:mm\nMM/dd")

        return dateTime.format(formatter)

    }


    fun formatLogTime(timestamp: Long): String {
        val instant =
            Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("MMM dd hh:mm")
        return dateTime.format(formatter)

    }

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(Locale.US,"%.2f %s", sizeInBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }

}