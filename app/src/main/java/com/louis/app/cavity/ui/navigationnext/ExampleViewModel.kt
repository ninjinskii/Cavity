package com.louis.app.cavity.ui.navigationnext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.louis.app.cavity.domain.repository.BottleRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.Bottle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
                // Mais on aura de besoin de app pour passer la db au repository dans tous les cas
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


// ---------------------------------
// Example no SharedFlow viewmodel, because shared flow seems to be painful to use when different observers has different lifecycles
// ALso, consider Channel() if data has only one observer

data class A(val event: String)
data class B(val event: String)

data class ExampleEventWrapper(val a: A? = null, val b: B? = null)

data class ExampleState(val title: String, val thing: Any, val events: ExampleEventWrapper)

open class BaseExampleNoEventViewModel<State>(initialState: ExampleState) {
    protected val stateFlow = MutableStateFlow(initialState)
    val state = stateFlow.asStateFlow()
}

class ExampleNoEventViewModel() :
    BaseExampleNoEventViewModel<ExampleState>(ExampleState("", 1, ExampleEventWrapper())) {


    fun aConsumed() {
        stateFlow.update {
            it.copy(events = it.events.copy(a = null))
        }
    }

    fun bConsumed() {
        stateFlow.update {
            it.copy(events = it.events.copy(b = null))
        }
    }
}
