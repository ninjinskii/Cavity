package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.bottle.BottleAndWineWithQGrapesAndFReviews

interface WineFilter {
    fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>)
            : List<BottleAndWineWithQGrapesAndFReviews>

    fun andCombine(filter: WineFilter): WineFilter {
        return if (this is NoFilter) filter else And(this, filter)
    }

    //  See for overriding this in child to get And filter instead of Or Filter (For wine colors)
    fun orCombine(filter: WineFilter): WineFilter {
        return if (this is NoFilter) filter else Or(this, filter)
    }
}
