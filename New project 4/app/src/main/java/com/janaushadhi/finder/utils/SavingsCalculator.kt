package com.janaushadhi.finder.utils

import com.janaushadhi.finder.data.model.Prescription

object SavingsCalculator {
    fun calculateTotalSavings(prescriptions: List<Prescription>): Int {
        return prescriptions.sumOf { prescription ->
            ((prescription.brandPrice - prescription.genericPrice).coerceAtLeast(0.0) * prescription.qty).toInt()
        }
    }
}
