package com.louis.app.cavity.model.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.ExpertAdvice

data class BottleWithExpertAdvices(
    @Embedded val bottle: Bottle,
    @Relation(
        parentColumn = "bottle_id",
        entityColumn = "expert_advice_id"
    )
    val expertAdvices: List<ExpertAdvice>
)
