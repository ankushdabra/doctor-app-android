package com.doctor.app.appointments.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.api.PatientDto
import com.doctor.app.appointments.viewmodel.PrescriptionViewModel
import com.doctor.app.appointments.viewmodel.PrescriptionViewModelFactory
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.core.ui.theme.PrimaryLight
import com.doctor.app.core.ui.theme.SecondaryLight
import com.doctor.app.login.api.UserDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointment: AppointmentDto,
    onBackClick: () -> Unit
) {
    var showPrescriptionScreen by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val repository = remember { AppointmentRepository(tokenManager) }
    val prescriptionViewModel: PrescriptionViewModel = viewModel(
        factory = PrescriptionViewModelFactory(repository)
    )

    // Handle internal back press when in prescription mode
    BackHandler(enabled = showPrescriptionScreen) {
        showPrescriptionScreen = false
    }

    AnimatedContent(
        targetState = showPrescriptionScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "PrescriptionTransition"
    ) { isPrescribing ->
        if (isPrescribing) {
            CreatePrescriptionScreen(
                appointment = appointment,
                viewModel = prescriptionViewModel,
                onBackClick = { showPrescriptionScreen = false },
                onPrescriptionCreated = {
                    showPrescriptionScreen = false
                    onBackClick()
                }
            )
        } else {
            AppointmentDetailContent(
                appointment = appointment,
                onBackClick = onBackClick,
                onStartConsultation = { showPrescriptionScreen = true }
            )
        }
    }
}

@Composable
fun AppointmentDetailContent(
    appointment: AppointmentDto,
    onBackClick: () -> Unit,
    onStartConsultation: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailHeader(
                    appointment = appointment
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Patient Metrics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Patient Metrics",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Outlined.MonitorHeart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(), 
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Age",
                                    value = "${appointment.patient.age ?: "N/A"}",
                                    unit = "years",
                                    icon = Icons.Outlined.Badge,
                                    color = Color(0xFF64B5F6)
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Blood",
                                    value = appointment.patient.bloodGroup ?: "N/A",
                                    unit = "group",
                                    icon = Icons.Outlined.WaterDrop,
                                    color = Color(0xFFE57373)
                                )
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(), 
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Weight",
                                    value = "72", 
                                    unit = "kg",
                                    icon = Icons.Outlined.Scale,
                                    color = Color(0xFF81C784)
                                )
                                MetricBox(
                                    modifier = Modifier.weight(1f),
                                    label = "Height",
                                    value = "175", 
                                    unit = "cm",
                                    icon = Icons.Outlined.Straighten,
                                    color = Color(0xFFFFB74D)
                                )
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Contact Email",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = appointment.patient.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = onStartConsultation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Start Consultation", 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { /* Handle Reschedule */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text("Reschedule", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { /* Handle Cancel */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF422222) else Color(0xFFFFEBEE),
                                contentColor = if (isDark) Color(0xFFFFB4B4) else Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Fixed Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DetailHeader(
    appointment: AppointmentDto
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
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
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(88.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = appointment.patient.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = appointment.status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))

            // Integrated Schedule Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScheduleItem(
                        icon = Icons.Default.CalendarToday,
                        value = appointment.appointmentDate,
                        isFloating = false
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    ScheduleItem(
                        icon = Icons.Default.Schedule,
                        value = appointment.appointmentTime,
                        isFloating = false
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleItem(icon: ImageVector, value: String, isFloating: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(22.dp),
            tint = if (isFloating) MaterialTheme.colorScheme.primary else Color.White
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Bold,
            color = if (isFloating) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppointmentDetailScreenPreview() {
    val mockAppointment = AppointmentDto(
        id = "10245",
        doctor = UserDto("1", "Dr. Aarti Mishra", "aarti@example.com", "DOCTOR", 23),
        patient = PatientDto("P101", "Rahul Kumar", "rahul@example.com", "PATIENT", 28, "Male", "O+"),
        appointmentDate = "27 Oct, 2023",
        appointmentTime = "10:30 AM",
        status = "BOOKED"
    )
    HealthcareTheme(darkTheme = false) {
        AppointmentDetailScreen(appointment = mockAppointment, onBackClick = {})
    }
}
