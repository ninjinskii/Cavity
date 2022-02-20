package com.louis.app.cavity.util

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private const val DAY_IN_MILLIS = 1000 * 60 * 60 * 24
    private const val HOUR_IN_MILLIS = 1000 * 60 * 60L
    private const val MINUTE_IN_MILLIS = 1000 * 60L

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

    fun formatTime(hour: Int, minute: Int, pattern: String = "hh:mm"): String {
        val formatter = SimpleDateFormat(pattern, Locale.FRENCH)
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = hour * HOUR_IN_MILLIS + minute * MINUTE_IN_MILLIS

        return formatter.format(calendar.time)
    }

    fun roundToDay(timestamp: Long): Long {
        val exceed = timestamp % DAY_IN_MILLIS
        return timestamp - exceed
    }

    fun getCurrentYear(): Pair<Long, Long> {
        return Calendar.getInstance().run {
            time = Date(System.currentTimeMillis())
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)

            val start = timeInMillis

            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)

            val end = timeInMillis

            start to end
        }
    }

    fun isToday(timestamp: Long?): Boolean {
        val currentTime = System.currentTimeMillis()
        val startOfDay = roundToDay(currentTime)
        val endOfDay = startOfDay + DAY_IN_MILLIS - 1 // -1 to not overlap next day

        return timestamp in startOfDay..endOfDay
    }
}
