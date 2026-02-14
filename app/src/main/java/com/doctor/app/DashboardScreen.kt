package com.doctor.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.ui.AppointmentDetailScreen
import com.doctor.app.appointments.ui.AppointmentListScreen
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.home.ui.HomeScreen
import com.doctor.app.login.ui.ProfileScreen

sealed class DashboardTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : DashboardTab("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Appointments : DashboardTab(
        "appointments",
        "Schedule",
        Icons.Filled.CalendarMonth,
        Icons.Outlined.CalendarMonth
    )

    object Patients :
        DashboardTab("patients", "Patients", Icons.Filled.People, Icons.Outlined.People)

    object Profile :
        DashboardTab("profile_tab", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun DashboardScreen(
    tokenManager: TokenManager
) {
    var selectedTab by remember { mutableStateOf<DashboardTab>(DashboardTab.Home) }
    var selectedAppointment by remember { mutableStateOf<AppointmentDto?>(null) }
    
    // Fetch cached user details
    val userDetails by tokenManager.userDetails.collectAsState(initial = null)

    // Handle back press to deselect appointment
    if (selectedAppointment != null) {
        BackHandler {
            selectedAppointment = null
        }
    }

    val tabs = listOf(
        DashboardTab.Home,
        DashboardTab.Appointments,
        DashboardTab.Patients,
        DashboardTab.Profile
    )

    Scaffold(
        bottomBar = {
            if (selectedAppointment == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { 
                                selectedTab = tab
                                selectedAppointment = null
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            if (selectedAppointment != null) {
                AppointmentDetailScreen(
                    appointment = selectedAppointment!!,
                    onBackClick = { selectedAppointment = null }
                )
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                                scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                ))
                            .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)))
                    },
                    label = "DashboardTabTransition"
                ) { targetTab ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (targetTab) {
                            DashboardTab.Home -> {
                                HomeScreen(
                                    tokenManager = tokenManager,
                                    onViewAllClick = { selectedTab = DashboardTab.Appointments },
                                    onAppointmentClick = { selectedAppointment = it }
                                )
                            }

                            DashboardTab.Appointments -> {
                                AppointmentListScreen(
                                    tokenManager = tokenManager,
                                    onAppointmentClick = { selectedAppointment = it }
                                )
                            }

                            DashboardTab.Patients -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Patient Records",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                            }

                            DashboardTab.Profile -> {
                                ProfileScreen(
                                    tokenManager = tokenManager
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    HealthcareTheme(darkTheme = false) {
        DashboardScreen(tokenManager = TokenManager(LocalContext.current))
    }
}
