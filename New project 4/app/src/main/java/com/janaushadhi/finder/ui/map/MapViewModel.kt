package com.janaushadhi.finder.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.janaushadhi.finder.data.model.Store
import com.janaushadhi.finder.data.repository.AuthRepository
import com.janaushadhi.finder.data.repository.StoreRepository
import com.janaushadhi.finder.utils.LocationHelper
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val storeRepository = StoreRepository()
    private val authRepository = AuthRepository()
    private val analytics = Firebase.analytics
    private var allStores = emptyList<Store>()
    private var userLat = 28.6139
    private var userLon = 77.2090
    private var openOnly = false

    private val _stores = MutableLiveData<List<Store>>(emptyList())
    val stores: LiveData<List<Store>> = _stores

    private val _state = MutableLiveData<MapUiState>(MapUiState.Loading)
    val state: LiveData<MapUiState> = _state

    var medicineName: String = ""
        private set

    init {
        loadStores()
    }

    fun setMedicine(value: String) {
        medicineName = value
    }

    fun setLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        filterStores()
    }

    fun setOpenOnly(value: Boolean) {
        openOnly = value
        filterStores()
    }

    fun viewStore(store: Store) {
        analytics.logEvent("store_viewed", android.os.Bundle().apply {
            putString("store_name", store.name)
        })
    }

    fun requestStock(store: Store, onResult: (String) -> Unit) {
        val uid = authRepository.currentUserId ?: return onResult("Please login again")
        val medicine = medicineName.ifBlank { "selected medicine" }
        viewModelScope.launch {
            storeRepository.requestStock(uid, store, medicine)
                .onSuccess { onResult("Request sent to ${store.name} for $medicine. They will contact you within 24 hours.") }
                .onFailure { onResult(it.localizedMessage ?: "Unable to request stock") }
        }
    }

    fun showAllStores() {
        _stores.value = allStores.filter { !openOnly || it.isOpenNow }.sortedBy { 
            LocationHelper.haversineDistance(userLat, userLon, it.latitude, it.longitude)
        }
    }

    fun showKarnatakaStores() {
        _stores.value = allStores.filter { it.state == "Karnataka" && (!openOnly || it.isOpenNow) }
            .sortedBy { LocationHelper.haversineDistance(userLat, userLon, it.latitude, it.longitude) }
    }

    fun showSouthIndiaStores() {
        val southIndianStates = setOf(
            "Karnataka", "Kerala", "Tamil Nadu", "Andhra Pradesh", 
            "Telangana", "Goa", "Maharashtra"
        )
        _stores.value = allStores.filter { it.state in southIndianStates && (!openOnly || it.isOpenNow) }
            .sortedBy { LocationHelper.haversineDistance(userLat, userLon, it.latitude, it.longitude) }
    }

    fun showNorthIndiaStores() {
        val northIndianStates = setOf(
            "Delhi", "Uttar Pradesh", "Punjab", "Haryana", "Rajasthan",
            "Uttarakhand", "Himachal Pradesh", "Jammu & Kashmir", "Chandigarh"
        )
        _stores.value = allStores.filter { it.state in northIndianStates && (!openOnly || it.isOpenNow) }
            .sortedBy { LocationHelper.haversineDistance(userLat, userLon, it.latitude, it.longitude) }
    }

    private fun loadStores() {
        viewModelScope.launch {
            _state.value = MapUiState.Loading
            storeRepository.getStores()
                .onSuccess {
                    allStores = it
                    _state.value = MapUiState.Ready
                    filterStores()
                }
                .onFailure { _state.value = MapUiState.Error(it.localizedMessage ?: "Unable to load stores") }
        }
    }

    private fun filterStores() {
        _stores.value = storeRepository.withinRadius(allStores, userLat, userLon, 50.0, openOnly)
    }
}

sealed class MapUiState {
    data object Loading : MapUiState()
    data object Ready : MapUiState()
    data class Error(val message: String) : MapUiState()
}
