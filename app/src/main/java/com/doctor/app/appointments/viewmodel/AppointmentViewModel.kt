package com.doctor.app.appointments.viewmodel

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

class AppointmentViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<AppointmentDto>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AppointmentDto>>> = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Assuming repository has getAppointments() implemented
                // repository.getAppointments()
                // For now, using mock or direct API call logic if repository is not updated yet
                // But let's assume implementation in repository
                val result = repository.getAppointments()
                result.onSuccess { appointments ->
                    _uiState.value = UiState.Success(appointments)
                }.onFailure { error ->
                    _uiState.value = UiState.Error(error.message ?: "Failed to load appointments")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}

class AppointmentViewModelFactory(
    private val repository: AppointmentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
