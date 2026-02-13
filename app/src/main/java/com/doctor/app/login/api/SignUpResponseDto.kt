package com.doctor.app.login.api

data class SignUpResponseDto(
    val user: UserDto,
    val token: String
)
