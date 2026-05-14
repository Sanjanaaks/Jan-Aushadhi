package com.janaushadhi.finder.utils

import com.janaushadhi.finder.data.model.Medicine
import me.xdrop.fuzzywuzzy.FuzzySearch

object FuzzySearchHelper {
    fun fuzzySearch(query: String, medicines: List<Medicine>): List<Medicine> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return medicines.sortedBy { it.brandName }

        return medicines.mapNotNull { medicine ->
            val brandScore = FuzzySearch.tokenSortRatio(normalizedQuery.lowercase(), medicine.brandName.lowercase())
            val genericScore = FuzzySearch.tokenSortRatio(normalizedQuery.lowercase(), medicine.genericName.lowercase())
            val partialBrandScore = FuzzySearch.partialRatio(normalizedQuery.lowercase(), medicine.brandName.lowercase())
            val score = maxOf(brandScore, genericScore, partialBrandScore)
            if (score >= 60) medicine to score else null
        }.sortedWith(compareByDescending<Pair<Medicine, Int>> { it.second }.thenBy { it.first.brandName })
            .map { it.first }
    }
}
