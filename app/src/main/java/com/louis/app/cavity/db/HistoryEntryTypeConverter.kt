package com.louis.app.cavity.db

import androidx.room.TypeConverter
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.fromInt
import com.louis.app.cavity.domain.history.toInt

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