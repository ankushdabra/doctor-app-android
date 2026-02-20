package com.doctor.app.login.api

import com.doctor.app.core.network.ApiUrlMapper
import com.doctor.app.core.network.NetworkModule
import com.doctor.app.core.storage.TokenManager

data class AuthResult(val user: UserDto, val token: String)

class AuthenticationRepository(private val tokenManager: TokenManager) {
    // Making the API initialization lazy prevents OkHttpClient from being built 
    // during the initial render in Android Studio Layout Preview, which avoids 
    // ClassNotFoundException for Conscrypt classes not available in the IDE environment.
    private val api: ApiUrlMapper by lazy {
        NetworkModule.provideRetrofit(tokenManager)
            .create(ApiUrlMapper::class.java)
    }

    suspend fun login(email: String, password: String): Result<LoginResponseDto> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerDoctor(request: DoctorSignUpRequestDto): Result<SignUpResponseDto> {
        return try {
            val response = api.registerDoctor(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Doctor registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<UserDto> {
        return try {
            val response = api.getProfile()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(request: ProfileUpdateRequestDto): Result<Unit> {
        return try {
            val response = api.updateProfile(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = api.logout()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
