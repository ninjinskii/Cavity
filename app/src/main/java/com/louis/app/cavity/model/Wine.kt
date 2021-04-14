package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "wine",
    foreignKeys = [ForeignKey(
        entity = Naming::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("naming_id"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Wine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val cuvee: String,
    @ColumnInfo(name = "is_organic") val isOrganic: Int,
    @ColumnInfo(name = "img_path") val imgPath: String,
    @ColumnInfo(name = "naming_id", index = true) val namingId: Long
)
