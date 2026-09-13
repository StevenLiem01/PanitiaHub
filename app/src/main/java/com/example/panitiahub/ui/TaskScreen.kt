package com.example.panitiahub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.CyberGreen
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Task(
    val id: String = "",
    val taskName: String = "",
    val division: String = "",
    val status: String = "To Do",
    val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    userRole: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var isLoading by remember { mutableStateOf(true) }

    val isAdmin = userRole == "Administrator"
    val tabs = listOf("To Do", "In Progress", "Done")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // State Bottom Sheet (Digunakan untuk Tambah & Edit)
    var showTaskSheet by remember { mutableStateOf(false) }
    val taskSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTask by remember { mutableStateOf<Task?>(null) } // null = Tambah, isi = Edit
    var newTaskName by remember { mutableStateOf("") }
    var newDivision by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State Dialog Hapus
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Task Management" else "Manajemen Tugas"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtNoTask = if (isEn) "No tasks in this stage." else "Tidak ada tugas di tahap ini."
    val txtAddDelegation = if (isEn) "Delegate New Task" else "Delegasikan Tugas Baru"
    val txtEditDelegation = if (isEn) "Edit Task Details" else "Edit Detail Tugas"
    val txtTaskName = if (isEn) "Task Name (e.g. Print ID Card)" else "Nama Tugas (Cth: Cetak ID Card)"
    val txtDivision = if (isEn) "Division (e.g. Public Relations)" else "Divisi (Cth: Humas)"
    val txtErrEmpty = if (isEn) "Task name and Division cannot be empty!" else "Nama tugas dan Divisi tidak boleh kosong!"
    val txtSave = if (isEn) "Save" else "Simpan"
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtErrSave = if (isEn) "Failed to save task." else "Gagal menyimpan tugas."
    val txtErrUpdate = if (isEn) "Failed to update task." else "Gagal memperbarui tugas."
    val txtDelTitle = if (isEn) "Delete Task" else "Hapus Tugas"
    val txtDelConfirm = if (isEn) "Are you sure you want to delete task" else "Apakah Anda yakin ingin menghapus tugas"
    val txtDelEnd = if (isEn) "from the board?" else "dari papan kerja?"
    val txtYes = if (isEn) "Yes, Delete" else "Ya, Hapus"
    val txtCancel = if (isEn) "Cancel" else "Batal"

    LaunchedEffect(Unit) {
        db.collection("tasks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedTasks = snapshot.documents.map { doc ->
                        Task(
                            id = doc.id,
                            taskName = doc.getString("taskName") ?: "",
                            division = doc.getString("division") ?: "",
                            status = doc.getString("status") ?: "To Do",
                            eventName = doc.getString("eventName") ?: ""
                        )
                    }
                    tasks = fetchedTasks
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        selectedTask = null
                        newTaskName = ""
                        newDivision = ""
                        showTaskSheet = true
                    },
                    containerColor = Color(0xFFF5A623),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Tugas")
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
            // ---------- Header ----------
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

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Tab bar Status ----------
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = ElectricBlue,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ElectricBlue
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title, // "To Do", "In Progress", "Done" adalah universal, tidak diterjemahkan
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) ElectricBlue else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredTasks = tasks.filter { it.status == tabs[selectedTabIndex] && it.eventName == eventName }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBlue)
                    }
                }
                filteredTasks.isEmpty() -> {
                    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = ElectricBlue.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                txtNoTask,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTasks) { task ->
                            TaskCard(
                                task = task,
                                isAdmin = isAdmin,
                                currentLanguage = currentLanguage, // Oper parameter ke card
                                onStatusChange = { newStatus ->
                                    db.collection("tasks").document(task.id).update("status", newStatus)
                                },
                                onEditClick = {
                                    selectedTask = task
                                    newTaskName = task.taskName
                                    newDivision = task.division
                                    showTaskSheet = true
                                },
                                onDeleteClick = { taskToDelete = task }
                            )
                        }
                    }
                }
            }
        }

        // ---------- Bottom Sheet: Form Tugas ----------
        if (showTaskSheet && isAdmin) {
            ModalBottomSheet(
                onDismissRequest = {
                    showTaskSheet = false
                    errorMessage = null
                },
                sheetState = taskSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = if (selectedTask == null) txtAddDelegation else txtEditDelegation,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text(txtTaskName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newDivision,
                        onValueChange = { newDivision = it },
                        label = { Text(txtDivision) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val trimmedName = newTaskName.trim()
                            val trimmedDiv = newDivision.trim()

                            if (trimmedName.isEmpty() || trimmedDiv.isEmpty()) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val taskData = hashMapOf(
                                "taskName" to trimmedName,
                                "division" to trimmedDiv,
                                "eventName" to eventName
                            )

                            if (selectedTask == null) {
                                taskData["status"] = "To Do"
                                db.collection("tasks").add(taskData)
                                    .addOnSuccessListener { showTaskSheet = false; isSubmitting = false }
                                    .addOnFailureListener { errorMessage = txtErrSave; isSubmitting = false }
                            } else {
                                db.collection("tasks").document(selectedTask!!.id).update(taskData as Map<String, Any>)
                                    .addOnSuccessListener { showTaskSheet = false; isSubmitting = false }
                                    .addOnFailureListener { errorMessage = txtErrUpdate; isSubmitting = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623), contentColor = Color.White)
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ---------- Dialog Konfirmasi Hapus Tugas ----------
        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text(text = txtDelTitle, fontWeight = FontWeight.Bold) },
                text = { Text(text = "$txtDelConfirm '${taskToDelete?.taskName}' $txtDelEnd") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            taskToDelete?.let { task ->
                                db.collection("tasks").document(task.id).delete()
                                    .addOnSuccessListener { taskToDelete = null }
                            }
                        }
                    ) { Text(txtYes, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) { Text(txtCancel) }
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    isAdmin: Boolean,
    currentLanguage: String,
    onStatusChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val accentColor = when (task.status) {
        "Done" -> CyberGreen
        "In Progress" -> Color(0xFFF5A623)
        else -> ElectricBlue
    }

    val isEn = currentLanguage == "EN"
    val txtMoveToDo = if (isEn) "Move to To Do" else "Pindahkan ke To Do"
    val txtMarkInProg = if (isEn) "Mark In Progress" else "Tandai In Progress"
    val txtMarkDone = if (isEn) "Mark Done" else "Tandai Done"
    val txtEditTask = if (isEn) "Edit Task" else "Edit Tugas"
    val txtDelete = if (isEn) "Delete" else "Hapus"

    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = accentColor.copy(alpha = 0.1f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconAvatar(icon = Icons.Filled.Assignment, tint = accentColor, size = 42.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.taskName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.division, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opsi", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (task.status != "To Do") {
                        DropdownMenuItem(text = { Text(txtMoveToDo) }, onClick = { onStatusChange("To Do"); expanded = false })
                    }
                    if (task.status != "In Progress") {
                        DropdownMenuItem(text = { Text(txtMarkInProg) }, onClick = { onStatusChange("In Progress"); expanded = false })
                    }
                    if (task.status != "Done") {
                        DropdownMenuItem(text = { Text(txtMarkDone) }, onClick = { onStatusChange("Done"); expanded = false })
                    }

                    if (isAdmin) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text(txtEditTask) },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = { expanded = false; onEditClick() }
                        )
                        DropdownMenuItem(
                            text = { Text(txtDelete, color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            onClick = { expanded = false; onDeleteClick() }
                        )
                    }
                }
            }
        }
    }
}