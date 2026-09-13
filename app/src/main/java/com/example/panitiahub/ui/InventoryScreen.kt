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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.CyberGreen
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class InventoryItem(
    val id: String = "", val itemName: String = "", val borrower: String = "",
    val source: String = "", val status: String = "Dipinjam", val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    userRole: String, eventName: String,
    currentLanguage: String,
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    var inventoryList by remember { mutableStateOf(listOf<InventoryItem>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val isAdmin = userRole == "Administrator"
    val accentCyan = Color(0xFF00BCD4)
    var showInventorySheet by remember { mutableStateOf(false) }
    val inventorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedInventory by remember { mutableStateOf<InventoryItem?>(null) }
    var itemName by remember { mutableStateOf("") }
    var borrower by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var inventoryToDelete by remember { mutableStateOf<InventoryItem?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Equipment Borrowing" else "Peminjaman Alat"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtSearch = if (isEn) "Search items or borrowers..." else "Cari barang atau peminjam..."
    val txtNoData = if (isEn) "No equipment borrowing records yet." else "Belum ada catatan peminjaman alat."
    val txtNoSearch = if (isEn) "Search not found." else "Pencarian tidak ditemukan."
    val txtAddRecord = if (isEn) "Record Borrowing" else "Catat Peminjaman"
    val txtEditRecord = if (isEn) "Edit Details" else "Edit Detail Peminjaman"
    val txtItemName = if (isEn) "Item Name (e.g. Walkie Talkie)" else "Nama Barang (Cth: HT, Proyektor)"
    val txtPIC = if (isEn) "Person in Charge (Borrower)" else "Penanggung Jawab (Nama Peminjam)"
    val txtSource = if (isEn) "Item Source (e.g. Warehouse)" else "Asal Barang (Cth: Gudang Kampus)"
    val txtErrEmpty = if (isEn) "All fields must be filled!" else "Semua kolom data harus diisi!"
    val txtSave = if (isEn) "Save Record" else "Simpan Catatan"
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtDelTitle = if (isEn) "Delete Record" else "Hapus Catatan"
    val txtDelConfirm = if (isEn) "Are you sure you want to delete" else "Apakah Anda yakin ingin menghapus catatan"
    val txtYes = if (isEn) "Yes, Delete" else "Ya, Hapus"
    val txtCancel = if (isEn) "Cancel" else "Batal"

    LaunchedEffect(Unit) {
        db.collection("inventories").whereEqualTo("eventName", eventName).addSnapshotListener { snapshot, e ->
            if (e != null) { isLoading = false; return@addSnapshotListener }
            if (snapshot != null) {
                inventoryList = snapshot.documents.map { doc ->
                    InventoryItem(doc.id, doc.getString("itemName") ?: "", doc.getString("borrower") ?: "", doc.getString("source") ?: "", doc.getString("status") ?: "Dipinjam", doc.getString("eventName") ?: "")
                }.sortedBy { it.status }
                isLoading = false
            }
        }
    }

    val filteredInventory = if (searchQuery.isEmpty()) inventoryList else inventoryList.filter { it.itemName.contains(searchQuery, true) || it.borrower.contains(searchQuery, true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { selectedInventory = null; itemName = ""; borrower = ""; source = ""; showInventorySheet = true },
                containerColor = accentCyan, contentColor = Color.White, shape = CircleShape
            ) { Icon(Icons.Filled.Add, contentDescription = null) }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { Icon(Icons.Filled.ArrowBack, null) }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(txtHeader, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(txtEvent, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(txtSearch, color = TextSecondary) }, leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextSecondary) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Clear, null, tint = TextSecondary) } },
                shape = RoundedCornerShape(14.dp), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            when {
                isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = accentCyan) }
                inventoryList.isEmpty() -> GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = accentCyan.copy(alpha = 0.15f)) { Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text(txtNoData, color = TextSecondary) } }
                filteredInventory.isEmpty() -> Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) { Text(txtNoSearch, color = TextSecondary) }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(filteredInventory) { item ->
                            InventoryCard(
                                item = item, isAdmin = isAdmin, currentLanguage = currentLanguage,
                                onStatusChange = { newStatus -> db.collection("inventories").document(item.id).update("status", newStatus) },
                                onEditClick = { selectedInventory = item; itemName = item.itemName; borrower = item.borrower; source = item.source; showInventorySheet = true },
                                onDeleteClick = { inventoryToDelete = item }
                            )
                        }
                    }
                }
            }
        }

        if (showInventorySheet) {
            ModalBottomSheet(onDismissRequest = { showInventorySheet = false; errorMessage = null }, sheetState = inventorySheetState, containerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 24.dp)) {
                    Text(if (selectedInventory == null) txtAddRecord else txtEditRecord, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text(txtItemName) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = borrower, onValueChange = { borrower = it }, label = { Text(txtPIC) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text(txtSource) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    if (errorMessage != null) Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    Button(
                        onClick = {
                            val tName = itemName.trim(); val tBorrower = borrower.trim(); val tSource = source.trim()
                            if (tName.isEmpty() || tBorrower.isEmpty() || tSource.isEmpty()) { errorMessage = txtErrEmpty; return@Button }
                            errorMessage = null; isSubmitting = true
                            val itemData = hashMapOf("itemName" to tName, "borrower" to tBorrower, "source" to tSource, "eventName" to eventName)
                            if (selectedInventory == null) {
                                itemData["status"] = "Dipinjam"
                                db.collection("inventories").add(itemData).addOnCompleteListener { showInventorySheet = false; isSubmitting = false }
                            } else {
                                db.collection("inventories").document(selectedInventory!!.id).update(itemData as Map<String, Any>).addOnCompleteListener { showInventorySheet = false; isSubmitting = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isSubmitting, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = accentCyan, contentColor = Color.White)
                    ) { Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold) }
                }
            }
        }

        if (inventoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { inventoryToDelete = null }, title = { Text(txtDelTitle, fontWeight = FontWeight.Bold) }, text = { Text("$txtDelConfirm '${inventoryToDelete?.itemName}'?") },
                confirmButton = { TextButton(onClick = { db.collection("inventories").document(inventoryToDelete!!.id).delete().addOnSuccessListener { inventoryToDelete = null } }) { Text(txtYes, color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { inventoryToDelete = null }) { Text(txtCancel) } }
            )
        }
    }
}

