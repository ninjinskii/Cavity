package com.louis.app.cavity.domain.history

enum class HistoryEntryType(val value: Int) {
    CONSUMPTION(0),
    REPLENISHMENT(1),
    GIFTED_TO(2),
    GIVEN_BY(3),
    TASTING(4)
}

fun HistoryEntryType.toInt() = value

fun isReplenishment(type: Int): Boolean {
    return when (type) {
        HistoryEntryType.REPLENISHMENT.value,
        HistoryEntryType.GIVEN_BY.value,
        HistoryEntryType.TASTING.value -> true

        HistoryEntryType.CONSUMPTION.value, HistoryEntryType.GIFTED_TO.value -> true
        else -> throw Error("Unknown history entry type $type")
    }
}

fun isConsumption(type: Int): Boolean {
    return !isReplenishment(type)
}
