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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.ui.AppointmentDetailScreen
import com.doctor.app.appointments.ui.AppointmentListScreen
import com.doctor.app.appointments.ui.PrescriptionListScreen
import com.doctor.app.appointments.viewmodel.AppointmentViewModel
import com.doctor.app.appointments.viewmodel.AppointmentViewModelFactory
import com.doctor.app.appointments.viewmodel.PrescriptionViewModel
import com.doctor.app.appointments.viewmodel.PrescriptionViewModelFactory
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.home.ui.HomeScreen
import com.doctor.app.home.viewmodel.HomeViewModel
import com.doctor.app.home.viewmodel.HomeViewModelFactory
import com.doctor.app.login.ui.ProfileScreen

enum class DashboardTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("Home", Icons.Filled.Home, Icons.Outlined.Home),
    Appointments("Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    Patients("Patients", Icons.Filled.People, Icons.Outlined.People),
    Profile("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun DashboardScreen(
    tokenManager: TokenManager
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.Home) }
    var selectedAppointment by remember { mutableStateOf<AppointmentDto?>(null) }
    
    // Shared Repository and ViewModels
    val repository = remember(tokenManager) { AppointmentRepository(tokenManager) }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val appointmentViewModel: AppointmentViewModel = viewModel(factory = AppointmentViewModelFactory(repository))
    val prescriptionViewModel: PrescriptionViewModel = viewModel(factory = PrescriptionViewModelFactory(repository))

    // Lifecycle-aware state collection
    val doctor by tokenManager.userDetails.collectAsStateWithLifecycle(initialValue = null)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Data reload logic
    val refreshCurrentTab = remember(homeViewModel, appointmentViewModel, prescriptionViewModel) {
        { tab: DashboardTab ->
            when (tab) {
                DashboardTab.Home -> homeViewModel.loadData()
                DashboardTab.Appointments -> appointmentViewModel.loadAppointments()
                DashboardTab.Patients -> prescriptionViewModel.loadPrescriptions()
                DashboardTab.Profile -> { /* Profile refresh if needed */ }
            }
        }
    }

    // Trigger data reload when selectedTab changes OR when returning from detail view
    LaunchedEffect(selectedTab, selectedAppointment) {
        if (selectedAppointment == null) {
            refreshCurrentTab(selectedTab)
        }
    }

    // Stable lambdas for callbacks to prevent unnecessary recompositions
    val onAppointmentClick = remember { { appointment: AppointmentDto -> selectedAppointment = appointment } }
    val onViewAllClick = remember { { selectedTab = DashboardTab.Appointments } }
    val onTabClick = remember(refreshCurrentTab) { 
        { tab: DashboardTab ->
            if (selectedTab == tab) {
                refreshCurrentTab(tab)
            } else {
                selectedTab = tab
                selectedAppointment = null
            }
        }
    }

    // Handle back press to deselect appointment
    if (selectedAppointment != null) {
        BackHandler { selectedAppointment = null }
    }

    Scaffold(
        bottomBar = {
            if (selectedAppointment == null) {
                DashboardBottomBar(
                    selectedTab = selectedTab,
                    onTabClick = onTabClick
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
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
                        val springSpec = spring<Float>(stiffness = Spring.StiffnessLow)
                        (fadeIn(animationSpec = springSpec) + scaleIn(initialScale = 0.95f, animationSpec = springSpec))
                            .togetherWith(fadeOut(animationSpec = springSpec))
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
                                doctor?.let {
                                    HomeScreen(
                                        doctor = it,
                                        uiState = homeUiState,
                                        onViewAllClick = onViewAllClick,
                                        onAppointmentClick = onAppointmentClick
                                    )
                                } ?: Box(Modifier.fillMaxSize(), Alignment.Center) { 
                                    CircularProgressIndicator(strokeWidth = 3.dp) 
                                }
                            }

                            DashboardTab.Appointments -> {
                                AppointmentListScreen(
                                    tokenManager = tokenManager,
                                    onAppointmentClick = onAppointmentClick
                                )
                            }

                            DashboardTab.Patients -> {
                                PrescriptionListScreen(
                                    tokenManager = tokenManager
                                )
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

@Composable
private fun DashboardBottomBar(
    selectedTab: DashboardTab,
    onTabClick: (DashboardTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        DashboardTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabClick(tab) },
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    HealthcareTheme(darkTheme = false) {
        DashboardScreen(tokenManager = TokenManager(LocalContext.current))
    }
}
