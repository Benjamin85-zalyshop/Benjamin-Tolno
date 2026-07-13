package com.example.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [Index(value = ["studentId"])],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolId: Int = 0,
    val studentId: Int,
    val amount: Long,
    val date: Long = System.currentTimeMillis(),
    val reason: String,
    val remoteId: String = "",
    val paymentMethod: String = "Espèces"
)
