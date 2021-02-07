package com.louis.app.cavity.ui.history

import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends

sealed class HistoryUiModel {
    class EntryModel(val model: HistoryEntryWithBottleAndTastingAndFriends) :
        HistoryUiModel()

    class HeaderModel(val date: Long) : HistoryUiModel()
}
