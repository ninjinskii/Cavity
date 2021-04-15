package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "naming",
    foreignKeys = [ForeignKey(
        entity = County::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("county_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Naming(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val naming: String,
    @ColumnInfo(name = "county_id", index = true) val countyId: Long
) :
    Chipable {

    override fun getItemId() = id
    override fun getChipText() = naming
    override fun toString() = naming
}
