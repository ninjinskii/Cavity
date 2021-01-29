package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.BottleAndWineWithQGrapesAndFReviews

class Or(private val filter: WineFilter, private val otherFilter: WineFilter) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        val firstFilter = filter.meetFilters(bottlesAndWine).toMutableSet()
        val secondFilter = otherFilter.meetFilters(bottlesAndWine)

        firstFilter.addAll(secondFilter)

        return firstFilter.toList()
    }
}
