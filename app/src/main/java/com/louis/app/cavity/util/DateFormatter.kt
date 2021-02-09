package com.louis.app.cavity.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    fun formatDate(timestamp: Long?): String {
        return if (timestamp != null && timestamp > 0L) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = timestamp

            formatter.format(calendar.time)
        } else {
            ""
        }
    }
}
