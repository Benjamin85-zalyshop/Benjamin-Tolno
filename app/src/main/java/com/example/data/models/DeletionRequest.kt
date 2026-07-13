package com.example.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DeletionRequest(
    val id: String = "",
    val studentRemoteId: String = "",
    val studentName: String = "",
    val grade: String = "",
    val section: String = "",
    val reason: String = "",
    val requestedBy: String = "",
    val requestedAt: Long = 0L
)