@Composable
private fun InventoryCard(item: InventoryItem, isAdmin: Boolean, currentLanguage: String, onStatusChange: (String) -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val isReturned = item.status == "Dikembalikan"
    val statusColor = if (isReturned) CyberGreen else Color(0xFFF5A623)
    val statusIcon = if (isReturned) Icons.Filled.CheckCircle else Icons.Filled.Warning
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedOptions by remember { mutableStateOf(false) }

    val isEn = currentLanguage == "EN"
    val txtStatus = if (isEn) { if (isReturned) "RETURNED" else "BORROWED" } else item.status.uppercase()
    val txtPicSource = if (isEn) "PIC: ${item.borrower} • From: ${item.source}" else "PJ: ${item.borrower} • Asal: ${item.source}"

    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = if (isReturned) Color.Transparent else statusColor.copy(alpha = 0.15f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconAvatar(icon = Icons.Filled.Build, tint = if (isReturned) Color.Gray else Color(0xFF00BCD4), size = 42.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.itemName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isReturned) TextSecondary else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(txtPicSource, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Surface(modifier = Modifier.clickable { expandedStatus = true }, color = statusColor.copy(alpha = 0.15f), contentColor = statusColor, shape = RoundedCornerShape(50)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(statusIcon, null, modifier = Modifier.size(14.dp))
                            Text(txtStatus, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (item.status != "Dipinjam") DropdownMenuItem(text = { Text(if (isEn) "Mark as Borrowed" else "Tandai Dipinjam", color = Color(0xFFF5A623)) }, onClick = { onStatusChange("Dipinjam"); expandedStatus = false })
                        if (item.status != "Dikembalikan") DropdownMenuItem(text = { Text(if (isEn) "Mark as Returned" else "Tandai Dikembalikan", color = CyberGreen) }, onClick = { onStatusChange("Dikembalikan"); expandedStatus = false })
                    }
                }
                if (isAdmin) {
                    Box {
                        IconButton(onClick = { expandedOptions = true }) { Icon(Icons.Filled.MoreVert, null, tint = TextSecondary) }
                        DropdownMenu(expanded = expandedOptions, onDismissRequest = { expandedOptions = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                            DropdownMenuItem(text = { Text(if (isEn) "Edit Item" else "Edit Barang") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, onClick = { expandedOptions = false; onEditClick() })
                            DropdownMenuItem(text = { Text(if (isEn) "Delete" else "Hapus", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { expandedOptions = false; onDeleteClick() })
                        }
                    }
                }
            }
        }
    }
}