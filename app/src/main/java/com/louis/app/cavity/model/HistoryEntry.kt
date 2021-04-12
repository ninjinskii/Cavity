package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.louis.app.cavity.R

@Entity(
    tableName = "history_entry",
    foreignKeys = [ForeignKey(
        entity = Bottle::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("bottle_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "bottle_id") val bottleId: Long,
    @ColumnInfo(name = "tasting_id") var tastingId: Long? = null,
    val comment: String,
    val type: Int,
    val favorite: Int,
) {
    fun getResources() = when (type) {
        0 -> HistoryEntryResources(
            R.color.cavity_red,
            R.drawable.ic_glass,
            R.string.nothing,
            R.string.consume_label,
            showFriends = true,
            rawType = 0
        )
        1 -> HistoryEntryResources(
            R.color.cavity_green,
            R.drawable.ic_bottle,
            R.string.buyed_at,
            R.string.nothing,
            showFriends = false,
            rawType = 1
        )
        2 -> HistoryEntryResources(
            R.color.cavity_red,
            R.drawable.ic_gift,
            R.string.gifted_to_someone,
            R.string.gifted_to,
            showFriends = false,
            rawType = 2
        )
        3 -> HistoryEntryResources(
            R.color.cavity_green,
            R.drawable.ic_gift,
            R.string.gifted_by_someone,
            R.string.gifted_by,
            showFriends = false,
            rawType = 3
        )
        4 -> HistoryEntryResources(
            R.color.cavity_gold,
            R.drawable.ic_toast_wine,
            R.string.tasting_label,
            R.string.no_negative,
            showFriends = true,
            rawType = 4
        )
        else -> throw IllegalStateException("Unknown history entry type $type")
    }
}

data class HistoryEntryResources(
    @ColorRes val color: Int,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    @StringRes val detailsLabel: Int,
    val showFriends: Boolean,
    val rawType: Int
)
