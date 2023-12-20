package com.louis.app.cavity.db

import androidx.room.TypeConverter

class StatsBottleIdsTypeConverter {
    @TypeConverter
    fun stringToIds(bottleIds: String): List<Long> {
        return bottleIds.split(",").mapNotNull { it.toLongOrNull() }
    }
}
