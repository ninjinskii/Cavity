package com.louis.app.cavity.ui.history

import com.louis.app.cavity.model.HistoryEntry

sealed class HistoryUiModel {
    class EntryModel(val historyEntry: HistoryEntry) : HistoryUiModel()
    class HeaedrModel(val date: Long) : HistoryUiModel()
}
