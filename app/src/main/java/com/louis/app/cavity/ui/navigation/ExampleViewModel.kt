package com.louis.app.cavity.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle

interface BottleConsumer {
    suspend fun consumeBottle(bottle: Bottle)
}

class BottleConsumerDelegate(private val repository: BottleRepository) : BottleConsumer {
    override suspend fun consumeBottle(bottle: Bottle) {
        repository.consumeBottle(bottle.id)
        // Complex business logic, this class is apparented to a use case in the usecase pattern
    }
}

class ExampleViewModel(val wineRepository: WineRepository, val consumer: BottleConsumer) :
    ViewModel(), BottleConsumer by consumer {
    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Application pourrait ne plus être nécessaire si les Repository ne sont plus des singletons, ce qui devrait être possible
                val app = checkNotNull(this[APPLICATION_KEY])
//                 val database = BottleRepository(app)
                val bottleRepository = BottleRepository.getInstance(app)
                val wineRepository = WineRepository.getInstance(app)
                ExampleViewModel(
                    wineRepository,
                    BottleConsumerDelegate(bottleRepository)
                )
            }
        }
    }
}
