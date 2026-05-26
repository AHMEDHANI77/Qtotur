package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academies")
data class Academy(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultHourlyRate: Double
)
