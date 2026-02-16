package com.doctor.app.appointments.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doctor.app.appointments.api.AppointmentRepository
import com.doctor.app.appointments.api.PrescriptionDto
import com.doctor.app.appointments.viewmodel.PrescriptionViewModel
import com.doctor.app.appointments.viewmodel.PrescriptionViewModelFactory
import com.doctor.app.core.storage.TokenManager
import com.doctor.app.core.ui.UiState
import com.doctor.app.core.ui.components.ListScreenHeader
import com.doctor.app.core.ui.components.LoadingState
import com.doctor.app.core.ui.theme.HealthcareTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrescriptionListScreen(
    tokenManager: TokenManager
) {
    val repository = remember { AppointmentRepository(tokenManager) }
    val viewModel: PrescriptionViewModel = viewModel(
        factory = PrescriptionViewModelFactory(repository)
    )
    val prescriptionsState by viewModel.prescriptionsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadPrescriptions()
    }

    when (val state = prescriptionsState) {
        is UiState.Loading -> LoadingState()
        is UiState.Error -> PrescriptionErrorState(
            message = state.message,
            onRetry = viewModel::loadPrescriptions
        )
        is UiState.Success -> PrescriptionListContent(state.data)
    }
}

@Composable
private fun PrescriptionListContent(
    prescriptions: List<PrescriptionDto>
) {
    if (prescriptions.isEmpty()) {
        EmptyPrescriptionsState()
        return
    }

    val groupedPrescriptions = remember(prescriptions) {
        prescriptions.groupBy { it.prescriptionDate }.toSortedMap(compareByDescending { it })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ListScreenHeader(
                label = "Prescriptions",
                countText = "${prescriptions.size} Total",
                title = "Prescription History",
                description = "Access and review all prescriptions issued to your patients.",
                icon = Icons.AutoMirrored.Outlined.ReceiptLong
            )
            Spacer(Modifier.height(16.dp))
        }

        groupedPrescriptions.forEach { (date, prescriptionsForDate) ->
            item {
                PrescriptionDateHeader(date)
                Spacer(Modifier.height(8.dp))
            }
            items(prescriptionsForDate) { prescription ->
                PrescriptionItemCard(prescription)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyPrescriptionsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "No Prescriptions Found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "There are no prescriptions recorded yet. Once you create prescriptions for patients, they will be listed here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f),
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PrescriptionErrorState(
    message: String,
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
            text = "Failed to load prescriptions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = message,
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

@Composable
private fun PrescriptionDateHeader(date: String) {
    val displayDate = remember(date) { getDisplayDate(date) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = displayDate,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (displayDate == "Today" || displayDate == "Yesterday") {
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
        val yesterday = today.minusDays(1)

        when (date) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun PrescriptionItemCard(
    prescription: PrescriptionDto
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (!isSystemInDarkTheme()) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prescription.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Visit: ${prescription.appointmentDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { /* View details */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "View details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Prescribed Medications",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = prescription.medications,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )

                    if (prescription.instructions.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "INSTRUCTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = prescription.instructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (prescription.notes.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Doctor's Notes",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = prescription.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrescriptionListScreenPreview() {
    val mockPrescriptions = listOf(
        PrescriptionDto(
            id = "1",
            medications = "Paracetamol 500mg (2x/day), Cetirizine 10mg (Nightly)",
            instructions = "Take Paracetamol after meals. Cetirizine should be taken before sleep for 5 days.",
            notes = "Patient has high fever and runny nose. Advised warm fluids.",
            prescriptionDate = LocalDate.now().toString(),
            appointmentId = "a1",
            appointmentDate = "2024-08-15",
            doctorName = "Dr. Rahul Mehta",
            patientName = "Alice Brown"
        ),
        PrescriptionDto(
            id = "2",
            medications = "Aspirin 75mg, Once daily",
            instructions = "Take in the morning after breakfast.",
            notes = "Regular cardiovascular checkup.",
            prescriptionDate = LocalDate.now().minusDays(1).toString(),
            appointmentId = "a2",
            appointmentDate = "2024-08-14",
            doctorName = "Dr. Rahul Mehta",
            patientName = "John Smith"
        )
    )
    HealthcareTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            PrescriptionListContent(mockPrescriptions)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyPrescriptionListPreview() {
    HealthcareTheme {
        PrescriptionListContent(emptyList())
    }
}

@Preview(showBackground = true)
@Composable
fun PrescriptionErrorPreview() {
    HealthcareTheme {
        PrescriptionErrorState(
            message = "Unable to connect to the server. Please try again.",
            onRetry = {}
        )
    }
}
