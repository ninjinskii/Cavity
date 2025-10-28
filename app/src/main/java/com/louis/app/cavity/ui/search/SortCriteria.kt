package com.louis.app.cavity.ui.search

import com.louis.app.cavity.R

enum class SortCriteria(val value: Int) {
    VINTAGE(R.string.vintage),
    NAMING(R.string.naming),
    NAME(R.string.name),
    BUY_DATE(R.string.buying_date),
    PRICE(R.string.price),
    NONE(R.string.no_sort)
}

data class Sort(val criteria: SortCriteria, val reversed: Boolean = false)
