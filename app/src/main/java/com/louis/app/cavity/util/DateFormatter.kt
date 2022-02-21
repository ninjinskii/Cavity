package com.louis.app.cavity.util

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private const val DAY_IN_MILLIS = 1000 * 60 * 60 * 24

    fun formatDate(timestamp: Long?, pattern: String = "dd MMM yyyy"): String {
        return if (timestamp != null && timestamp > 0L) {
            val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
            val formatter = SimpleDateFormat(pattern, locale)
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = timestamp

            formatter.format(calendar.time)
        } else {
            ""
        }
    }

    fun roundToDay(timestamp: Long): Long {
        val exceed = timestamp % DAY_IN_MILLIS
        return timestamp - exceed
    }

    fun isToday(timestamp: Long?): Boolean {
        val currentTime = System.currentTimeMillis()
        val startOfDay = roundToDay(currentTime)
        val endOfDay = startOfDay + DAY_IN_MILLIS - 1 // -1 to not overlap next day

        return timestamp in startOfDay..endOfDay
    }
}
