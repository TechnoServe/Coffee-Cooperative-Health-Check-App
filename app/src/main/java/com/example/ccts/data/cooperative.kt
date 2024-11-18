package com.example.ccts.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cooperative")
data class Cooperative(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ownerName: String,
    val location: String
)