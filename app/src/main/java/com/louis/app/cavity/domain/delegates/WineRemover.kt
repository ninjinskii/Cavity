package com.louis.app.cavity.domain.delegates

import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.util.toBoolean

interface WineRemover {
    suspend fun removeWine(wineId: Long): Boolean
    suspend fun deleteWine(wineId: Long): Boolean
}

// TODO: use objects as return type instead of booleans
class RemoveWineUseCase(
    val wineRepository: WineRepository,
    val bottleRepository: BottleRepository
) :
    WineRemover {

    /**
     * Remove the wine if no associated consumed bottle are bound to it.
     * Otherwise just mark it as hidden and remove its unconsumed bottles,
     * so it doesn't show on home screen
     */
    override suspend fun removeWine(wineId: Long): Boolean {
        try {
            val wineBottles = bottleRepository.getBottlesForWineNotLive(wineId)
            val (consumed, stock) = wineBottles.partition { it.consumed.toBoolean() }

            bottleRepository.deleteBottles(stock)

            when {
                consumed.isNotEmpty() -> wineRepository.hideWineById(wineId)
                else -> deleteWine(wineId)
            }

            return true
        } catch (_: Exception) {
            return false
        }
    }

    /**
     * Delete the wine and all child objects. Will also delete linked bottles, drunk or not.
     * To prevent this, see removeWine
     */
    override suspend fun deleteWine(wineId: Long): Boolean {
        try {
            wineRepository.deleteWineById(wineId)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
