package com.janaushadhi.finder.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janaushadhi.finder.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthUiState>()
    val authState: LiveData<AuthUiState> = _authState

    fun login(email: String, password: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthUiState.Error("Invalid email")
            return
        }
        if (password.isBlank()) {
            _authState.value = AuthUiState.Error("Wrong password")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.login(email, password)
                .onSuccess { _authState.value = AuthUiState.Success }
                .onFailure { _authState.value = AuthUiState.Error(repository.friendlyAuthMessage(it)) }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        val validationError = validateRegistration(name, email, phone, password, confirmPassword)
        if (validationError != null) {
            _authState.value = AuthUiState.Error(validationError)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.register(name, email, phone, password)
                .onSuccess { _authState.value = AuthUiState.Success }
                .onFailure { _authState.value = AuthUiState.Error(repository.friendlyAuthMessage(it)) }
        }
    }

    fun resetPassword(email: String) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthUiState.Error("Invalid email")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            repository.sendPasswordReset(email)
                .onSuccess { _authState.value = AuthUiState.Message("Password reset link sent") }
                .onFailure { _authState.value = AuthUiState.Error(repository.friendlyAuthMessage(it)) }
        }
    }

    private fun validateRegistration(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): String? = when {
        name.isBlank() -> "Full name is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
        phone.length != 10 || !phone.all { it.isDigit() } -> "Enter a valid 10 digit phone number"
        password.length < 6 -> "Password must be at least 6 characters"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }
}

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Message(val message: String) : AuthUiState()
}
