package com.example.panitiahub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.panitiahub.ui.theme.NeonPink
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class VendorItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val status: String = "Pending",
    val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorScreen(
    userRole: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    var vendors by remember { mutableStateOf(listOf<VendorItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddVendorSheet by remember { mutableStateOf(false) }
    val addVendorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var vendorName by remember { mutableStateOf("") }
    var vendorCategory by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Vendor Status" else "Status Vendor"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtNoVendor = if (isEn) "No vendors submitted yet." else "Belum ada vendor yang diajukan."
    val txtAddVendorTitle = if (isEn) "Submit New Vendor" else "Ajukan Vendor Baru"
    val txtVendorName = if (isEn) "Vendor Name (e.g. Bu Tejo Catering)" else "Nama Vendor (Cth: Catering Bu Tejo)"
    val txtCategory = if (isEn) "Category (e.g. Consumption)" else "Kategori (Cth: Konsumsi)"
    val txtErrEmpty = if (isEn) "Vendor name and category cannot be empty!" else "Nama vendor dan kategori tidak boleh kosong!"
    val txtErrSave = if (isEn) "Failed to save vendor." else "Gagal menyimpan vendor."
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtSave = if (isEn) "Save Vendor" else "Simpan Vendor"

    LaunchedEffect(Unit) {
        db.collection("vendors")
            .whereEqualTo("eventName", eventName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedVendors = snapshot.documents.map { doc ->
                        VendorItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            status = doc.getString("status") ?: "Pending",
                            eventName = doc.getString("eventName") ?: ""
                        )
                    }
                    vendors = fetchedVendors
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddVendorSheet = true },
                containerColor = NeonPink,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Vendor")
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
                        CircularProgressIndicator(color = NeonPink)
                    }
                }
                vendors.isEmpty() -> {
                    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = NeonPink.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = txtNoVendor,
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
                        items(vendors) { vendor ->
                            VendorCard(
                                vendor = vendor,
                                currentLanguage = currentLanguage, // Oper ke Card
                                onStatusChange = { newStatus ->
                                    db.collection("vendors").document(vendor.id).update("status", newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddVendorSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddVendorSheet = false
                    errorMessage = null
                },
                sheetState = addVendorSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(txtAddVendorTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vendorName,
                        onValueChange = { vendorName = it },
                        label = { Text(txtVendorName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vendorCategory,
                        onValueChange = { vendorCategory = it },
                        label = { Text(txtCategory) },
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
                            val trimmedName = vendorName.trim()
                            val trimmedCat = vendorCategory.trim()

                            if (trimmedName.isEmpty() || trimmedCat.isEmpty()) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val vendorData = hashMapOf(
                                "name" to trimmedName,
                                "category" to trimmedCat,
                                "eventName" to eventName,
                                "status" to "Pending"
                            )

                            db.collection("vendors").add(vendorData)
                                .addOnSuccessListener {
                                    vendorName = ""
                                    vendorCategory = ""
                                    isSubmitting = false
                                    showAddVendorSheet = false
                                }
                                .addOnFailureListener {
                                    errorMessage = txtErrSave
                                    isSubmitting = false
                                }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = Color.White)
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCard(
    vendor: VendorItem,
    currentLanguage: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val (statusColor, statusIcon) = when (vendor.status.uppercase()) {
        "APPROVED" -> CyberGreen to Icons.Filled.CheckCircle
        "REJECTED" -> MaterialTheme.colorScheme.error to Icons.Filled.Cancel
        else -> Color(0xFFF5A623) to Icons.Filled.Schedule
    }

    val isEn = currentLanguage == "EN"
    val txtMarkPending = if (isEn) "Mark Pending" else "Tandai Pending"
    val txtMarkApproved = if (isEn) "Mark Approved" else "Tandai Approved"
    val txtMarkRejected = if (isEn) "Mark Rejected" else "Tandai Rejected"

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = ElectricBlue.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconAvatar(icon = Icons.Filled.Storefront, tint = ElectricBlue, size = 42.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${vendor.eventName} • ${vendor.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box {
                Surface(
                    modifier = Modifier.clickable { expanded = true },
                    color = statusColor.copy(alpha = 0.15f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(statusIcon, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            text = vendor.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (vendor.status != "Pending") {
                        DropdownMenuItem(
                            text = { Text(txtMarkPending) },
                            onClick = { onStatusChange("Pending"); expanded = false }
                        )
                    }
                    if (vendor.status != "Approved") {
                        DropdownMenuItem(
                            text = { Text(txtMarkApproved, color = CyberGreen) },
                            onClick = { onStatusChange("Approved"); expanded = false }
                        )
                    }
                    if (vendor.status != "Rejected") {
                        DropdownMenuItem(
                            text = { Text(txtMarkRejected, color = MaterialTheme.colorScheme.error) },
                            onClick = { onStatusChange("Rejected"); expanded = false }
                        )
                    }
                }
            }
        }
    }
}