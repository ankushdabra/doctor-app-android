package com.doctor.app.appointments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.api.PrescriptionRequestDto
import com.doctor.app.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrescriptionViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
    val uiState = _uiState.asStateFlow()

    fun createPrescription(
        patientId: String,
        doctorId: String,
        appointmentId: String,
        medications: String,
        instructions: String,
        notes: String
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val request = PrescriptionRequestDto(
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentId = appointmentId,
                    medications = medications,
                    instructions = instructions,
                    notes = notes
                )
                val result = repository.createPrescription(request)
                if (result.isSuccess) {
                    _uiState.value = UiState.Success(true)
                } else {
                    _uiState.value =
                        UiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

class PrescriptionViewModelFactory(
    private val repository: AppointmentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrescriptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrescriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
