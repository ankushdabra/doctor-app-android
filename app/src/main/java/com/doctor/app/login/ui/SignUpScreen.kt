package com.doctor.app.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.R
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.core.ui.theme.PrimaryLight
import com.doctor.app.core.ui.theme.SecondaryLight
import com.doctor.app.login.api.AuthenticationRepository
import com.doctor.app.login.api.DoctorSignUpRequestDto
import com.doctor.app.login.api.TimeSlotDto
import com.doctor.app.login.viewmodel.SignUpViewModel
import com.doctor.app.login.viewmodel.SignUpViewModelFactory

enum class SignUpStep {
    PersonalDetails,
    Availability
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    tokenManager: TokenManager,
    onRegistrationSuccess: () -> Unit
) {
    val repository = AuthenticationRepository(tokenManager)
    val viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModelFactory(repository, tokenManager)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    SignUpContent(
        state = state,
        onRegisterClick = viewModel::registerDoctor,
        onRegistrationSuccess = onRegistrationSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    state: UiState<Boolean>,
    onRegisterClick: (DoctorSignUpRequestDto) -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(SignUpStep.PersonalDetails) }

    // Form data
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf<Map<String, List<TimeSlotDto>>>(emptyMap()) }

    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var specializationError by remember { mutableStateOf<String?>(null) }
    var qualificationError by remember { mutableStateOf<String?>(null) }
    var experienceError by remember { mutableStateOf<String?>(null) }
    var consultationFeeError by remember { mutableStateOf<String?>(null) }
    var aboutError by remember { mutableStateOf<String?>(null) }
    var clinicAddressError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state) {
        if (state is UiState.Success && state.data) {
            onRegistrationSuccess()
        }
    }

    fun validatePersonalDetails(): Boolean {
        var isValid = true
        if (name.isBlank()) {
            nameError = "Name is required"; isValid = false
        } else nameError = null
        if (email.isBlank()) {
            emailError = "Email is required"; isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid format"; isValid = false
        } else emailError = null
        if (password.isBlank() || password.length < 6) {
            passwordError = "Min 6 characters"; isValid = false
        } else passwordError = null
        if (specialization.isBlank()) {
            specializationError = "Required"; isValid = false
        } else specializationError = null
        if (qualification.isBlank()) {
            qualificationError = "Required"; isValid = false
        } else qualificationError = null
        if (experience.isBlank() || experience.toIntOrNull() == null) {
            experienceError = "Enter number"; isValid = false
        } else experienceError = null
        if (consultationFee.isBlank() || consultationFee.toDoubleOrNull() == null) {
            consultationFeeError = "Enter amount"; isValid = false
        } else consultationFeeError = null
        if (about.isBlank()) {
            aboutError = "Required"; isValid = false
        } else aboutError = null
        if (clinicAddress.isBlank()) {
            clinicAddressError = "Required"; isValid = false
        } else clinicAddressError = null
        return isValid
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background Elements
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(bottomStart = 64.dp, bottomEnd = 64.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryLight, SecondaryLight.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Logo with Glow
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_healthcare_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.padding(14.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Doctor Registration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Text(
                    text = if (currentStep == SignUpStep.PersonalDetails) "Step 1: Professional Details" else "Step 2: Availability Slots",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (currentStep == SignUpStep.PersonalDetails) {
                            PersonalDetailsForm(
                                name = name,
                                onNameChange = { name = it; nameError = null },
                                nameError = nameError,
                                email = email,
                                onEmailChange = { email = it; emailError = null },
                                emailError = emailError,
                                password = password,
                                onPasswordChange = { password = it; passwordError = null },
                                passwordError = passwordError,
                                specialization = specialization,
                                onSpecializationChange = {
                                    specialization = it; specializationError = null
                                },
                                specializationError = specializationError,
                                qualification = qualification,
                                onQualificationChange = {
                                    qualification = it; qualificationError = null
                                },
                                qualificationError = qualificationError,
                                experience = experience,
                                onExperienceChange = { experience = it; experienceError = null },
                                experienceError = experienceError,
                                consultationFee = consultationFee,
                                onConsultationFeeChange = {
                                    consultationFee = it; consultationFeeError = null
                                },
                                consultationFeeError = consultationFeeError,
                                clinicAddress = clinicAddress,
                                onClinicAddressChange = {
                                    clinicAddress = it; clinicAddressError = null
                                },
                                clinicAddressError = clinicAddressError,
                                about = about,
                                onAboutChange = { about = it; aboutError = null },
                                aboutError = aboutError
                            )

                            Spacer(Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    if (validatePersonalDetails()) currentStep = SignUpStep.Availability
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text("Next Step", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        } else {
                            AvailabilityForm(
                                availability = availability,
                                onAvailabilityChange = { availability = it }
                            )

                            Spacer(Modifier.height(32.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { currentStep = SignUpStep.PersonalDetails },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Back", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        onRegisterClick(
                                            DoctorSignUpRequestDto(
                                                name = name,
                                                email = email,
                                                password = password,
                                                specialization = specialization,
                                                qualification = qualification,
                                                experience = experience.toIntOrNull() ?: 0,
                                                consultationFee = consultationFee.toDoubleOrNull()
                                                    ?: 0.0,
                                                about = about,
                                                clinicAddress = clinicAddress,
                                                availability = availability
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = state !is UiState.Loading,
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                                ) {
                                    if (state is UiState.Loading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Complete", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (state is UiState.Error) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsForm(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    specialization: String,
    onSpecializationChange: (String) -> Unit,
    specializationError: String?,
    qualification: String,
    onQualificationChange: (String) -> Unit,
    qualificationError: String?,
    experience: String,
    onExperienceChange: (String) -> Unit,
    experienceError: String?,
    consultationFee: String,
    onConsultationFeeChange: (String) -> Unit,
    consultationFeeError: String?,
    clinicAddress: String,
    onClinicAddressChange: (String) -> Unit,
    clinicAddressError: String?,
    about: String,
    onAboutChange: (String) -> Unit,
    aboutError: String?
) {
    var specializationExpanded by remember { mutableStateOf(false) }
    var qualificationExpanded by remember { mutableStateOf(false) }
    var experienceExpanded by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val specializationOptions = listOf(
        "General Physician",
        "Cardiologist",
        "Dermatologist",
        "Neurologist",
        "Pediatrician",
        "Psychiatrist"
    )
    val qualificationOptions = listOf("MBBS", "MD", "MS", "DNB", "PhD")
    val experienceOptions = (1..40).map { "$it" }

    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Full Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        isError = nameError != null,
        supportingText = nameError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email Address") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        isError = emailError != null,
        supportingText = emailError?.let { { Text(it) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null)
            }
        },
        isError = passwordError != null,
        supportingText = passwordError?.let { { Text(it) } },
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    Spacer(Modifier.height(16.dp))

    ExposedDropdownMenuBox(
        expanded = specializationExpanded,
        onExpandedChange = { specializationExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = specialization,
            onValueChange = onSpecializationChange,
            label = { Text("Specialization") },
            leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            isError = specializationError != null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specializationExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        ExposedDropdownMenu(
            expanded = specializationExpanded,
            onDismissRequest = { specializationExpanded = false }) {
            specializationOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSpecializationChange(option); specializationExpanded = false })
            }
        }
    }
    Spacer(Modifier.height(12.dp))

    ExposedDropdownMenuBox(
        expanded = qualificationExpanded,
        onExpandedChange = { qualificationExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = qualification,
            onValueChange = onQualificationChange,
            label = { Text("Qualification") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            isError = qualificationError != null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualificationExpanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        ExposedDropdownMenu(
            expanded = qualificationExpanded,
            onDismissRequest = { qualificationExpanded = false }) {
            qualificationOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onQualificationChange(option); qualificationExpanded = false })
            }
        }
    }
    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ExposedDropdownMenuBox(
            expanded = experienceExpanded,
            onExpandedChange = { experienceExpanded = it },
            modifier = Modifier.weight(2f)
        ) {
            OutlinedTextField(
                value = experience,
                onValueChange = {},
                readOnly = true,
                label = { Text("Exp. (Yrs)", maxLines = 1) },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                isError = experienceError != null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = experienceExpanded) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) })
            )
            ExposedDropdownMenu(
                expanded = experienceExpanded,
                onDismissRequest = { experienceExpanded = false }) {
                experienceOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onExperienceChange(option); experienceExpanded = false })
                }
            }
        }
        OutlinedTextField(
            value = consultationFee,
            onValueChange = onConsultationFeeChange,
            label = { Text("Fee (₹)") },
            isError = consultationFeeError != null,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = clinicAddress,
        onValueChange = onClinicAddressChange,
        label = { Text("Clinic Address") },
        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        isError = clinicAddressError != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = about,
        onValueChange = onAboutChange,
        label = { Text("About Yourself") },
        isError = aboutError != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        minLines = 3,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityForm(
    availability: Map<String, List<TimeSlotDto>>,
    onAvailabilityChange: (Map<String, List<TimeSlotDto>>) -> Unit
) {
    val days = listOf(
        "Monday" to "MON",
        "Tuesday" to "TUE",
        "Wednesday" to "WED",
        "Thursday" to "THU",
        "Friday" to "FRI",
        "Saturday" to "SAT",
        "Sunday" to "SUN"
    )
    var showTimePicker by remember { mutableStateOf<Triple<String, Int, Boolean>?>(null) } // Day Key (API), Index, isStart

    showTimePicker?.let { (dayKey, index, isStart) ->
        val timePickerState = rememberTimePickerState()
        Dialog(onDismissRequest = { showTimePicker = null }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isStart) "Start Time" else "End Time",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = null }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = {
                            val hour = timePickerState.hour
                            val minute = timePickerState.minute
                            val amPm = if (hour < 12) "AM" else "PM"
                            val hour12 = if (hour % 12 == 0) 12 else hour % 12
                            val newTime = String.format("%02d:%02d %s", hour12, minute, amPm)
                            
                            val daySlots = availability[dayKey]?.toMutableList() ?: mutableListOf()
                            val currentSlot = daySlots[index]
                            val newSlot = if (isStart) {
                                currentSlot.copy(startTime = newTime)
                            } else {
                                currentSlot.copy(endTime = newTime)
                            }
                            daySlots[index] = newSlot
                            onAvailabilityChange(availability + (dayKey to daySlots))
                            showTimePicker = null
                        }) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Weekly Availability",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        days.forEach { (displayName, apiKey) ->
            val slots = availability[apiKey] ?: emptyList()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            displayName,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = {
                            val currentSlots = slots.toMutableList()
                            currentSlots.add(TimeSlotDto("09:00 AM", "05:00 PM"))
                            onAvailabilityChange(availability + (apiKey to currentSlots))
                        }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add slot", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    slots.forEachIndexed { index, slot ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            TimeSlotChip(
                                text = slot.startTime,
                                onClick = { showTimePicker = Triple(apiKey, index, true) }
                            )
                            Text(" to ", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodySmall)
                            TimeSlotChip(
                                text = slot.endTime,
                                onClick = { showTimePicker = Triple(apiKey, index, false) }
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                val currentSlots = slots.toMutableList()
                                currentSlots.removeAt(index)
                                onAvailabilityChange(availability + (apiKey to currentSlots))
                            }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    if (slots.isEmpty()) {
                        Text(
                            "Not available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.width(90.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_6)
@Composable
fun SignUpScreenPreview() {
    HealthcareTheme {
        SignUpContent(
            state = UiState.Success(false),
            onRegisterClick = {},
            onRegistrationSuccess = {})
    }
}
