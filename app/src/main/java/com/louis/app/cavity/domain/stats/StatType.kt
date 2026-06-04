package com.louis.app.cavity.domain.stats

import androidx.annotation.LayoutRes
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.toInt

enum class StatType(
    val buildTypes: (includeGifts: Boolean) -> List<Int>,
    @param:LayoutRes val buttonId: Int,
    val supportsYearSelection: Boolean
) {
    STOCK(
        { emptyList() },
        R.id.buttonStock,
        supportsYearSelection = false
    ),

    REPLENISHMENTS(
        { includeGifts ->
            mutableListOf(HistoryEntryType.ADD.toInt()).apply {
                if (includeGifts) {
                    add(HistoryEntryType.GIVEN_BY.toInt())
                }
            }
        },
        R.id.buttonReplenishments,
        supportsYearSelection = true
    ),

    CONSUMPTIONS(
        { includeGifts ->
            mutableListOf(
                HistoryEntryType.REMOVE.toInt(),
                HistoryEntryType.TASTING.toInt()
            ).apply {
                if (includeGifts) {
                    add(HistoryEntryType.GIFTED_TO.toInt())
                }
            }
        }, R.id.buttonConsumptions,
        supportsYearSelection = true
    )
}

fun fromButtonId(buttonId: Int) = StatType.entries.first { it.buttonId == buttonId }
