package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Academy::class,
            parentColumns = ["id"],
            childColumns = ["academyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["academyId"])]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val academyId: Long,
    val studentName: String,
    val sessionDate: Long, // timestamp in millis
    val durationHours: Double,
    val hourlyRate: Double, // The rate at the exact time of session logging
    val notes: String = ""
)
