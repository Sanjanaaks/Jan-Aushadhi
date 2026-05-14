package com.janaushadhi.finder.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val prescriptions: List<Prescription> = emptyList()
)
