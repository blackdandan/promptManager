package com.promptmanager.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptmanager.app.core.network.model.LoginRequest
import com.promptmanager.app.core.network.model.RegisterRequest
import com.promptmanager.app.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            val request = LoginRequest(email, password)
            val result = authRepository.login(request)
            result.fold(
                onSuccess = { _loginState.value = AuthUiState.Success },
                onFailure = { _loginState.value = AuthUiState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun register(username: String, email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            val request = RegisterRequest(username, email, password, displayName)
            val result = authRepository.register(request)
            result.fold(
                onSuccess = { _registerState.value = AuthUiState.Success },
                onFailure = { _registerState.value = AuthUiState.Error(it.message ?: "Registration failed") }
            )
        }
    }
    
    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
