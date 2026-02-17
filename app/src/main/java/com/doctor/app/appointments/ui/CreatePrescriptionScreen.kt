package com.doctor.app.appointments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
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

private val SAMPLE_MEDICINES = listOf(
    "Paracetamol 500mg",
    "Amoxicillin 500mg",
    "Ibuprofen 400mg",
    "Cetirizine 10mg",
    "Metformin 500mg",
    "Atorvastatin 20mg",
    "Omeprazole 20mg",
    "Amlodipine 5mg",
    "Losartan 50mg",
    "Albuterol Inhaler",
    "Azithromycin 250mg",
    "Lisinopril 10mg",
    "Gabapentin 300mg",
    "Hydrochlorothiazide 25mg",
    "Sertraline 50mg"
)

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
    var selectedMedications by remember { mutableStateOf(emptyList<String>()) }
    var medicationInput by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val submitPrescription = {
        val finalMeds = if (medicationInput.isNotBlank()) {
            if (selectedMedications.contains(medicationInput)) {
                selectedMedications.joinToString(", ")
            } else {
                (selectedMedications + medicationInput).joinToString(", ")
            }
        } else {
            selectedMedications.joinToString(", ")
        }
        if (finalMeds.isNotBlank()) {
            onSubmit(finalMeds, instructions, notes)
        }
    }

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
                        value = medicationInput,
                        onValueChange = { medicationInput = it },
                        placeholder = if (selectedMedications.isEmpty()) "Enter medications (e.g., Paracetamol 500mg)" else "Add more medications...",
                        icon = Icons.Default.Medication,
                        suggestions = SAMPLE_MEDICINES,
                        selectedItems = selectedMedications,
                        onItemAdd = { medication ->
                            if (medication.isNotBlank() && !selectedMedications.contains(medication)) {
                                selectedMedications = selectedMedications + medication
                            }
                        },
                        onItemRemove = { medication ->
                            selectedMedications = selectedMedications - medication
                        },
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = {
                                if (medicationInput.isNotBlank()) {
                                    if (!selectedMedications.contains(medicationInput)) {
                                        selectedMedications = selectedMedications + medicationInput
                                    }
                                    medicationInput = ""
                                } else {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            }
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
                                submitPrescription()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submitPrescription()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = uiState !is UiState.Loading && (selectedMedications.isNotEmpty() || medicationInput.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (uiState is UiState.Loading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PrescriptionInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    suggestions: List<String> = emptyList(),
    selectedItems: List<String> = emptyList(),
    onItemAdd: (String) -> Unit = {},
    onItemRemove: (String) -> Unit = {},
    imeAction: androidx.compose.ui.text.input.ImeAction = androidx.compose.ui.text.input.ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, selectedItems) {
        if (value.isBlank()) emptyList()
        else suggestions.filter { 
            it.contains(value, ignoreCase = true) && !selectedItems.contains(it) && it != value 
        }
    }

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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedItems.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    selectedItems.forEach { item ->
                        InputChip(
                            selected = true,
                            onClick = { },
                            label = { Text(item, style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onItemRemove(item) }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }
            
            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded && filteredSuggestions.isNotEmpty(),
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { 
                            onValueChange(it)
                            expanded = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true),
                        placeholder = {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = imeAction),
                        keyboardActions = keyboardActions,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && filteredSuggestions.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    onItemAdd(suggestion)
                                    onValueChange("")
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            } else {
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
