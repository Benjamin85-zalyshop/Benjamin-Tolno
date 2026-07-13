package com.example.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ClassFee(
    val grade: String = "",
    val feeAmount: Long = 0L
)
