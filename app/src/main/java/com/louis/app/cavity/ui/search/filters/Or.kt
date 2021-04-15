package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.db.dao.BoundedBottle

class Or(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        val firstFilter = filter.meetFilters(boundedBottle).toMutableSet()
        val secondFilter = otherFilter.meetFilters(boundedBottle)

        firstFilter.addAll(secondFilter)

        return firstFilter.toList()
    }
}
