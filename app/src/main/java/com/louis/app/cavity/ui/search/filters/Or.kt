package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.WineWithBottles

class Or (private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        val firstFilter = filter.meetFilters(wines).toMutableList()
        val secondFilter = otherFilter.meetFilters(wines)

        secondFilter.forEach {
            if (it !in firstFilter) {
                firstFilter.add(it)
            }
        }

        return firstFilter
    }
}
