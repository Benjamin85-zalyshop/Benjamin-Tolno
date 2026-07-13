package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int = 0,
    val amount: Long,
    val date: Long = System.currentTimeMillis(),
    val reason: String,
    val section: String = "Général",
    val remoteId: String = ""
)
