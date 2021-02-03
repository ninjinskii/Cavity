package com.louis.app.cavity.model.relation.history

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef

data class HistoryEntryWithBottleAndTastingAndFriends(
    @Embedded
    val historyEntry: HistoryEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val bottle: Bottle,

    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id"
    )
    val tasting: Tasting,

    @Relation(
        parentColumn = "history_id",
        entityColumn = "friend_id",
        associateBy = Junction(FriendHistoryEntryXRef::class)
    )
    val friends: List<Friend>
)
