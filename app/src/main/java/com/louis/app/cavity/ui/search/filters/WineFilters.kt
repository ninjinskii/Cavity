package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.relation.WineWithBottles
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.toBoolean
import java.time.LocalDate
import java.util.*

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.bottles.any { bottle -> bottle.isReadyToDrink() } }
    }
}

class FilterRed : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.color == WineColor.COLOR_RED.colorInt }
    }
}

class FilterWhite : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.color == WineColor.COLOR_WHITE.colorInt }
    }
}

class FilterSweet : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.color == WineColor.COLOR_SWEET.colorInt }
    }
}

class FilterRose : WineFilter{
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.color == WineColor.COLOR_ROSE.colorInt }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val countyId: Long) : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines.filter { it.wine.countyId == countyId }
    }
}

//class FilterDate(private val date: Date, private val searchBefore: Boolean) : WineFilter {
//    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
//        return wines.filter { it.bottles.any { bottle -> LocalDate.parse(bottle.buyDate).after(date) } }
//    }
//}

class NoFilter : WineFilter {
    override fun meetFilters(wines: List<WineWithBottles>): List<WineWithBottles> {
        return wines
    }
}
