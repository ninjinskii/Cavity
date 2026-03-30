package com.louis.app.cavity.domain.delegates

import com.louis.app.cavity.R
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt

interface RemoveWineUseCase {
    suspend operator fun invoke(wine: Wine?): UseCaseResult
    suspend fun deleteWine(wine: Wine?): UseCaseResult
}

// TODO: demande à une ia comment on gère ce soucis de transcation, car théoriquement il faut avoir accès à la bdd pour faire une transcation
// mais seul les repo ont accès à la bdd. Ducoup on utilise un des repor pour faire une transaction multi item entre plusieurs repo, mais on choisi un repo arbitrairement
class RemoveWine(
    private val wineRepository: WineRepository,
    private val bottleRepository: BottleRepository,
    private val errorReporter: ErrorReporter
) :
    RemoveWineUseCase {

    /**
     * Remove the wine if no associated consumed bottle are bound to it.
     * Otherwise just mark it as hidden and remove its unconsumed bottles,
     * so it doesn't show on home screen
     */
    override suspend operator fun invoke(wine: Wine?): UseCaseResult {
        if (wine == null) {
            errorReporter.captureMessage("Trying to remove a null wine")
            return UseCaseResult.Fail()
        }

        try {
            val wineBottles = bottleRepository.getBottlesForWineNotLive(wine.id)
            val (consumed, stock) = wineBottles.partition { it.consumed.toBoolean() }

            bottleRepository.transaction {
                bottleRepository.deleteBottles(stock)

                when {
                    consumed.isNotEmpty() -> hideWine(wine)
                    else -> wineRepository.deleteWine(wine)
                }
            }

            return UseCaseResult.Success(R.string.wine_deleted)
        } catch (e: Exception) {
            errorReporter.captureException(e)
            return UseCaseResult.Fail()
        }
    }

    /**
     * Delete the wine and all child objects. Will also delete linked bottles, drunk or not.
     */
    override suspend fun deleteWine(wine: Wine?): UseCaseResult {
        if (wine == null) {
            errorReporter.captureMessage("Trying to remove a null wine")
            return UseCaseResult.Fail()
        }

        try {
            wineRepository.deleteWine(wine)
            return UseCaseResult.Success(R.string.wine_deleted)
        } catch (e: Exception) {
            errorReporter.captureException(e)
            return UseCaseResult.Fail()
        }
    }

    private suspend fun hideWine(wine: Wine) {
        val updatedWine = wine.copy(hidden = true.toInt())
        wineRepository.updateWine(updatedWine)
    }
}
