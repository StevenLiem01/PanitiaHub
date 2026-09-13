package com.example.panitiahub.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class CpItem(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpScreen(
    userRole: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    val context = LocalContext.current
    var cps by remember { mutableStateOf(listOf<CpItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    val isAdmin = userRole == "Administrator"
    val accentPurple = Color(0xFF9B51E0)

    var showAddCpSheet by remember { mutableStateOf(false) }
    val addCpSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var cpName by remember { mutableStateOf("") }
    var cpPhone by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cpToDelete by remember { mutableStateOf<CpItem?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Contact Person (PIC)" else "Contact Person (PIC)"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtNoCp = if (isEn) "No PIC contacts added yet." else "Belum ada kontak PIC yang ditambahkan."
    val txtAddCpTitle = if (isEn) "Add Contact Person" else "Tambah Contact Person"
    val txtCpName = if (isEn) "PIC Name (e.g. Andrew - Event Div)" else "Nama PIC (Cth: Andrew - Divisi Acara)"
    val txtCpPhone = if (isEn) "Phone Number (e.g. 08123456789)" else "Nomor Telepon (Cth: 08123456789)"
    val txtErrEmpty = if (isEn) "Name and phone number cannot be empty!" else "Nama dan nomor telepon tidak boleh kosong!"
    val txtErrSave = if (isEn) "Failed to save contact." else "Gagal menyimpan kontak."
    val txtErrCall = if (isEn) "Failed to load call." else "Gagal memuat panggilan."
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtSave = if (isEn) "Save Contact" else "Simpan Kontak"
    val txtDelTitle = if (isEn) "Delete Contact" else "Hapus Kontak"
    val txtDelConfirm = if (isEn) "Are you sure you want to delete" else "Apakah Anda yakin ingin menghapus"
    val txtDelEnd = if (isEn) "from the Contact Person list?" else "dari daftar Contact Person?"
    val txtYes = if (isEn) "Yes, Delete" else "Ya, Hapus"
    val txtCancel = if (isEn) "Cancel" else "Batal"

    LaunchedEffect(Unit) {
        db.collection("contact_persons")
            .whereEqualTo("eventName", eventName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedCps = snapshot.documents.map { doc ->
                        CpItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            phone = doc.getString("phone") ?: "",
                            eventName = doc.getString("eventName") ?: ""
                        )
                    }
                    cps = fetchedCps
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddCpSheet = true },
                    containerColor = accentPurple,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = txtAddCpTitle)
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

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accentPurple)
                    }
                }
                cps.isEmpty() -> {
                    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = accentPurple.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                txtNoCp,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(cps) { cp ->
                            GlowCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cp.phone}"))
                                            context.startActivity(dialIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, txtErrCall, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                glowColor = accentPurple.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconAvatar(icon = Icons.Filled.Contacts, tint = accentPurple, size = 42.dp)
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cp.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = cp.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cp.phone}"))
                                                    context.startActivity(dialIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, txtErrCall, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Call,
                                                contentDescription = "Panggil",
                                                tint = accentPurple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (isAdmin) {
                                            IconButton(
                                                onClick = { cpToDelete = cp },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    contentDescription = "Hapus CP",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
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
        }

        if (showAddCpSheet && isAdmin) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddCpSheet = false
                    errorMessage = null
                },
                sheetState = addCpSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(txtAddCpTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = cpName,
                        onValueChange = { cpName = it },
                        label = { Text(txtCpName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cpPhone,
                        onValueChange = { cpPhone = it },
                        label = { Text(txtCpPhone) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                            val trimmedName = cpName.trim()
                            val trimmedPhone = cpPhone.trim()

                            if (trimmedName.isEmpty() || trimmedPhone.isEmpty()) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val cpData = hashMapOf(
                                "name" to trimmedName,
                                "phone" to trimmedPhone,
                                "eventName" to eventName
                            )

                            db.collection("contact_persons").add(cpData)
                                .addOnSuccessListener {
                                    cpName = ""
                                    cpPhone = ""
                                    isSubmitting = false
                                    showAddCpSheet = false
                                }
                                .addOnFailureListener {
                                    errorMessage = txtErrSave
                                    isSubmitting = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentPurple, contentColor = Color.White)
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (cpToDelete != null) {
            AlertDialog(
                onDismissRequest = { cpToDelete = null },
                title = { Text(text = txtDelTitle, fontWeight = FontWeight.Bold) },
                text = { Text(text = "$txtDelConfirm '${cpToDelete?.name}' $txtDelEnd") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            cpToDelete?.let { cp ->
                                db.collection("contact_persons").document(cp.id).delete()
                                    .addOnSuccessListener { cpToDelete = null }
                            }
                        }
                    ) { Text(txtYes, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { cpToDelete = null }) { Text(txtCancel) }
                }
            )
        }
    }
}