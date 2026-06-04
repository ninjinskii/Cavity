package com.louis.app.cavity.model

import com.louis.app.cavity.domain.stats.BaseStat
import com.louis.app.cavity.domain.stats.PriceByCurrency

data class CountyDetails(
    val bottleCount: Int,
    val bottlePrices: List<PriceByCurrency>,
    val namings: List<BaseStat>,
    val vintages: List<BaseStat>
)
