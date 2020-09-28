package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.BottleAndWine

interface WineFilter {
    fun meetFilters(bottlesAndWine: List<BottleAndWine>) : List<BottleAndWine>

    fun andCombine(filter: WineFilter): WineFilter {
        return if (this is NoFilter) filter else And(this, filter)
    }

    fun orCombine(filter: WineFilter): WineFilter {
        return if (this is NoFilter) filter else Or(this, filter)
    }
}
