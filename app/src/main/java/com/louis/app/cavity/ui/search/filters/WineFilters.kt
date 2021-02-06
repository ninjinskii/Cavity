package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.bottle.BottleAndWineWithQGrapesAndFReviews
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.toBoolean

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.isReadyToDrink() }
    }
}

class FilterColor(private val color: WineColor) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.wine.color == Wine.wineColorToColorNumber(color) }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.wine.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val countyId: Long) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.wine.countyId == countyId }
    }
}

class FilterDate(private val beyond: Long?, private val until: Long?) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        val from = beyond ?: 0L
        val to = until ?: Long.MAX_VALUE

        return bottlesAndWine.filter { it.bottle.buyDate in (from + 1) until to }
    }
}

class FilterText(private val query: String) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter {
            with(it.bottle) {
                val slug =
                    it.wine.name + it.wine.naming +
                            it.wine.cuvee + buyLocation + otherInfo + tasteComment

                return@filter slug.contains(query, ignoreCase = true)
            }
        }
    }
}

class FilterPrice(private val minPrice: Int, private val maxPrice: Int) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return when {
            minPrice == maxPrice && maxPrice != 0 ->
                bottlesAndWine.filter { it.bottle.price > maxPrice }
            else -> bottlesAndWine.filter { it.bottle.price.toInt() in minPrice..maxPrice }
        }
    }
}

class FilterFavorite : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.bottle.isFavorite.toBoolean() }
    }
}

class FilterPdf : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.bottle.pdfPath.isNotEmpty() }
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
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter { it.bottle.vintage in minYear..maxYear }
    }
}

class FilterGrape(private val grapeId: Long) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter {
            it.qGrapesIds.contains(grapeId)
        }
    }
}

class FilterReview(private val reviewId: Long) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine.filter {
            it.fReviewsIds.contains(reviewId)
        }
    }
}

object NoFilter : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWineWithQGrapesAndFReviews>):
            List<BottleAndWineWithQGrapesAndFReviews> {
        return bottlesAndWine
    }
}
