package com.doctor.app.core.network

import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.PrescriptionDto
import com.doctor.app.appointments.api.PrescriptionRequestDto
import com.doctor.app.appointments.api.TodaysAppointmentsResponse
import com.doctor.app.login.api.DoctorDetailsDto
import com.doctor.app.login.api.DoctorSignUpRequestDto
import com.doctor.app.login.api.LoginRequestDto
import com.doctor.app.login.api.LoginResponseDto
import com.doctor.app.login.api.ProfileUpdateRequestDto
import com.doctor.app.login.api.SignUpResponseDto
import com.doctor.app.login.api.TimeSlotDto
import com.doctor.app.login.api.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiUrlMapper {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/auth/register-doctor")
    suspend fun registerDoctor(@Body request: DoctorSignUpRequestDto): Response<SignUpResponseDto>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("/api/profile")
    suspend fun getProfile(): UserDto

    @PUT("/api/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequestDto): Response<Unit>

    @PUT("/api/profile/doctor-details")
    suspend fun updateDoctorDetails(@Body details: DoctorDetailsDto): Response<Unit>

    @POST("/api/profile/availability")
    suspend fun updateAvailability(@Body availability: Map<String, List<TimeSlotDto>>): Response<Unit>

    @GET("/api/appointments")
    suspend fun getAppointments(): List<AppointmentDto>

    @GET("/api/appointments/today")
    suspend fun getTodaysAppointments(): TodaysAppointmentsResponse

    @POST("/api/prescriptions")
    suspend fun createPrescription(@Body request: PrescriptionRequestDto): Response<Unit>

    @GET("/api/prescriptions")
    suspend fun getPrescriptions(): List<PrescriptionDto>
}
