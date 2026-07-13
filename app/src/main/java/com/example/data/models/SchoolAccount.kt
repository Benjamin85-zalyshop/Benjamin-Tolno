package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_accounts")
data class SchoolAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val schoolName: String,
    val passwordHash: String,
    val financierPasswordHash: String = "",
    val hasActiveSubscription: Boolean = false,
    val isPendingValidation: Boolean = false,
    val paymentPhoneNumber: String? = null,
    val transactionId: String? = null,
    val displayName: String = "",
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
