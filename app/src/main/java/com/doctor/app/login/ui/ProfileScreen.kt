package com.doctor.app.login.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.components.LoadingState
import com.doctor.app.core.ui.theme.HealthcareTheme
import com.doctor.app.login.api.AuthenticationRepository
import com.doctor.app.login.api.DoctorDetailsDto
import com.doctor.app.login.api.ProfileUpdateRequestDto
import com.doctor.app.login.api.TimeSlotDto
import com.doctor.app.login.api.UserDto
import com.doctor.app.login.viewmodel.ProfileViewModel
import com.doctor.app.login.viewmodel.ProfileViewModelFactory
import java.util.Locale

@Composable
fun ProfileScreen(
    tokenManager: TokenManager
) {
    val repository = AuthenticationRepository(tokenManager)
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(repository, tokenManager)
    )

    // Primary source: collect from application cache
    val doctor by tokenManager.userDetails.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSystemInDarkTheme()) {
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                )
            )
    ) {
        if (doctor != null) {
            // Instant render using cached data
            ProfileContent(
                user = doctor!!,
                themeMode = themeMode,
                updateState = updateState,
                onThemeChange = viewModel::setThemeMode,
                onUpdateProfile = viewModel::updateProfile,
                onLogoutClick = viewModel::logout
            )
        } else {
            // Fallback to VM state if cache is empty (e.g. first time or cleared)
            when (val state = uiState) {
                is UiState.Loading -> LoadingState()
                is UiState.Error -> ProfileErrorState(
                    onRetry = viewModel::loadProfile
                )

                is UiState.Success -> {
                    ProfileContent(
                        user = state.data,
                        themeMode = themeMode,
                        updateState = updateState,
                        onThemeChange = viewModel::setThemeMode,
                        onUpdateProfile = viewModel::updateProfile,
                        onLogoutClick = viewModel::logout
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileErrorState(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Unable to load profile. Please check your internet connection and try again.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    user: UserDto,
    themeMode: String,
    updateState: UiState<Unit>,
    onThemeChange: (String) -> Unit,
    onUpdateProfile: (ProfileUpdateRequestDto) -> Unit,
    onLogoutClick: () -> Unit
) {
    val details = user.doctorDetails
    val displayName =
        if (user.name.startsWith("Dr.", ignoreCase = true)) user.name else "Dr. ${user.name}"
    val isDark = isSystemInDarkTheme()

    var isEditing by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Unified states for professional info & availability
    var availability by remember(user.doctorDetails?.availability) {
        mutableStateOf(user.doctorDetails?.availability ?: emptyMap())
    }
    var specialization by remember(details) { mutableStateOf(details?.specialization ?: "") }
    var qualification by remember(details) { mutableStateOf(details?.qualification ?: "") }
    var experience by remember(details) { mutableStateOf(details?.experience?.toString() ?: "0") }
    var consultationFee by remember(details) {
        mutableStateOf(
            details?.consultationFee?.toString() ?: "0.0"
        )
    }
    var clinicAddress by remember(details) { mutableStateOf(details?.clinicAddress ?: "") }
    var about by remember(details) { mutableStateOf(details?.about ?: "") }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success && isEditing) {
            isEditing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Consistent dark navy gradient for header as requested
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
                // Top Bar with Settings and Cancel (when editing)
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        if (isEditing) {
                            IconButton(onClick = {
                                // Reset values on cancel
                                availability = user.doctorDetails?.availability ?: emptyMap()
                                specialization = details?.specialization ?: ""
                                qualification = details?.qualification ?: ""
                                experience = details?.experience?.toString() ?: "0"
                                consultationFee = details?.consultationFee?.toString() ?: "0.0"
                                clinicAddress = details?.clinicAddress ?: ""
                                about = details?.about ?: ""
                                isEditing = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Edit",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false }
                            ) {
                                Text(
                                    text = "App Settings",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                val options = listOf("LIGHT", "DARK", "FOLLOW_SYSTEM")
                                val labels = listOf("Light Mode", "Dark Mode", "System Default")
                                val icons = listOf(
                                    Icons.Outlined.LightMode,
                                    Icons.Outlined.DarkMode,
                                    Icons.Outlined.SettingsSuggest
                                )

                                options.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = { Text(labels[index]) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = icons[index],
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            onThemeChange(option)
                                            showSettingsMenu = false
                                        },
                                        trailingIcon = {
                                            if (themeMode == option) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "Sign Out",
                                            color = MaterialTheme.colorScheme.error
                                        ) 
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Logout,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        onLogoutClick()
                                        showSettingsMenu = false
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // Decorative background elements
                Box(
                    modifier = Modifier
                        .offset(x = 280.dp, y = (-40).dp)
                        .size(200.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.06f),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .offset(x = (-30).dp, y = 110.dp)
                        .size(120.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = CircleShape
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = specialization.ifEmpty { details?.specialization ?: "Healthcare Professional" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- Content Section ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Professional Details Section
                Text(
                    text = "Professional Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isEditing) {
                            OutlinedTextField(
                                value = specialization,
                                onValueChange = { specialization = it },
                                label = { Text("Specialization") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = qualification,
                                onValueChange = { qualification = it },
                                label = { Text("Qualification") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = experience,
                                onValueChange = { experience = it },
                                label = { Text("Experience (Years)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = consultationFee,
                                onValueChange = { consultationFee = it },
                                label = { Text("Consultation Fee") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = clinicAddress,
                                onValueChange = { clinicAddress = it },
                                label = { Text("Clinic Address") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                            OutlinedTextField(
                                value = about,
                                onValueChange = { about = it },
                                label = { Text("About") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 5
                            )
                        } else {
                            ProfileDetailRow(
                                icon = Icons.Outlined.Star,
                                label = "Specialization",
                                value = details?.specialization ?: "Not specified"
                            )
                            ProfileDetailRow(
                                icon = Icons.Outlined.School,
                                label = "Qualification",
                                value = details?.qualification ?: "Not specified"
                            )
                            ProfileDetailRow(
                                icon = Icons.Outlined.History,
                                label = "Experience",
                                value = "${details?.experience ?: 0} Years"
                            )
                            ProfileDetailRow(
                                icon = Icons.Outlined.CurrencyRupee,
                                label = "Consultation Fee",
                                value = "₹${details?.consultationFee ?: 0.0}"
                            )
                            ProfileDetailRow(
                                icon = Icons.Outlined.Business,
                                label = "Clinic Address",
                                value = details?.clinicAddress ?: "Not specified"
                            )
                            ProfileDetailRow(
                                icon = Icons.Outlined.Info,
                                label = "About",
                                value = details?.about ?: "No description provided"
                            )
                        }
                    }
                }

                // Weekly Availability Section
                Text(
                    text = "Weekly Availability",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        AvailabilityEditor(
                            availability = availability,
                            isEditable = isEditing,
                            onAvailabilityChange = { availability = it }
                        )
                    }
                }

                if (updateState is UiState.Error) {
                    Text(
                        text = updateState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(80.dp)) // Extra space for FAB
            }
        }

        // --- Integrated Floating Action Button for Edit & Save ---
        ExtendedFloatingActionButton(
            onClick = {
                if (!isEditing) {
                    isEditing = true
                } else if (updateState !is UiState.Loading) {
                    val updateRequest = ProfileUpdateRequestDto(
                        name = user.name,
                        email = user.email,
                        age = user.age,
                        gender = user.gender,
                        bloodGroup = user.bloodGroup,
                        weight = user.weight,
                        height = user.height,
                        specialization = specialization,
                        qualification = qualification,
                        experience = experience.toIntOrNull() ?: 0,
                        consultationFee = consultationFee.toDoubleOrNull() ?: 0.0,
                        about = about,
                        clinicAddress = clinicAddress,
                        profileImage = details?.profileImage,
                        availability = availability
                    )
                    onUpdateProfile(updateRequest)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = {
                if (updateState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            },
            text = {
                Text(text = if (isEditing) "Save" else "Edit")
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityEditor(
    availability: Map<String, List<TimeSlotDto>>,
    isEditable: Boolean,
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

    var showTimePicker by remember { mutableStateOf<Triple<String, Int, Boolean>?>(null) }

    if (isEditable) {
        showTimePicker?.let { (dayKey, index, isStart) ->
            val daySlots = availability[dayKey] ?: emptyList()
            val currentSlot = daySlots.getOrNull(index)
            
            // Extract initial hour and minute from the existing string (e.g., "09:00" or "09:00 AM")
            val timeString = currentSlot?.let { if (isStart) it.startTime else it.endTime } ?: ""
            
            val initialHour = if (timeString.isNotEmpty()) {
                val parts = timeString.split(":")
                var h = parts.getOrNull(0)?.toIntOrNull() ?: 9
                if (timeString.contains("PM", ignoreCase = true) && h < 12) h += 12
                if (timeString.contains("AM", ignoreCase = true) && h == 12) h = 0
                h
            } else 9
            
            val initialMinute = if (timeString.isNotEmpty()) {
                val parts = timeString.split(":")
                if (parts.size > 1) {
                    parts[1].take(2).toIntOrNull() ?: 0
                } else 0
            } else 0

            val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = false
            )
            
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
                            text = if (isStart) "Select Start Time" else "Select End Time",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        TimePicker(state = timePickerState)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
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
                                val newTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                                
                                val updatedDaySlots = daySlots.toMutableList()
                                if (index < updatedDaySlots.size) {
                                    val slotToUpdate = updatedDaySlots[index]
                                    val newSlot = if (isStart) {
                                        slotToUpdate.copy(startTime = newTime)
                                    } else {
                                        slotToUpdate.copy(endTime = newTime)
                                    }
                                    updatedDaySlots[index] = newSlot
                                    onAvailabilityChange(availability + (dayKey to updatedDaySlots))
                                }
                                showTimePicker = null
                            }) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val visibleDays = if (isEditable) days else days.filter { (_, apiKey) -> 
            availability[apiKey]?.isNotEmpty() == true 
        }

        if (visibleDays.isEmpty() && !isEditingInAvailability(isEditable)) {
            Text(
                text = "No availability slots defined.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            visibleDays.forEach { (displayName, apiKey) ->
                val slots = availability[apiKey] ?: emptyList()
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isEditable) {
                            IconButton(
                                onClick = {
                                    val currentSlots = slots.toMutableList()
                                    currentSlots.add(TimeSlotDto("09:00 AM", "05:00 PM"))
                                    onAvailabilityChange(availability + (apiKey to currentSlots))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add slot",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (slots.isEmpty()) {
                        Text(
                            text = "No slots available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        slots.forEachIndexed { index, slot ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                ProfileTimeSlotChip(
                                    text = formatTimeForDisplay(slot.startTime),
                                    onClick = if (isEditable) {
                                        { showTimePicker = Triple(apiKey, index, true) }
                                    } else null
                                )
                                Text(
                                    text = "to",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                ProfileTimeSlotChip(
                                    text = formatTimeForDisplay(slot.endTime),
                                    onClick = if (isEditable) {
                                        { showTimePicker = Triple(apiKey, index, false) }
                                    } else null
                                )
                                if (isEditable) {
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = {
                                            val currentSlots = slots.toMutableList()
                                            currentSlots.removeAt(index)
                                            onAvailabilityChange(availability + (apiKey to currentSlots))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// Helper to format 24h string to AM/PM string for display
private fun formatTimeForDisplay(time: String): String {
    if (time.contains("AM") || time.contains("PM")) return time
    return try {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].take(2).toInt()
        val amPm = if (hour < 12) "AM" else "PM"
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        String.format(Locale.getDefault(), "%02d:%02d %s", h12, minute, amPm)
    } catch (e: Exception) {
        time
    }
}

// Helper to avoid issues with captured variables in Composable lambda if any
private fun isEditingInAvailability(isEditable: Boolean) = isEditable

@Composable
fun ProfileTimeSlotChip(text: String, onClick: (() -> Unit)? = null) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    HealthcareTheme {
        ProfileContent(
            user = UserDto(
                id = "ac1f3bbe-4a9b-45ae-9fc1-0c97b2e7a31b",
                name = "Rahul Mehta",
                email = "apple@gmail.com",
                role = "DOCTOR",
                doctorDetails = DoctorDetailsDto(
                    id = "965d77e0-5222-4651-bfaa-50c14954955f",
                    name = "Dr. Rahul Mehta",
                    specialization = "Cardiologist",
                    qualification = "MBBS, MD (Cardiology)",
                    experience = 18,
                    consultationFee = 700.0,
                    about = "Heart specialist with expertise in interventional cardiology.",
                    clinicAddress = "Andheri East, Mumbai",
                    availability = mapOf(
                        "MON" to listOf(TimeSlotDto("09:00 AM", "12:00 PM"), TimeSlotDto("05:00 PM", "08:00 PM")),
                        "WED" to listOf(TimeSlotDto("10:00 AM", "03:00 PM"))
                    )
                )
            ),
            themeMode = "FOLLOW_SYSTEM",
            updateState = UiState.Success(Unit),
            onThemeChange = {},
            onUpdateProfile = { _ -> },
            onLogoutClick = {}
        )
    }
}
