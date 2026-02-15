package com.doctor.app.appointments.api

import com.doctor.app.login.api.UserDto

data class TodaysAppointmentsResponse(
    val appointments: List<AppointmentDto>,
    val totalEarnings: Double
)

data class AppointmentDto(
    val id: String,
    val doctor: UserDto,
    val patient: PatientDto,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String
)

data class PatientDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val age: Int? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val weight: Double? = null,
    val height: Double? = null
)
