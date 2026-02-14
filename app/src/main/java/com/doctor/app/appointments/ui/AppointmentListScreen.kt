package com.doctor.app.appointments.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.api.PatientDto
import com.doctor.app.appointments.viewmodel.AppointmentViewModel
import com.doctor.app.appointments.viewmodel.AppointmentViewModelFactory
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.components.LoadingState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.core.ui.theme.PrimaryLight
import com.doctor.app.core.ui.theme.SecondaryLight
import com.doctor.app.login.api.UserDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AppointmentListScreen(
    tokenManager: TokenManager,
    onAppointmentClick: (AppointmentDto) -> Unit = {}
) {
    val repository = AppointmentRepository(tokenManager)
    val viewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingState()
        is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message) }
        is UiState.Success -> AppointmentListContent(state.data, onAppointmentClick)
    }
}

@Composable
private fun AppointmentListContent(
    appointments: List<AppointmentDto>,
    onAppointmentClick: (AppointmentDto) -> Unit
) {
    val groupedAppointments = remember(appointments) {
        appointments.groupBy { it.appointmentDate }.toSortedMap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            AppointmentHeader(appointments.size)
            Spacer(Modifier.height(16.dp))
        }

        groupedAppointments.forEach { (date, appointmentsForDate) ->
            item {
                DateHeader(date)
                Spacer(Modifier.height(8.dp))
            }
            items(appointmentsForDate) { appointment ->
                AppointmentItemCard(appointment, onAppointmentClick)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DateHeader(date: String) {
    val displayDate = remember(date) { getDisplayDate(date) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = displayDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (displayDate == "Today" || displayDate == "Tomorrow") {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getDisplayDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        when (date) {
            today -> "Today"
            tomorrow -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun AppointmentHeader(count: Int) {
    val isDark = isSystemInDarkTheme()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    },
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .offset(x = 280.dp, y = (-40).dp)
                .size(200.dp)
                .background(
                    color = Color.White.copy(alpha = if (isDark) 0.04f else 0.08f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .offset(x = (-30).dp, y = 110.dp)
                .size(120.dp)
                .background(
                    color = Color.White.copy(alpha = if (isDark) 0.03f else 0.05f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 64.dp, end = 24.dp, bottom = 48.dp)
        ) {
            Text(
                text = "Appointments",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(Modifier.height(12.dp))
            
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "You have $count scheduled visits",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AppointmentItemCard(
    appointment: AppointmentDto,
    onClick: (AppointmentDto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick(appointment) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.patient.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = "${appointment.patient.gender} • ${appointment.patient.age} yrs • ${appointment.patient.bloodGroup}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(10.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = appointment.appointmentTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (appointment.status == "BOOKED") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (appointment.status == "BOOKED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentListScreenPreview() {
    val today = LocalDate.now().toString()
    val tomorrow = LocalDate.now().plusDays(1).toString()

    val mockAppointments = listOf(
        AppointmentDto(
            id = "1",
            doctor = UserDto("1", "Dr. Rahul Mehta", "rahul@example.com", "DOCTOR"),
            patient = PatientDto("1", "Alice Brown", "alice@gmail.com", "PATIENT", 21, "Female", "O+"),
            appointmentDate = today,
            appointmentTime = "09:00 AM",
            status = "BOOKED"
        ),
        AppointmentDto(
            id = "2",
            doctor = UserDto("1", "Dr. Rahul Mehta", "rahul@example.com", "DOCTOR"),
            patient = PatientDto("2", "John Smith", "john@gmail.com", "PATIENT", 45, "Male", "A+"),
            appointmentDate = today,
            appointmentTime = "10:30 AM",
            status = "PENDING"
        ),
        AppointmentDto(
            id = "3",
            doctor = UserDto("1", "Dr. Rahul Mehta", "rahul@example.com", "DOCTOR"),
            patient = PatientDto("3", "Jane Doe", "jane@gmail.com", "PATIENT", 30, "Female", "B+"),
            appointmentDate = tomorrow,
            appointmentTime = "11:00 AM",
            status = "BOOKED"
        )
    )
    HealthcareTheme {
        AppointmentListContent(mockAppointments, {})
    }
}
