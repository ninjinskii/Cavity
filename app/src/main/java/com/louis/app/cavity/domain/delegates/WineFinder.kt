package com.louis.app.cavity.domain.delegates

import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Wine
import kotlinx.coroutines.flow.Flow

class WineFinder(val wineRepository: WineRepository) {
    fun getWine(wineId: Long): Flow<Wine> {
        return wineRepository.getWineByIdFlow(wineId)
    }
}
