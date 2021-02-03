package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.Tasting

data class HistoryEntryWithBottleAndTastingAndFriends(
    @Embedded
    val historyEntry: HistoryEntry,

    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "bottle_id"
    )
    val bottle: Bottle,

    @Relation(
        parentColumn = "tasting_id",
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
