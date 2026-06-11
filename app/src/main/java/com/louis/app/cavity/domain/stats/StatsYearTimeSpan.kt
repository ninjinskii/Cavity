package com.louis.app.cavity.domain.stats

data class StatsYearTimeSpan(val year: Int, val yearStart: Long, val yearEnd: Long) {
    override fun toString() = year.toString()

    companion object {
        val ALL_YEARS = StatsYearTimeSpan(
            0,
            0L,
            System.currentTimeMillis()
        )
    }
}
