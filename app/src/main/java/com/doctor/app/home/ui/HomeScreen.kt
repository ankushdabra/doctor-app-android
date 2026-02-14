package com.doctor.app.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.core.ui.theme.PrimaryLight
import com.doctor.app.core.ui.theme.SecondaryLight
import com.doctor.app.home.viewmodel.HomeViewModel
import com.doctor.app.home.viewmodel.HomeViewModelFactory
import com.doctor.app.login.api.UserDto
import java.util.Calendar

@Composable
fun HomeScreen(
    doctor: UserDto,
    uiState: UiState<List<AppointmentDto>>,
    onViewAllClick: () -> Unit = {},
    onAppointmentClick: (AppointmentDto) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeaderSection(doctor.name)
        }

        item {
            when (val state = uiState) {
                is UiState.Success -> {
                    val nextAppointment = state.data.firstOrNull { it.status == "BOOKED" }
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
                is UiState.Success -> AppointmentsRow(state.data, onAppointmentClick)
                is UiState.Loading -> Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() }
                is UiState.Error -> Text("Failed to load schedule", modifier = Modifier.padding(20.dp))
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
    val repository = AppointmentRepository(tokenManager)
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    // Collect doctor profile from cache
    val doctor by tokenManager.userDetails.collectAsState(initial = null)
    
    if (doctor != null) {
        HomeScreen(
            doctor = doctor!!,
            uiState = uiState,
            onViewAllClick = onViewAllClick,
            onAppointmentClick = onAppointmentClick
        )
    } else {
        // Fallback loading state while cache is being read
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun HeaderSection(name: String) {
    val displayName = if (name.startsWith("Dr.", ignoreCase = true)) name else "Dr. $name"
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(50.dp),
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
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "$salutation,",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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
    val contentColor = Color.White
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryLight, SecondaryLight.copy(alpha = 0.8f)),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 260.dp, y = (-30).dp)
                    .size(180.dp)
                    .background(color = Color.White.copy(alpha = 0.08f), shape = CircleShape)
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Surface(
                            color = contentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "NEXT PATIENT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                fontWeight = FontWeight.Bold
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
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Badge, 
                                contentDescription = null, 
                                tint = contentColor, 
                                modifier = Modifier.size(32.dp)
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = appointment.appointmentTime, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = contentColor
                        )
                    }
                    
                    Button(
                        onClick = { onStartClick(appointment) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = contentColor, 
                            contentColor = PrimaryLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(uiState: UiState<List<AppointmentDto>>) {
    val isDark = isSystemInDarkTheme()
    val count = if (uiState is UiState.Success) uiState.data.size.toString() else "--"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCardEnhanced(
            modifier = Modifier.weight(1f),
            title = "Patients Today",
            value = count,
            icon = Icons.Outlined.Group,
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFE3F2FD)
        )
        StatCardEnhanced(
            modifier = Modifier.weight(1f),
            title = "Total Earnings",
            value = "₹8.4k",
            icon = Icons.Outlined.History,
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F8E9)
        )
    }
}

@Composable
private fun StatCardEnhanced(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AppointmentsRow(
    appointments: List<AppointmentDto>,
    onAppointmentClick: (AppointmentDto) -> Unit
) {
    if (appointments.isEmpty()) {
        Text("No appointments for today", modifier = Modifier.padding(20.dp))
        return
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(appointments) { appointment ->
            AppointmentSmallCard(appointment, onAppointmentClick)
        }
    }
}

@Composable
private fun AppointmentSmallCard(
    appointment: AppointmentDto,
    onClick: (AppointmentDto) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick(appointment) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(appointment.patient.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(appointment.appointmentTime, style = MaterialTheme.typography.labelSmall, color = PrimaryLight)
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
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 20.dp), shape = RoundedCornerShape(28.dp)) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    }
}

@Composable
private fun ErrorCard(msg: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(28.dp)) {
        Text(msg, modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun NoAppointmentsCard() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(28.dp)) {
        Text("No appointments scheduled for today", modifier = Modifier.padding(24.dp))
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
    HealthcareTheme {
        HomeScreen(
            doctor = mockDoctor,
            uiState = UiState.Success(emptyList())
        )
    }
}
