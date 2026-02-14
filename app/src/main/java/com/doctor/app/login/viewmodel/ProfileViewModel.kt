package com.doctor.app.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.login.api.AuthenticationRepository
import com.doctor.app.login.api.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: AuthenticationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _refreshError = MutableStateFlow<String?>(null)

    // Cleaned up UI State: It now prioritizes the local cache (TokenManager)
    // and only shows loading/error states when no cached data is available.
    val uiState: StateFlow<UiState<UserDto>> = tokenManager.userDetails
        .combine(_refreshError) { user, error ->
            when {
                user != null -> UiState.Success(user)
                error != null -> UiState.Error(error)
                else -> UiState.Loading
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val themeMode: StateFlow<String> = tokenManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "FOLLOW_SYSTEM")

    init {
        loadProfile()
    }

    /**
     * Refreshes the profile from the network and updates the local cache.
     * The UI will automatically react to the cache update via the userDetails flow.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _refreshError.value = null
            repository.getProfile()
                .onSuccess { user ->
                    // Save to cache for application-wide consistency
                    tokenManager.saveUserDetails(user)
                }
                .onFailure { error ->
                    _refreshError.value = error.message ?: "Failed to load profile"
                }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            tokenManager.saveThemeMode(mode)
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}

class ProfileViewModelFactory(
    private val repository: AuthenticationRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
