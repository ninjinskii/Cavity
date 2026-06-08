package com.louis.app.cavity.domain.stats

import com.louis.app.cavity.R
import com.louis.app.cavity.domain.history.getConsumptionTypes
import com.louis.app.cavity.domain.history.getReplenishmentTypes

sealed class InventoryStatFilter {
    abstract fun getHistoryEntryTypes(includeGifts: Boolean): List<Int>
    abstract val buttonId: Int
    abstract val supportsYearFiltering: Boolean
    abstract val supportsGivenBottleFiltering: Boolean

    object Stock : InventoryStatFilter() {
        override fun getHistoryEntryTypes(includeGifts: Boolean) = emptyList<Int>()
        override val buttonId = R.id.buttonStock
        override val supportsYearFiltering = false
        override val supportsGivenBottleFiltering = false
    }

    object Replenishments : InventoryStatFilter() {
        override fun getHistoryEntryTypes(includeGifts: Boolean) =
            getReplenishmentTypes(includeGifts).map { it.intValue }

        override val buttonId = R.id.buttonReplenishments
        override val supportsYearFiltering = true
        override val supportsGivenBottleFiltering = true

    }

    object Consumptions : InventoryStatFilter() {
        override fun getHistoryEntryTypes(includeGifts: Boolean) =
            getConsumptionTypes(includeGifts).map { it.intValue }

        override val buttonId = R.id.buttonConsumptions
        override val supportsYearFiltering = true
        override val supportsGivenBottleFiltering = true
    }

    companion object {
        fun fromButtonId(id: Int): InventoryStatFilter =
            listOf(Stock, Replenishments, Consumptions)
                .firstOrNull { it.buttonId == id }
                ?: error("Unknown buttonId=$id")
    }
}
