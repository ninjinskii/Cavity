package com.louis.app.cavity.domain.stats

import kotlinx.coroutines.flow.Flow

interface StatsQueries {
    fun getStockByCounty(): Flow<List<BaseStat>>
    fun getStockByColor(): Flow<List<WineColorStat>>
    fun getStockByVintage(): Flow<List<BaseStat>>
    fun getStockByNaming(): Flow<List<BaseStat>>
    fun getBottleStatsForCounty(countyId: Long, storageLocation: String?): Flow<List<BottleStatRow>>
    fun getStatsByHistoryEntry(
        start: Long,
        end: Long,
        types: List<Int>,
        groupByColumn: String
    ): Flow<List<BaseStat>>
}
