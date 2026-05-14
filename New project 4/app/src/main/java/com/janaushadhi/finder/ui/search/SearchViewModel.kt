package com.janaushadhi.finder.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.janaushadhi.finder.data.model.Medicine
import com.janaushadhi.finder.data.repository.AuthRepository
import com.janaushadhi.finder.data.repository.MedicineRepository
import com.janaushadhi.finder.utils.FuzzySearchHelper
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val medicineRepository = MedicineRepository()
    private val authRepository = AuthRepository()
    private val analytics = Firebase.analytics

    private val allMedicines = MutableLiveData<List<Medicine>>(emptyList())
    private val query = MutableLiveData("")
    private val category = MutableLiveData("All")

    private val _searchResults = MutableLiveData<List<Medicine>>(emptyList())
    val searchResults: LiveData<List<Medicine>> = _searchResults

    private val _state = MutableLiveData<SearchUiState>(SearchUiState.Loading)
    val state: LiveData<SearchUiState> = _state

    init {
        fetchMedicines()
    }

    fun fetchMedicines() {
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            medicineRepository.getMedicines()
                .onSuccess {
                    allMedicines.value = it
                    _state.value = SearchUiState.Ready
                    applySearch()
                }
                .onFailure { _state.value = SearchUiState.Error(it.localizedMessage ?: "Unable to load medicines") }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
        if (value.isNotBlank()) {
            analytics.logEvent("medicine_searched", android.os.Bundle().apply {
                putString("medicine_name", value)
            })
        }
        applySearch()
    }

    fun updateCategory(value: String) {
        category.value = value
        applySearch()
    }

    fun addToPrescription(medicine: Medicine, onResult: (String) -> Unit) {
        val uid = authRepository.currentUserId
        if (uid == null) {
            onResult("Please login again")
            return
        }
        viewModelScope.launch {
            medicineRepository.addPrescription(uid, medicine)
                .onSuccess { onResult("${medicine.brandName} added to prescriptions") }
                .onFailure { onResult(it.localizedMessage ?: "Unable to add prescription") }
        }
    }

    private fun applySearch() {
        val source = allMedicines.value.orEmpty()
        val selectedCategory = category.value.orEmpty()
        val filtered = if (selectedCategory == "All") source else source.filter { it.category == selectedCategory }
        _searchResults.value = FuzzySearchHelper.fuzzySearch(query.value.orEmpty(), filtered)
    }
}

sealed class SearchUiState {
    data object Loading : SearchUiState()
    data object Ready : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
