package com.louis.app.cavity.model.relation.history

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef

data class HistoryEntryWithFriends(
    @Embedded val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FriendHistoryEntryXRef::class,
            parentColumn = "history_entry_id",
            entityColumn = "friend_id"
        )
    )
    val friends: List<Friend>
)
