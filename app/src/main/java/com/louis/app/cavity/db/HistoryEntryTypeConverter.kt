package com.louis.app.cavity.db

import androidx.room.TypeConverter
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.fromInt
import com.louis.app.cavity.domain.history.toInt
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class HistoryEntryTypeConverter {
    @TypeConverter
    fun historyEntryTypeToInt(type: HistoryEntryType): Int {
        return type.toInt()
    }

    @TypeConverter
    fun intToHistoryEntryType(intType: Int): HistoryEntryType {
        return fromInt(intType)
    }
}

class HistoryEntryTypeAdapter {
    @FromJson
    fun fromJson(entry: Int): HistoryEntryType {
        return fromInt(entry)
    }

    @ToJson
    fun toJson(entry: HistoryEntryType): Int {
        return entry.toInt()
    }
}
