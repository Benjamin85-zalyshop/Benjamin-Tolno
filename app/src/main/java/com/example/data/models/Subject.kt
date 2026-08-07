package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int = 0,
    val section: String = "",
    val grade: String = "",
    val name: String = "",
    val coefficient: Int = 1,
    val maxScore: Float = 20f,
    val remoteId: String = ""
)
