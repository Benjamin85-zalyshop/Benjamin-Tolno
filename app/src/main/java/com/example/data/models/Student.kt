package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int = 0,
    val firstName: String,
    val lastName: String,
    val grade: String,
    val section: String = "Non défini",
    val remoteId: String = "",
    val parentWhatsApp: String? = null,
    val registrationFee: Long = 0L,
    val reenrollmentFee: Long = 0L
)
