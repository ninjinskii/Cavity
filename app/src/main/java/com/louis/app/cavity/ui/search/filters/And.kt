package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.WineWithBottles

class And(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        val firstFilter = filter.meetFilters(wines)
        return otherFilter.meetFilters(firstFilter)
    }
}
