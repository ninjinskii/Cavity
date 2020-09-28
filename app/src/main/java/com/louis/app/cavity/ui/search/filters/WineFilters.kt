package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.toBoolean

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isReadyToDrink() }
    }
}

class FilterRed : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == WineColor.COLOR_RED.colorInt }
    }
}

class FilterWhite : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == WineColor.COLOR_WHITE.colorInt }
    }
}

class FilterSweet : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == WineColor.COLOR_SWEET.colorInt }
    }
}

class FilterRose : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == WineColor.COLOR_ROSE.colorInt }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val countyId: Long) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.countyId == countyId }
    }
}

class FilterDate(private val date: Long, private val searchBefore: Boolean) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return if (searchBefore)
            bottlesAndWine.filter { it.buyDate < date }
        else
            bottlesAndWine.filter { date in 0L..it.buyDate }

    }
}

class NoFilter : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine
    }
}
