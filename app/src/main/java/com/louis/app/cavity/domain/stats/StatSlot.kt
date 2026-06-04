package com.louis.app.cavity.domain.stats

import com.louis.app.cavity.model.WineColor
import kotlinx.coroutines.flow.Flow

enum class StatSlot(
    val groupBy: String,
    val stockQuery: (StatsQueries) -> Flow<List<Stat>>,
    val postProcess: (List<BaseStat>) -> List<Stat> = { it }
) {
    COUNTY(
        groupBy = "county.name",
        stockQuery = { it.getStockByCounty() }
    ),

    COLOR(
        groupBy = "wine.color",
        stockQuery = { it.getStockByColor() },
        postProcess = { stats ->
            stats.map {
                WineColorStat(
                    WineColor.valueOf(it.label),
                    it.count,
                    it.percentage,
                    it.bottleIds
                )
            }
        }
    ),

    VINTAGE(
        groupBy = "bottle.vintage",
        stockQuery = { it.getStockByVintage() }
    ),

    NAMING(
        groupBy = "wine.naming",
        stockQuery = { it.getStockByNaming() }
    )
}
