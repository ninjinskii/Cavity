package com.louis.app.cavity.ui.addtasting

import com.louis.app.cavity.model.BottleSize
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.TastingAction
import com.louis.app.cavity.model.TastingBottle
import kotlin.math.log

class TastingScheduler(
    private val tasting: Tasting,
    private val tastingBottles: List<TastingBottle>
) {
    companion object {
        private const val STANDARD_COOLING_COEFFICIENT = 0.002768
        private const val MAGNUM_COOLING_COEFFICIENT = 0.002139
        private const val SLIM_COOLING_COEFFICIENT = 0.003656
    }

    fun getTastingActions(): List<TastingAction> {
        val actions = mutableListOf<TastingAction>()

        for (tastingBottle in tastingBottles) {
            if (tastingBottle.drinkTemp.value < tasting.cellarTemp) {
                val fridgeTime = getCoolingTime(
                    tastingBottle.size,
                    tasting.cellarTemp,
                    tasting.fridgeTemp,
                    tastingBottle.drinkTemp.value
                )

                val setToFridgeAction = TastingAction(
                    0,
                    TastingAction.Action.SET_TO_FRIDGE,
                    fridgeTime.toInt(),
                    tastingBottle.bottleId,
                    0
                )

                actions.add(setToFridgeAction)
            }

            if (tastingBottle.jugTime > 0) {

            }
        }

        return actions
    }

    private fun getCoolingTime(
        bottleSize: BottleSize,
        ambientTemp: Int,
        fridgeTemp: Int,
        desiredTemp: Int
    ): Float {
        val k = when (bottleSize) {
            BottleSize.NORMAL -> STANDARD_COOLING_COEFFICIENT
            BottleSize.MAGNUM -> MAGNUM_COOLING_COEFFICIENT
            BottleSize.SLIM -> SLIM_COOLING_COEFFICIENT
        }

        return (
            -log(
                (desiredTemp.toDouble() - fridgeTemp.toDouble()) /
                    (ambientTemp.toDouble() - fridgeTemp.toDouble()),
                10.0
            )
                / k
            ).toFloat()
    }
}
