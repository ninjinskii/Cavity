package com.louis.app.cavity.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.louis.app.cavity.R
import com.louis.app.cavity.db.HistoryEntryTypeConverter
import com.louis.app.cavity.domain.history.HistoryEntryType
import com.louis.app.cavity.domain.history.toInt

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
    @ColumnInfo(name = "bottle_id", index = true) val bottleId: Long,
    @ColumnInfo(name = "tasting_id") var tastingId: Long? = null,
    val comment: String,
    @field:TypeConverters(HistoryEntryTypeConverter::class)
    val type: HistoryEntryType,
    val favorite: Int,
) {
    fun getResources() = when (type.toInt()) {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryEntry

        if (id != other.id) return false
        if (date != other.date) return false
        if (bottleId != other.bottleId) return false
        if (tastingId != other.tastingId) return false
        if (comment != other.comment) return false
        return type == other.type
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + bottleId.hashCode()
        result = 31 * result + (tastingId?.hashCode() ?: 0)
        result = 31 * result + comment.hashCode()
        result = 31 * result + type.toInt()
        return result
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
