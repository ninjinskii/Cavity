package com.louis.app.cavity.db

import androidx.room.TypeConverter
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.model.WineColor

class Converters {
    @TypeConverter
    fun numberToHistoryType(value: Int) = when (value) {
        0 -> HistoryEntryType.TYPE_CONSUME
        1 -> HistoryEntryType.TYPE_REPLENISHMENT
        2 -> HistoryEntryType.TYPE_GIFTED_TO
        3 -> HistoryEntryType.TYPE_GIFTED_BY
        4 -> HistoryEntryType.TYPE_TASTING
        else -> throw IllegalStateException("Unknown history entry type $value")
    }

    @TypeConverter
    fun historyTypeToNumber(value: HistoryEntryType) = when (value) {
        HistoryEntryType.TYPE_CONSUME -> 0
        HistoryEntryType.TYPE_REPLENISHMENT -> 1
        HistoryEntryType.TYPE_GIFTED_TO -> 2
        HistoryEntryType.TYPE_GIFTED_BY -> 3
        HistoryEntryType.TYPE_TASTING -> 4
    }

    @TypeConverter
    fun numberToWineColor(value: Int) = when(value) {
        0 -> WineColor.WINE_RED
        1 -> WineColor.WINE_WHITE
        2 -> WineColor.WINE_SWEET
        3 -> WineColor.WINE_ROSE
        else -> throw IllegalStateException("Unknown wine color $value")
    }

    @TypeConverter
    fun wineColorToNumber(value: WineColor) = when(value) {
        WineColor.WINE_RED -> 0
        WineColor.WINE_WHITE -> 1
        WineColor.WINE_SWEET -> 2
        WineColor.WINE_ROSE -> 3
    }
}
