package com.doctor.app.login.api

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val specialization: String? = null,
    val qualification: String? = null,
    val experience: Int? = null,
    val consultationFee: Double? = null,
    val about: String? = null,
    val clinicAddress: String? = null,
    val profileImage: String? = null
)
