package com.louis.app.cavity.ui.addwine

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.repository.CountyRepository
import com.louis.app.cavity.domain.repository.WineRepository
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.ui.UiEvent
import com.louis.app.cavity.ui.UiEventManager
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toInt
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AddWineEvent {
    data class WineChange(val wine: Wine) : AddWineEvent
}

data class AddWineState(
    val updatedWine: Wine? = null,
    val image: String = "",
    val namings: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AddWineViewModel(app: Application) :
    BaseViewModel<AddWineState, AddWineEvent>(app, AddWineState()) {

    private val countyRepository = CountyRepository.getInstance(app)
    private val wineRepository = WineRepository.getInstance(app)

    private val _countyId = MutableStateFlow<Long?>(null)

    private val isEditMode: Boolean
        get() = wineId != 0L

    private var wineId = 0L

    init {
        viewModelScope.launch {
            _countyId
                .filterNotNull()
                .flatMapLatest { wineRepository.getNamingsForCounty(it) }
                .collect { viewState = viewState.copy(namings = it) }
        }
    }

    fun getNamings(): Flow<List<String>> = _countyId
        .filterNotNull()
        .flatMapLatest { wineRepository.getNamingsForCounty(it) }

    fun start(wineId: Long) {
        this.wineId = wineId

        if (wineId != 0L) {
            viewModelScope.launch(IO) {
                val wine = wineRepository.getWineByIdNotLive(wineId)

                _countyId.value = wine.countyId
                viewState = viewState.copy(updatedWine = wine, image = wine.imgPath)
            }
        }
    }

    fun getAllCounties() = countyRepository.getAllCounties()

    fun saveWine(
        name: String,
        naming: String,
        cuvee: String,
        isOrganic: Int,
        colorChipId: Int,
        county: County?
    ) {
        if (county == null) {
            UiEventManager.send(UiEvent.Snackbar(R.string.no_county))
            return
        }

        val color = when (colorChipId) {
            R.id.colorRed -> WineColor.RED
            R.id.colorWhite -> WineColor.WHITE
            R.id.colorSweet -> WineColor.SWEET
            else /* R.id.colorRose */ -> WineColor.ROSE
        }

        val wine = Wine(
            wineId,
            name,
            naming,
            color,
            cuvee,
            isOrganic,
            viewState.image,
            county.id
        )

        viewModelScope.launch(IO) {
            val duplicate = getSimilarWineIfAny(wine)

            if (duplicate != null) {
                when {
                    duplicate.hidden.toBoolean() && !isEditMode -> {
                        wineRepository.updateWine(duplicate.copy(hidden = false.toInt()))
                        sendWineEvents(wine, R.string.wine_already_exists_emergence)
                    }

                    else -> UiEventManager.send(UiEvent.Snackbar(R.string.wine_already_exists))
                }

                return@launch
            }

            when {
                isEditMode -> {
                    wineRepository.updateWine(wine)
                    sendWineEvents(wine, R.string.wine_updated)
                }

                else -> {
                    val wineId = wineRepository.insertWine(wine)
                    val updatedWine = wine.copy(id = wineId)
                    sendWineEvents(updatedWine, R.string.wine_added)
                    reset()
                }
            }
        }
    }

    fun setImage(imagePath: String) {
        viewState = viewState.copy(image = imagePath)
    }

    fun setCountyId(countyId: Long?) {
        countyId?.let {
            _countyId.value = it
        }
    }

    private fun sendWineEvents(wine: Wine, @StringRes message: Int) {
        val event = AddWineEvent.WineChange(wine)
        val uiEvent = UiEvent.Snackbar(message, R.id.snackbarAnchor)
        UiEventManager.send(uiEvent)
        emitEvent(event)
    }

    private fun reset() {
        wineId = 0
    }

    private suspend fun getSimilarWineIfAny(wine: Wine): Wine? {
        val hiddenWines = wineRepository.getWineByAttributes(wine.color, wine.isOrganic, wine.cuvee)

        return withContext(Default) {
            for (hiddenWine in hiddenWines) {
                val hiddenWineName = hiddenWine.name.lowercase()
                val wineName = wine.name.lowercase()
                val hiddenWineNaming = hiddenWine.naming.lowercase()
                val wineNaming = wine.naming.lowercase()

                val hasCloseNames =
                    levenshtein(hiddenWineName, wineName) <= WINE_DUPLICATE_THRESHOLD
                val hasCloseNamings =
                    levenshtein(hiddenWineNaming, wineNaming) <= WINE_DUPLICATE_THRESHOLD
                val isSelf = hiddenWine.id == wineId && isEditMode

                if (hasCloseNames && hasCloseNamings && !isSelf) {
                    return@withContext hiddenWine
                }
            }

            return@withContext null
        }
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1 until rhsLength) {
            newCost[0] = i

            for (j in 1 until lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }

    companion object {
        private const val WINE_DUPLICATE_THRESHOLD = 2
    }
}
