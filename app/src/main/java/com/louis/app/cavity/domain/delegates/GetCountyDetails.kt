package com.louis.app.cavity.domain.delegates

import com.louis.app.cavity.domain.stats.BaseStat
import com.louis.app.cavity.domain.stats.PriceByCurrency
import com.louis.app.cavity.domain.stats.StatsQueries
import com.louis.app.cavity.model.CountyDetails
import com.louis.app.cavity.util.L
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GetCountyDetailsUseCase {
    suspend operator fun invoke(
        countyId: Long,
        storageLocation: String? = null
    ): Flow<CountyDetails>
}

class GetCountyDetails(private val statsQueries: StatsQueries) : GetCountyDetailsUseCase {
    override suspend fun invoke(countyId: Long, storageLocation: String?): Flow<CountyDetails> {
        return statsQueries.getBottleStatsForCounty(countyId, storageLocation)
            .map { rows ->
                L.v("compute county stats thread: ${Thread.currentThread()}")
                val bottleCount = rows.size

                val pricesByCurrency =
                    rows.filter { it.price != -1.0 }
                        .groupBy { it.currency }
                        .map { (currency, bottles) ->
                            PriceByCurrency(
                                sum = bottles.sumOf { it.price.toLong() },
                                currency = currency ?: ""
                            )
                        }

                val namings =
                    rows.groupBy { it.naming ?: "" }
                        .map { (label, bottles) ->
                            BaseStat(
                                label = label,
                                count = bottles.size,
                                percentage = bottles.size * 100f / bottleCount,
                                bottleIds = bottles.map { it.bottleId }
                            )
                        }
                        .sortedBy { it.percentage }

                val vintages =
                    rows.groupBy { it.vintage ?: "" }
                        .map { (label, bottles) ->
                            BaseStat(
                                label = label,
                                count = bottles.size,
                                percentage = bottles.size * 100f / bottleCount,
                                bottleIds = bottles.map { it.bottleId }
                            )
                        }
                        .sortedWith(
                            compareBy<BaseStat> { it.percentage }
                                .thenBy { it.label }
                        )

                CountyDetails(bottleCount, pricesByCurrency, namings, vintages)
            }
    }
}
