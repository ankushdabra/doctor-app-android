package com.doctor.app.appointments.api

data class PrescriptionRequestDto(
    val patientId: String,
    val doctorId: String,
    val appointmentId: String,
    val medications: String,
    val instructions: String,
    val notes: String
)
