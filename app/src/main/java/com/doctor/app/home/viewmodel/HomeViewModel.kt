package com.doctor.app.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AppointmentDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AppointmentDto>>> = _uiState.asStateFlow()

    init {
        loadTodaysAppointments()
    }

    fun loadTodaysAppointments() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTodaysAppointments()
                .onSuccess { appointments ->
                    _uiState.value = UiState.Success(appointments)
                }
                .onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Unknown error occurred")
                }
        }
    }
}

class HomeViewModelFactory(private val repository: AppointmentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
