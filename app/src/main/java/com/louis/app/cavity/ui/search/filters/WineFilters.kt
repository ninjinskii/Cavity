package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.model.relation.bottle.BoundedBottle
import com.louis.app.cavity.util.toBoolean

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.isReadyToDrink() }
    }
}

class FilterColor(private val color: Int) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wineAndNaming.wine.color == color }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wineAndNaming.wine.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val countyId: Long) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wineAndNaming.naming.countyId == countyId }
    }
}

class FilterDate(private val beyond: Long?, private val until: Long?) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        val from = beyond ?: 0L
        val to = until ?: Long.MAX_VALUE

        return boundedBottle.filter { it.bottle.buyDate in (from + 1) until to }
    }
}

class FilterText(private val query: String) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter {
            val (wine, naming) = it.wineAndNaming
            val slug =
                wine.name + naming.naming + wine.cuvee +
                    it.bottle.buyLocation + it.bottle.otherInfo + it.bottle.tasteComment

            return@filter slug.contains(query, ignoreCase = true)
        }
    }
}

class FilterPrice(private val minPrice: Int, private val maxPrice: Int) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return when {
            minPrice == maxPrice && maxPrice != 0 ->
                boundedBottle.filter { it.bottle.price > maxPrice }
            else -> boundedBottle.filter { it.bottle.price.toInt() in minPrice..maxPrice }
        }
    }
}

class FilterFavorite : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.isFavorite.toBoolean() }
    }
}

class FilterPdf : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.pdfPath.isNotEmpty() }
    }
}

// Might be irrelevant
//class FilterStock(private val minStock: Int, private val maxStock: Int) : WineFilter {
//    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
//            List<BottleAndWineWithQGrapesAndFReviews> {
//        return bottlesAndWine.filter { it.count in minStock..maxStock }
//    }
//}

class FilterVintage(private val minYear: Int, private val maxYear: Int) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.vintage in minYear..maxYear }
    }
}

class FilterGrape(private val grape: Grape) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.grapes.contains(grape) }
    }
}

class FilterReview(private val review: Review) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.reviews.contains(review) }
    }
}

object NoFilter : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle
    }
}
