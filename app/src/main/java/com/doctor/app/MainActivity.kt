package com.doctor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.login.ui.LoginScreen
import com.doctor.app.login.ui.ProfileScreen
import com.doctor.app.login.ui.SignUpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager(this)
        setContent {
            val themeMode by tokenManager.themeMode.collectAsState(initial = "FOLLOW_SYSTEM")
            val token by tokenManager.token.collectAsState(initial = null)
            
            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            HealthcareTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                
                // Determine start destination based on token
                val startDestination = if (token == null) "login" else "profile"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            tokenManager = tokenManager,
                            onLoginSuccess = {
                                navController.navigate("profile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onRegisterClick = {
                                navController.navigate("signup")
                            }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            tokenManager = tokenManager,
                            onRegistrationSuccess = {
                                navController.navigate("profile") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("profile") {
                        ProfileScreen(
                            tokenManager = tokenManager
                        )
                    }
                }
            }
        }
    }
}
