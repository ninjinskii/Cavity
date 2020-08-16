package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expert_advice")
data class ExpertAdvice(
    @ColumnInfo(name = "contest_name") val contestName: String,
    @ColumnInfo(name = "is_medal") val isMedal: Int,
    @ColumnInfo(name = "is_star") val isStar: Int,
    @ColumnInfo(name = "is_rate_20") val isRate20: Int,
    @ColumnInfo(name = "is_rate_100") val isRate100: Int,
    val stars: Int,
    val rate: Int,
    val medal: Int,
    @ColumnInfo(name = "id_bottle") val idBottle: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_expert_advice")
    var idExpertAdvice: Long = 0
}