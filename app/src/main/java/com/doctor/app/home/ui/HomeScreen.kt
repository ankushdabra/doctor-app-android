package com.doctor.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.api.PatientDto
import com.doctor.app.appointments.api.TodaysAppointmentsResponse
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.home.viewmodel.HomeViewModel
import com.doctor.app.home.viewmodel.HomeViewModelFactory
import com.doctor.app.login.api.UserDto
import java.util.Calendar

@Composable
fun HomeScreen(
    doctor: UserDto,
    uiState: UiState<TodaysAppointmentsResponse>,
    onViewAllClick: () -> Unit = {},
    onAppointmentClick: (AppointmentDto) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(doctor.name)
        }

        item {
            when (val state = uiState) {
                is UiState.Success -> {
                    val nextAppointment = remember(state.data.appointments) {
                        state.data.appointments.firstOrNull { it.status == "BOOKED" }
                    }
                    if (nextAppointment != null) {
                        NextAppointmentHighlight(nextAppointment, onAppointmentClick)
                    } else {
                        NoAppointmentsCard()
                    }
                }
                is UiState.Loading -> LoadingCard()
                is UiState.Error -> ErrorCard(state.message)
            }
        }

        item {
            StatsGrid(uiState)
        }

        item {
            SectionHeader(
                title = "Today's Schedule",
                actionText = "View All",
                onActionClick = onViewAllClick
            )
        }
        
        item {
            when (val state = uiState) {
                is UiState.Success -> AppointmentsRow(state.data.appointments, onAppointmentClick)
                is UiState.Loading -> LoadingScheduleRow()
                is UiState.Error -> Text(
                    text = "Failed to load schedule", 
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    tokenManager: TokenManager,
    onViewAllClick: () -> Unit = {},
    onAppointmentClick: (AppointmentDto) -> Unit = {}
) {
    val repository = remember(tokenManager) { AppointmentRepository(tokenManager) }
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val doctor by tokenManager.userDetails.collectAsStateWithLifecycle(initialValue = null)
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    if (doctor != null) {
        HomeScreen(
            doctor = doctor!!,
            uiState = uiState,
            onViewAllClick = onViewAllClick,
            onAppointmentClick = onAppointmentClick
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 3.dp)
        }
    }
}

@Composable
private fun HeaderSection(name: String) {
    val displayName = remember(name) { if (name.startsWith("Dr.", ignoreCase = true)) name else "Dr. $name" }
    val salutation = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = salutation,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NextAppointmentHighlight(
    appointment: AppointmentDto,
    onStartClick: (AppointmentDto) -> Unit
) {
    // consistently deep, professional medical navy gradient requested
    val darkHeaderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF002E69), // Deep Navy
            Color(0xFF004494)  // Professional Blue
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    val contentColor = Color.White
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = darkHeaderBrush)
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .offset(x = 260.dp, y = (-30).dp)
                    .size(180.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.06f),
                        shape = CircleShape
                    )
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = contentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "NEXT PATIENT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = appointment.patient.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Text(
                            text = "Scheduled Appointment",
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = contentColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Badge, 
                                contentDescription = null, 
                                tint = contentColor, 
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer, 
                            contentDescription = null, 
                            tint = contentColor, 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = appointment.appointmentTime, 
                            style = MaterialTheme.typography.labelLarge, 
                            color = contentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Button(
                        onClick = { onStartClick(appointment) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.9f), 
                            contentColor = Color(0xFF002E69)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("Start Now", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(uiState: UiState<TodaysAppointmentsResponse>) {
    val patientCount = remember(uiState) {
        if (uiState is UiState.Success) uiState.data.appointments.size.toString() else "--"
    }
    
    val earningsValue = remember(uiState) {
        when (val state = uiState) {
            is UiState.Success -> {
                val earnings = state.data.totalEarnings
                if (earnings >= 1000) {
                    val kValue = earnings / 1000.0
                    val formatted = String.format("%.1f", kValue)
                    "₹${if (formatted.endsWith(".0")) formatted.substringBefore(".0") else formatted}k"
                } else {
                    "₹${earnings.toInt()}"
                }
            }
            is UiState.Loading -> "..."
            is UiState.Error -> "Err"
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCardEnhanced(
            modifier = Modifier.weight(1f),
            title = "Patients Today",
            value = patientCount,
            icon = Icons.Outlined.Group,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            accentColor = MaterialTheme.colorScheme.primary
        )
        StatCardEnhanced(
            modifier = Modifier.weight(1f),
            title = "Today's Earnings",
            value = earningsValue,
            icon = Icons.Outlined.History,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            accentColor = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StatCardEnhanced(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp), 
                    tint = accentColor
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = value, 
                style = MaterialTheme.typography.headlineSmall, 
                fontWeight = FontWeight.Black,
                color = contentColor
            )
            Text(
                text = title, 
                style = MaterialTheme.typography.labelSmall, 
                color = contentColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AppointmentsRow(
    appointments: List<AppointmentDto>,
    onAppointmentClick: (AppointmentDto) -> Unit
) {
    if (appointments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "No appointments scheduled for today", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(appointments, key = { _, item -> item.id }) { index, appointment ->
            val containerColor = if (index % 2 == 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (index % 2 == 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            
            AppointmentSmallCard(
                appointment = appointment, 
                containerColor = containerColor,
                contentColor = contentColor,
                onClick = onAppointmentClick
            )
        }
    }
}

@Composable
private fun AppointmentSmallCard(
    appointment: AppointmentDto,
    containerColor: Color,
    contentColor: Color,
    onClick: (AppointmentDto) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick(appointment) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(40.dp), 
                shape = CircleShape, 
                color = contentColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person, 
                        contentDescription = null, 
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = appointment.patient.name, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            Text(
                text = appointment.appointmentTime, 
                style = MaterialTheme.typography.labelSmall, 
                color = contentColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).clickable { onActionClick() }.padding(4.dp)
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun LoadingScheduleRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun ErrorCard(msg: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), 
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = msg, 
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun NoAppointmentsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No appointments scheduled",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Your schedule is currently clear for today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockDoctor = UserDto(
        id = "1",
        name = "Aarti Mishra",
        email = "aarti.mishra@healthcare.com",
        role = "DOCTOR"
    )
    val mockAppointments = listOf(
        AppointmentDto(
            id = "1",
            doctor = mockDoctor,
            patient = PatientDto("1", "Rahul Kumar", "rahul@gmail.com", "PATIENT", 28, "Male", "O+"),
            appointmentDate = "Today",
            appointmentTime = "10:30 AM",
            status = "BOOKED"
        )
    )
    HealthcareTheme {
        HomeScreen(
            doctor = mockDoctor,
            uiState = UiState.Success(
                TodaysAppointmentsResponse(
                    appointments = mockAppointments,
                    totalEarnings = 1400.0
                )
            )
        )
    }
}
