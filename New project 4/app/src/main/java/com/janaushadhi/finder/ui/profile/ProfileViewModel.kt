package com.janaushadhi.finder.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.janaushadhi.finder.data.model.User
import com.janaushadhi.finder.data.repository.AuthRepository
import com.janaushadhi.finder.utils.SavingsCalculator
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val analytics = Firebase.analytics

    private val _profile = MutableLiveData<User>()
    val profile: LiveData<User> = _profile

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess {
                    _profile.value = it
                    analytics.logEvent("savings_calculated", android.os.Bundle().apply {
                        putLong("amount_saved", SavingsCalculator.calculateTotalSavings(it.prescriptions).toLong())
                    })
                }
                .onFailure { _message.value = it.localizedMessage ?: "Unable to load profile" }
        }
    }

    fun sendChangePassword() {
        val email = authRepository.currentEmail
        if (email == null) {
            _message.value = "No email address found. Please login again."
            return
        }
        
        viewModelScope.launch {
            _message.value = "Sending password reset link..."
            authRepository.sendPasswordReset(email)
                .onSuccess { 
                    _message.value = "Password reset link sent to $email. Please check your inbox (and spam folder)."
                }
                .onFailure { error ->
                    val errorMessage = when {
                        error.localizedMessage?.contains("email", true) == true -> 
                            "Email address issue: ${error.localizedMessage}"
                        error.localizedMessage?.contains("network", true) == true -> 
                            "Network error. Please check your internet connection and try again."
                        else -> 
                            "Failed to send reset link: ${authRepository.friendlyAuthMessage(error)}"
                    }
                    _message.value = errorMessage
                }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            authRepository.updateProfile(name, phone)
                .onSuccess { 
                    _message.value = "Profile updated successfully"
                    loadProfile() // Reload profile data
                }
                .onFailure { _message.value = it.localizedMessage ?: "Unable to update profile" }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
