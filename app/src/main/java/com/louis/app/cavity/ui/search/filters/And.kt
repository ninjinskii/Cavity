package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.BottleAndWineWithQGrapesAndFReviews

class And(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        val firstFilter = filter.meetFilters(bottlesAndWine)
        return otherFilter.meetFilters(firstFilter)
    }
}
