package com.louis.app.cavity.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = timestamp

        return formatter.format(calendar.time)
    }
}
