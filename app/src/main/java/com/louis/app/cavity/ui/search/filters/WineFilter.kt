package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.WineWithBottles

interface WineFilter {
    fun meetFilters(wines: List<WineWithBottles>) : List<WineWithBottles>
}
