package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.louis.app.cavity.util.toInt

@Entity(tableName = "expert_advice")
data class ExpertAdvice(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "expert_advice_id")
    val expertAdviceId: Long = 0,
    @ColumnInfo(name = "contest_name") val contestName: String,
    @ColumnInfo(name = "is_medal") var isMedal: Int,
    @ColumnInfo(name = "is_star") var isStar: Int,
    @ColumnInfo(name = "is_rate_20") var isRate20: Int,
    @ColumnInfo(name = "is_rate_100") var isRate100: Int,
    val value: Int,
    @ColumnInfo(name = "bottle_id") var bottleId: Long
) {
    fun revertType() {
        isMedal = false.toInt()
        isStar = false.toInt()
        isRate20 = false.toInt()
        isRate100 = false.toInt()
    }
}
