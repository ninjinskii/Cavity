package com.louis.app.cavity.ui.history

import com.louis.app.cavity.model.relation.history.BoundedHistoryEntry

sealed class HistoryUiModel {
    class EntryModel(val model: BoundedHistoryEntry) : HistoryUiModel()
    class HeaderModel(val date: Long) : HistoryUiModel()
}
