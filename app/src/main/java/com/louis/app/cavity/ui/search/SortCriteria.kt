package com.louis.app.cavity.ui.search

import com.louis.app.cavity.R
import com.louis.app.cavity.db.dao.BoundedBottle

enum class SortCriteria(val value: Int, val selector: (BoundedBottle) -> Comparable<*>?) {
    VINTAGE(R.string.vintage, { it.bottle.vintage }),
    APOGEE(R.string.apogee, { it.bottle.apogee }),
    NAMING(R.string.naming, { it.wine.naming }),
    NAME(R.string.name, { it.wine.name }),
    BUY_DATE(R.string.buying_date, { it.bottle.buyDate }),
    PRICE(R.string.price, { it.bottle.price.takeIf { price -> price != -1f } }),
    NONE(R.string.no_sort, { null })
}

data class Sort(val criteria: SortCriteria, val reversed: Boolean = false)
