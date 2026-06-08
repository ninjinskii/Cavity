package com.louis.app.cavity.domain.stats

data class StatsYearTimeSpan(val year: Int, val yearStart: Long, val yearEnd: Long) {
    override fun toString() = year.toString()
}
