package com.doctor.app.login.api

data class DoctorSignUpRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val specialization: String,
    val qualification: String,
    val experience: Int,
    val consultationFee: Double,
    val about: String,
    val clinicAddress: String,
    val availability: Map<String, List<TimeSlotDto>> = emptyMap()
)

data class TimeSlotDto(
    val startTime: String,
    val endTime: String
)
