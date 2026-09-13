package com.example.panitiahub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.components.RoleBadge
import com.example.panitiahub.ui.components.SectionLabel
import com.example.panitiahub.ui.theme.CyberGreen
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class EventItem(val id: String, val name: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    userRole: String,
    currentLanguage: String,
    onEventClick: (EventItem) -> Unit,
    onProfileClick: () -> Unit
) {
    val db = Firebase.firestore

    var eventList by remember { mutableStateOf(listOf<EventItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    var newEventName by remember { mutableStateOf("") }
    var selectedDateText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var eventToDisable by remember { mutableStateOf<EventItem?>(null) }

    var showAddEventSheet by remember { mutableStateOf(false) }
    val addEventSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAdmin = userRole == "Administrator"

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtWelcome = if (isEn) "Welcome," else "Selamat datang,"
    val txtActiveAgenda = if (isEn) "Active Agendas" else "Agenda Aktif"
    val txtNoAgenda = if (isEn) "No active agendas yet." else "Belum ada agenda aktif."
    val txtAddAgenda = if (isEn) "Add New Agenda" else "Tambah Agenda Baru"
    val txtNewAgendaName = if (isEn) "New Agenda Name" else "Nama Agenda Baru"
    val txtEventDate = if (isEn) "Event D-Day Date" else "Tanggal Hari H Acara"
    val txtSave = if (isEn) "Save Agenda" else "Simpan Agenda"
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtCloseAgenda = if (isEn) "Close Agenda" else "Tutup Agenda"
    val txtCloseConfirm = if (isEn) "Are you sure you want to complete/deactivate agenda" else "Apakah Anda yakin ingin menyelesaikan/menonaktifkan agenda"
    val txtYes = if (isEn) "Yes, Deactivate" else "Ya, Nonaktifkan"
    val txtCancel = if (isEn) "Cancel" else "Batal"
    val txtSelect = if (isEn) "Select" else "Pilih"
    val txtActiveBadge = if (isEn) "ACTIVE" else "AKTIF"
    val txtEmptyNameErr = if (isEn) "Agenda name cannot be empty!" else "Nama agenda tidak boleh kosong!"
    val txtShortNameErr = if (isEn) "Use a clearer agenda name (min. 4 characters)." else "Gunakan nama agenda yang lebih jelas (minimal 4 karakter)."
    val txtEmptyDateErr = if (isEn) "Please set the event D-Day date!" else "Silakan tentukan tanggal hari H acara!"
    val txtServerErr = if (isEn) "Failed to connect to server." else "Gagal menghubungi server."
    val txtNotSetDate = if (isEn) "Date not set" else "Tanggal belum diatur"

    LaunchedEffect(Unit) {
        db.collection("events")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedEvents = mutableListOf<EventItem>()
                    for (document in snapshot.documents) {
                        val name = document.getString("eventName") ?: ""
                        val date = document.getString("eventDate") ?: txtNotSetDate
                        if (name.isNotBlank()) {
                            fetchedEvents.add(EventItem(id = document.id, name = name, date = date))
                        }
                    }
                    eventList = fetchedEvents
                    isLoading = false
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column {
                    // ---------- Header ----------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = txtWelcome,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                            Text(
                                text = username,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            RoleBadge(role = userRole)
                        }

                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Profil",
                                tint = ElectricBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel(text = txtActiveAgenda)

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = ElectricBlue)
                            }
                        }
                        eventList.isEmpty() -> {
                            GlowCard(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = ElectricBlue.copy(alpha = 0.15f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = txtNoAgenda,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                items(eventList) { event ->
                                    AgendaCard(
                                        event = event,
                                        isAdmin = isAdmin,
                                        activeText = txtActiveBadge,
                                        onClick = { onEventClick(event) },
                                        onDisableClick = { eventToDisable = event }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isAdmin) {
                item {
                    Button(
                        onClick = { showAddEventSheet = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF06110D))
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(txtAddAgenda, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showAddEventSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddEventSheet = false
                    errorMessage = null
                },
                sheetState = addEventSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(txtAddAgenda, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newEventName,
                        onValueChange = { newEventName = it },
                        label = { Text(txtNewAgendaName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = selectedDateText,
                        onValueChange = {},
                        label = { Text(txtEventDate) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = {
                            val trimmedEvent = newEventName.trim()

                            if (trimmedEvent.isEmpty()) {
                                errorMessage = txtEmptyNameErr
                                return@Button
                            }
                            if (trimmedEvent.length < 4) {
                                errorMessage = txtShortNameErr
                                return@Button
                            }
                            if (selectedDateText.isEmpty()) {
                                errorMessage = txtEmptyDateErr
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val newEventData = hashMapOf(
                                "eventName" to trimmedEvent,
                                "eventDate" to selectedDateText,
                                "isActive" to true
                            )

                            db.collection("events").add(newEventData).addOnSuccessListener {
                                newEventName = ""
                                selectedDateText = ""
                                isSubmitting = false
                                showAddEventSheet = false
                            }.addOnFailureListener {
                                errorMessage = txtServerErr
                                isSubmitting = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color(0xFF06110D))
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selection = datePickerState.selectedDateMillis
                        if (selection != null) {
                            selectedDateText = formatter.format(java.util.Date(selection))
                        }
                        showDatePicker = false
                    }) {
                        Text(txtSelect)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(txtCancel)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (eventToDisable != null) {
            AlertDialog(
                onDismissRequest = { eventToDisable = null },
                title = { Text(text = txtCloseAgenda, fontWeight = FontWeight.Bold) },
                text = { Text(text = "$txtCloseConfirm '${eventToDisable?.name}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            eventToDisable?.let { event ->
                                db.collection("events").document(event.id)
                                    .update("isActive", false)
                                    .addOnSuccessListener { eventToDisable = null }
                            }
                        }
                    ) { Text(txtYes, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { eventToDisable = null }) { Text(txtCancel) }
                }
            )
        }
    }
}

@Composable
private fun AgendaCard(
    event: EventItem,
    isAdmin: Boolean,
    activeText: String,
    onClick: () -> Unit,
    onDisableClick: () -> Unit
) {
    GlowCard(
        modifier = Modifier
            .width(230.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        glowColor = ElectricBlue.copy(alpha = 0.25f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconAvatar(icon = Icons.Filled.Event, tint = ElectricBlue, size = 32.dp)
                    Surface(
                        color = CyberGreen.copy(alpha = 0.15f),
                        contentColor = CyberGreen,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = activeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = onDisableClick,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Nonaktifkan Agenda", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Column {
                Text(text = event.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = event.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
            }
        }
    }
}