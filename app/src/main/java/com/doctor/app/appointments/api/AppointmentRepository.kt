package com.doctor.app.appointments.api

import com.doctor.app.core.network.ApiUrlMapper
import com.doctor.app.core.network.NetworkModule
import com.doctor.app.core.storage.TokenManager

class AppointmentRepository(private val tokenManager: TokenManager) {
    // Making the API initialization lazy prevents OkHttpClient from being built 
    // during the initial render in Android Studio Layout Preview, which avoids 
    // ClassNotFoundException for Conscrypt classes not available in the IDE environment.
    private val api: ApiUrlMapper by lazy {
        NetworkModule.provideRetrofit(tokenManager)
            .create(ApiUrlMapper::class.java)
    }

    suspend fun getAppointments(): Result<List<AppointmentDto>> {
        return try {
            val response = api.getAppointments()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodaysAppointments(): Result<List<AppointmentDto>> {
        return try {
            val response = api.getTodaysAppointments()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPrescription(request: PrescriptionRequestDto): Result<Unit> {
        return try {
            val response = api.createPrescription(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create prescription"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPrescriptions(): Result<List<PrescriptionDto>> {
        return try {
            val response = api.getPrescriptions()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
