package com.example.panitiahub.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.CyberGreen
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class AttendanceSession(
    val id: String = "",
    val title: String = "",
    val eventName: String = "",
    val isActive: Boolean = true,
    val attendees: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    userRole: String,
    username: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    val context = LocalContext.current
    var sessions by remember { mutableStateOf(listOf<AttendanceSession>()) }
    var isLoading by remember { mutableStateOf(true) }

    val isAdmin = userRole == "Administrator"
    val accentRose = Color(0xFFE91E63)

    var showAddSessionSheet by remember { mutableStateOf(false) }
    val addSessionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sessionTitle by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Meeting Attendance" else "Absensi Rapat"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtNoSession = if (isEn) "No meeting sessions opened yet." else "Belum ada sesi rapat yang dibuka."
    val txtOpenSession = if (isEn) "Open Meeting Session" else "Buka Sesi Rapat"
    val txtAgendaTitle = if (isEn) "Meeting Agenda (e.g. Plenary 1)" else "Agenda Rapat (Cth: Rapat Pleno 1)"
    val txtErrEmpty = if (isEn) "Meeting title cannot be empty!" else "Judul rapat tidak boleh kosong!"
    val txtErrSave = if (isEn) "Failed to create session." else "Gagal membuat sesi."
    val txtSaving = if (isEn) "Opening Session..." else "Membuka Sesi..."
    val txtSave = if (isEn) "Start Attendance" else "Mulai Absensi"

    val txtOngoing = if (isEn) "Ongoing" else "Sedang Berlangsung"
    val txtClosed = if (isEn) "Session Closed" else "Sesi Ditutup"
    val txtCloseBtn = if (isEn) "Close" else "Tutup"
    val txtAttendedBtn = if (isEn) "Attend" else "Hadir"
    val txtAlreadyAttended = if (isEn) "✓ You Have Attended" else "✓ Anda Sudah Hadir"

    val txtExportSuccess = if (isEn) "Attendance Data exported successfully!" else "Data Absensi berhasil diekspor!"
    val txtExportFail = if (isEn) "Failed to export data" else "Gagal mengekspor data"
    val txtCsvHeader = if (isEn) "Meeting Agenda,Status,Total Attended,Attendee Names\n" else "Agenda Rapat,Status,Jumlah Hadir,Daftar Nama Hadir\n"

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = outputStream.bufferedWriter()
                    writer.write(txtCsvHeader)
                    sessions.forEach { session ->
                        val status = if (session.isActive) (if(isEn) "Ongoing" else "Berlangsung") else (if(isEn) "Closed" else "Selesai")
                        val names = session.attendees.joinToString(";")
                        writer.write("\"${session.title}\",${status},${session.attendees.size},\"${names}\"\n")
                    }
                    writer.flush()
                }
                Toast.makeText(context, txtExportSuccess, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, txtExportFail, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        db.collection("attendances")
            .whereEqualTo("eventName", eventName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedSessions = snapshot.documents.map { doc ->
                        AttendanceSession(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            eventName = doc.getString("eventName") ?: "",
                            isActive = doc.getBoolean("isActive") ?: true,
                            attendees = doc.get("attendees") as? List<String> ?: emptyList()
                        )
                    }
                    sessions = fetchedSessions.sortedByDescending { it.isActive }
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddSessionSheet = true },
                    containerColor = accentRose,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(txtHeader, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(txtEvent, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                if (sessions.isNotEmpty() && isAdmin) {
                    IconButton(
                        onClick = {
                            val fileName = "Absensi_${eventName.replace(" ", "_")}.csv"
                            exportLauncher.launch(fileName)
                        },
                        modifier = Modifier.background(accentRose.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Export Absensi", tint = accentRose)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accentRose)
                    }
                }
                sessions.isEmpty() -> {
                    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = accentRose.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                txtNoSession,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(sessions) { session ->
                            val isUserAttended = session.attendees.contains(username)

                            val txtAttendedCount = if (isEn) "${session.attendees.size} Attended" else "${session.attendees.size} Hadir"

                            GlowCard(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = if (session.isActive) accentRose.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconAvatar(
                                                icon = Icons.Filled.FactCheck,
                                                tint = if (session.isActive) accentRose else Color.Gray,
                                                size = 36.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = session.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (session.isActive) txtOngoing else txtClosed,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (session.isActive) CyberGreen else TextSecondary
                                                )
                                            }
                                        }

                                        if (isAdmin && session.isActive) {
                                            TextButton(
                                                onClick = {
                                                    db.collection("attendances").document(session.id).update("isActive", false)
                                                }
                                            ) {
                                                Text(txtCloseBtn, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.PeopleAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(txtAttendedCount, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                        }

                                        if (session.isActive) {
                                            if (isUserAttended) {
                                                Text(txtAlreadyAttended, color = CyberGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            } else {
                                                Button(
                                                    onClick = {
                                                        db.collection("attendances").document(session.id)
                                                            .update("attendees", FieldValue.arrayUnion(username))
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = accentRose),
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text(txtAttendedBtn, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                                                }
                                            }
                                        }
                                    }

                                    if (session.attendees.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = session.attendees.joinToString(", "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddSessionSheet && isAdmin) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddSessionSheet = false
                    errorMessage = null
                },
                sheetState = addSessionSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(txtOpenSession, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text(txtAgendaTitle) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = {
                            val trimmedTitle = sessionTitle.trim()
                            if (trimmedTitle.isEmpty()) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val sessionData = hashMapOf(
                                "title" to trimmedTitle,
                                "eventName" to eventName,
                                "isActive" to true,
                                "attendees" to emptyList<String>()
                            )

                            db.collection("attendances").add(sessionData)
                                .addOnSuccessListener {
                                    sessionTitle = ""
                                    isSubmitting = false
                                    showAddSessionSheet = false
                                }
                                .addOnFailureListener {
                                    errorMessage = txtErrSave
                                    isSubmitting = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentRose, contentColor = Color.White)
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}