package com.louis.app.cavity.model.relation.history

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.HistoryEntry
import com.louis.app.cavity.model.relation.bottle.BottleAndWine
import com.louis.app.cavity.model.relation.crossref.FriendHistoryEntryXRef
import com.louis.app.cavity.model.relation.tasting.TastingWithBottles

// TODO: Might need optimization
data class HistoryEntryWithBottleAndTastingAndFriends(
    @Embedded
    val historyEntry: HistoryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "bottle_id"
    )
    val bottleAndWine: BottleAndWine,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id"
    )
    val tasting: TastingWithBottles?,
    @Relation(
        parentColumn = "history_id",
        entityColumn = "friend_id",
        associateBy = Junction(FriendHistoryEntryXRef::class)
    )
    val friends: List<Friend>
)
