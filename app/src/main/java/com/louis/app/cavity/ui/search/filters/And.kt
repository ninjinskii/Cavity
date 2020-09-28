package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.BottleAndWine

class And(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        val firstFilter = filter.meetFilters(bottlesAndWine)
        return otherFilter.meetFilters(firstFilter)
    }
}
