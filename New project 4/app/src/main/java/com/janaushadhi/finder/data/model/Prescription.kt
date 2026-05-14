package com.janaushadhi.finder.data.model

import com.google.firebase.Timestamp

data class Prescription(
    val brandName: String = "",
    val genericName: String = "",
    val qty: Int = 1,
    val refillDate: Timestamp = Timestamp.now(),
    val brandPrice: Double = 0.0,
    val genericPrice: Double = 0.0,
    val enabled: Boolean = true
)
