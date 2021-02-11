package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friend")
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String,
    @ColumnInfo(name = "img_path") val imgPath: String,
) : Chipable {
    companion object {
        fun parseName(nameLastName: String): Pair<String, String> {
            val split = nameLastName.trim().split(" ", limit = 1)

            return if (split.size == 1) {
                nameLastName to ""
            } else {
                split[0] to split[2]
            }
        }
    }

    override fun getItemId() = id
    override fun getChipText() = "$firstName $lastName".trim()
}
