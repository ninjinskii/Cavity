package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.db.dao.BoundedBottle

class And(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        val firstFilter = filter.meetFilters(boundedBottle)
        return otherFilter.meetFilters(firstFilter)
    }
}
