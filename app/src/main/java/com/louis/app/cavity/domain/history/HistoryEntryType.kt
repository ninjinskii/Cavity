package com.louis.app.cavity.domain.history

// Replenishment is everything that could be considered as an added bottle, either given by a friend,
// or user buy. Consumptions are the opposite: gifted to a friend, drunk bottle, in tasting or not.
enum class HistoryEntryType(val intValue: Int, val isReplenishment: Boolean) {
    REMOVE(0, false),
    ADD(1, true),
    GIFTED_TO(2, false),
    GIVEN_BY(3, true),
    TASTING(4, false)
}

fun fromInt(typeInt: Int): HistoryEntryType =
    HistoryEntryType.entries.firstOrNull { it.intValue == typeInt }
        ?: error("Unknown HistoryEntryType value=$typeInt")

fun HistoryEntryType.isConsumption(): Boolean {
    return !this.isReplenishment
}

fun getReplenishmentTypes(includeGifts: Boolean): List<HistoryEntryType> =
    listOfNotNull(
        HistoryEntryType.ADD,
        HistoryEntryType.GIVEN_BY.takeIf { includeGifts }
    )

fun getConsumptionTypes(includeGifts: Boolean): List<HistoryEntryType> =
    listOfNotNull(
        HistoryEntryType.REMOVE,
        HistoryEntryType.TASTING,
        HistoryEntryType.GIFTED_TO.takeIf { includeGifts }
    )
