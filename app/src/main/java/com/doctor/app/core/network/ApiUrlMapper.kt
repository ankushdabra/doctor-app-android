package com.doctor.app.core.network

import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.login.api.DoctorSignUpRequestDto
import com.doctor.app.login.api.LoginRequestDto
import com.doctor.app.login.api.LoginResponseDto
import com.doctor.app.login.api.SignUpResponseDto
import com.doctor.app.login.api.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiUrlMapper {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/auth/register-doctor")
    suspend fun registerDoctor(@Body request: DoctorSignUpRequestDto): Response<SignUpResponseDto>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("/api/profile")
    suspend fun getProfile(): UserDto

    @GET("/api/appointments")
    suspend fun getAppointments(): List<AppointmentDto>
}
