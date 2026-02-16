package com.doctor.app.appointments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doctor.app.appointments.api.AppointmentDto
import com.doctor.app.appointments.api.PatientDto
import com.doctor.app.appointments.viewmodel.PrescriptionViewModel
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.login.api.UserDto

@Composable
fun CreatePrescriptionScreen(
    appointment: AppointmentDto,
    viewModel: PrescriptionViewModel,
    onBackClick: () -> Unit,
    onPrescriptionCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CreatePrescriptionContent(
        appointment = appointment,
        uiState = uiState,
        onBackClick = onBackClick,
        onPrescriptionCreated = onPrescriptionCreated,
        onSubmit = { medications, instructions, notes ->
            viewModel.createPrescription(
                patientId = appointment.patient.id,
                doctorId = appointment.doctor.id,
                appointmentId = appointment.id,
                medications = medications,
                instructions = instructions,
                notes = notes
            )
        }
    )
}

@Composable
fun CreatePrescriptionContent(
    appointment: AppointmentDto,
    uiState: UiState<Boolean>,
    onBackClick: () -> Unit,
    onPrescriptionCreated: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var medications by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                if (uiState.data) {
                    onPrescriptionCreated()
                }
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrescriptionHeader(appointment = appointment)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Prescription Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    PrescriptionInputCard(
                        label = "Medications",
                        value = medications,
                        onValueChange = { medications = it },
                        placeholder = "Enter medications (e.g., Paracetamol 500mg)",
                        icon = Icons.Default.Medication,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrescriptionInputCard(
                        label = "Instructions",
                        value = instructions,
                        onValueChange = { instructions = it },
                        placeholder = "Enter instructions (e.g., Twice a day after meals)",
                        icon = Icons.Default.Description,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrescriptionInputCard(
                        label = "Additional Notes",
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Any additional notes or symptoms",
                        icon = Icons.AutoMirrored.Filled.Notes,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (medications.isNotBlank()) {
                                    onSubmit(medications, instructions, notes)
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSubmit(medications, instructions, notes)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = uiState !is UiState.Loading && medications.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF002E69),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState is UiState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Submit Prescription",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
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
private fun PrescriptionHeader(appointment: AppointmentDto) {
    // consistently deep, professional medical navy gradient requested
    val darkHeaderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF002E69), // Deep Navy
            Color(0xFF004494)  // Professional Blue
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(brush = darkHeaderBrush)
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .offset(x = 280.dp, y = (-40).dp)
                .size(200.dp)
                .background(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "New Prescription for",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = appointment.patient.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PrescriptionInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF002E69),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF002E69)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = keyboardActions
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePrescriptionScreenPreview() {
    val appointment = AppointmentDto(
        id = "1",
        doctor = UserDto(
            id = "d1",
            name = "Dr. Smith",
            email = "smith@example.com",
            role = "DOCTOR"
        ),
        patient = PatientDto(
            id = "p1",
            name = "John Doe",
            email = "john@example.com",
            role = "PATIENT",
            weight = 70.0,
            height = 175.0
        ),
        appointmentDate = "2024-08-15",
        appointmentTime = "10:00 AM",
        status = "Confirmed"
    )

    HealthcareTheme {
        CreatePrescriptionContent(
            appointment = appointment,
            uiState = UiState.Success(false),
            onBackClick = {},
            onPrescriptionCreated = {},
            onSubmit = { _, _, _ -> }
        )
    }
}
