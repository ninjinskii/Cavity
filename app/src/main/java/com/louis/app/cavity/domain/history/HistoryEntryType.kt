package com.louis.app.cavity.domain.history

// Replenishment is everything that could be considered as an added bottle, either given by a friend,
// or user buy. Consumptions are the opposite: gifted to a friend, drunk bottle, in tasting or not.
enum class HistoryEntryType(val value: Int) {
    REMOVE(0),
    ADD(1),
    GIFTED_TO(2),
    GIVEN_BY(3),
    TASTING(4)
}

fun HistoryEntryType.toInt() = value

fun fromInt(intType: Int): HistoryEntryType {
    return when (intType) {
        0 -> HistoryEntryType.REMOVE
        1 -> HistoryEntryType.ADD
        2 -> HistoryEntryType.GIFTED_TO
        3 -> HistoryEntryType.GIVEN_BY
        /* 4 */ else -> HistoryEntryType.TASTING
    }
}

fun HistoryEntryType.isReplenishment(): Boolean {
    return when (this.toInt()) {
        HistoryEntryType.ADD.value, HistoryEntryType.GIVEN_BY.value -> true

        HistoryEntryType.REMOVE.value,
        HistoryEntryType.GIFTED_TO.value,
        HistoryEntryType.TASTING.value -> false

        else -> throw Error("Unknown history entry type ${this.toInt()}")
    }
}

fun HistoryEntryType.isConsumption(): Boolean {
    return !isReplenishment()
}
