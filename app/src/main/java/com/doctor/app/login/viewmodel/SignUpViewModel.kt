package com.doctor.app.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.login.api.AuthenticationRepository
import com.doctor.app.login.api.DoctorSignUpRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: AuthenticationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val state: StateFlow<UiState<Boolean>> = _state.asStateFlow()

    fun registerDoctor(doctorRequest: DoctorSignUpRequestDto) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            repository.registerDoctor(doctorRequest)
                .onSuccess { token ->
                    tokenManager.saveToken(token)
                    _state.value = UiState.Success(true)
                }
                .onFailure { exception ->
                    _state.value = UiState.Error(exception.message ?: "Doctor registration failed")
                }
        }
    }

    fun resetState() {
        _state.value = UiState.Success(false)
    }
}

class SignUpViewModelFactory(
    private val repository: AuthenticationRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
