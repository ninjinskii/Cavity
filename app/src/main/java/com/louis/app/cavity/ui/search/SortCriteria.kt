package com.louis.app.cavity.ui.search

enum class SortCriteria {
    NONE,
    VINTAGE,
    NAMING,
    NAME,
    BUY_DATE,
    PRICE
}

data class Sort(val criteria: SortCriteria, val reversed: Boolean = false)
