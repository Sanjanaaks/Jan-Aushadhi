package com.janaushadhi.finder.data.model

data class Medicine(
    val id: String = "",
    val brandName: String = "",
    val genericName: String = "",
    val brandPrice: Double = 0.0,
    val genericPrice: Double = 0.0,
    val category: String = "",
    val manufacturer: String = ""
) {
    val savingsAmount: Int
        get() = (brandPrice - genericPrice).coerceAtLeast(0.0).toInt()

    val savingsPercent: Int
        get() = if (brandPrice > 0) ((savingsAmount / brandPrice) * 100).toInt() else 0
}
