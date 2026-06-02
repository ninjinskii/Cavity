package com.louis.app.cavity.ui.tasting

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.louis.app.cavity.db.dao.BoundedTasting
import com.louis.app.cavity.domain.repository.FriendRepository
import com.louis.app.cavity.domain.repository.TastingRepository
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.BaseViewModel
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

sealed interface TastingEvent {
    data class UserFeedback(@StringRes val resId: Int) : TastingEvent
}

data class TastingUiState(
    val undoneTastings: List<BoundedTasting> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val hasTastingToday: Boolean = false
)

class TastingViewModel(app: Application) : BaseViewModel<TastingUiState, TastingEvent>(app, TastingUiState()) {
    private val tastingRepository = TastingRepository.getInstance(app)
    private val friendRepository = FriendRepository.getInstance(app)

    init {
        combine(
            tastingRepository.getUndoneTastings(),
            friendRepository.getAllFriends()
        ) { tastings, friends ->
            TastingUiState(
                undoneTastings = tastings,
                friends = friends,
                hasTastingToday = tastings.any { DateFormatter.isToday(it.tasting.date) && !it.tasting.done }
            )
        }
            .onEach { viewState = it }
            .launchIn(viewModelScope)
    }
}
