package com.louis.app.cavity.domain.stats

import com.louis.app.cavity.R
import com.louis.app.cavity.domain.history.getConsumptionTypes
import com.louis.app.cavity.domain.history.getReplenishmentTypes

sealed class InventoryStatFilter {
    abstract fun retainHistoryEntryTypes(includeGifts: Boolean): List<Int>
    abstract val buttonId: Int
    abstract val supportsYearFiltering: Boolean

    object Stock : InventoryStatFilter() {
        override fun retainHistoryEntryTypes(includeGifts: Boolean) = emptyList<Int>()
        override val buttonId = R.id.buttonStock
        override val supportsYearFiltering = false
    }

    object Replenishments : InventoryStatFilter() {
        override fun retainHistoryEntryTypes(includeGifts: Boolean) =
            getReplenishmentTypes(includeGifts).map { it.intValue }

        override val buttonId = R.id.buttonReplenishments
        override val supportsYearFiltering = true

    }

    object Consumptions : InventoryStatFilter() {
        override fun retainHistoryEntryTypes(includeGifts: Boolean) =
            getConsumptionTypes(includeGifts).map { it.intValue }

        override val buttonId = R.id.buttonConsumptions
        override val supportsYearFiltering = true
    }

    companion object {
        fun fromButtonId(id: Int): InventoryStatFilter =
            listOf(Stock, Replenishments, Consumptions)
                .firstOrNull { it.buttonId == id }
                ?: error("Unknown buttonId=$id")
    }
}
