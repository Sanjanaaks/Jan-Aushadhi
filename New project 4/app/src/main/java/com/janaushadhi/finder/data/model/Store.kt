package com.janaushadhi.finder.data.model

data class Store(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val phone: String = "",
    val isOpenNow: Boolean = false,
    val district: String = "",
    val state: String = "",
    val distanceKm: Double = 0.0
)
