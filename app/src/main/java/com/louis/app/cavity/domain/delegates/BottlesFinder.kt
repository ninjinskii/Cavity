package com.louis.app.cavity.domain.delegates

import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.model.Bottle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BottlesFinder(private val bottleRepository: BottleRepository) {
    fun getBottle(bottleId: Long): Flow<Bottle> {
        return flow {
            val bottle = bottleRepository.getBottleByIdNotLive(bottleId)
            emit(bottle)
        }
    }
}
