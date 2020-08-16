package com.louis.app.cavity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person")
data class Person(
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "last_name") val lastName: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_person")
    var idPerson: Long = 0
}