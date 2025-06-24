package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.domain.history.toInt
import com.louis.app.cavity.model.*
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.isReadyToDrink() }
    }
}

class FilterColor(private val color: WineColor) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wine.color == color }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wine.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val county: County) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.wine.countyId == county.id }
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
            val slug =
                it.wine.name + it.wine.naming + it.wine.cuvee +
                        it.bottle.buyLocation + it.bottle.otherInfo

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

class FilterSelected : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.isSelected }
    }
}

class FilterConsumed(private val consumed: Boolean) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.consumed == consumed.toInt() }
    }
}

class FilterCapacity(private val bottleSize: BottleSize) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter { it.bottle.bottleSize == bottleSize }
    }
}

class FilterFriend(private val friendId: Long, private val historyEntryType: Int) : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle.filter {
            return@filter it.historyEntriesWithFriends.find { entryWithFriends ->
                entryWithFriends.historyEntry.type.toInt() == historyEntryType &&
                        (friendId in entryWithFriends.friends.map { f -> f.id })
            } != null
        }
    }
}

object NoFilter : WineFilter {
    override fun meetFilters(boundedBottle: List<BoundedBottle>): List<BoundedBottle> {
        return boundedBottle
    }
}
