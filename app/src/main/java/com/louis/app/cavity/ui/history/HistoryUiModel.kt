package com.louis.app.cavity.ui.history

import com.louis.app.cavity.model.relation.history.BoundedHistoryEntry

sealed class HistoryUiModel {
    data class EntryModel(val model: BoundedHistoryEntry) : HistoryUiModel()
    data class HeaderModel(val date: Long) : HistoryUiModel()
}
