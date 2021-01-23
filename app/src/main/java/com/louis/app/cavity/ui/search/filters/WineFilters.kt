package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.toBoolean

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isReadyToDrink() }
    }
}

class FilterColor(private val color: WineColor) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == Wine.wineColorToColorNumber(color) }
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

class FilterDate(private val beyond: Long?, private val until: Long?) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        val from = beyond ?: 0L
        val to = until ?: Long.MAX_VALUE

        return bottlesAndWine.filter { it.buyDate in (from + 1) until to }
    }
}

class FilterText(private val query: String) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter {
            val slug =
                it.name + it.naming + it.cuvee + it.buyLocation + it.otherInfo + it.tasteComment
            return@filter slug.contains(query, ignoreCase = true)
        }
    }
}

class FilterPrice(private val minPrice: Int, private val maxPrice: Int) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return when {
            minPrice == maxPrice && maxPrice != 0 -> bottlesAndWine.filter { it.price > maxPrice }
            else -> bottlesAndWine.filter { it.price in minPrice..maxPrice }
        }
    }
}

class FilterFavorite : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isFavorite.toBoolean() }
    }
}

class FilterPdf : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.pdfPath.isNotEmpty() }
    }
}

class FilterStock(private val minStock: Int, private val maxStock: Int) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.count in minStock..maxStock }
    }
}

class FilterVintage(private val minYear: Int, private val maxYear: Int) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.vintage in minYear..maxYear }
    }
}

object NoFilter : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine
    }
}
