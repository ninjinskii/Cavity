package com.louis.app.cavity.model.relation.tasting

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.model.relation.crossref.TastingFriendXRef

data class TastingWithFriends(
    @Embedded val tasting: Tasting,
    @Relation(
        parentColumn = "id",
        entityColumn = "tasting_id",
        associateBy = Junction(TastingFriendXRef::class)
    )
    val friends: List<Friend>
)
