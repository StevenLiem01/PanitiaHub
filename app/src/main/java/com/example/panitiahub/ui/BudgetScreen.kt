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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.CyberGreen
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.NeonPink
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat
import java.util.Locale

data class BudgetItem(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "Pengeluaran",
    val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    userRole: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    val context = LocalContext.current
    var budgets by remember { mutableStateOf(listOf<BudgetItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    val isAdmin = userRole == "Administrator"

    // Format Rupiah
    val localeID = Locale("in", "ID")
    val formatRupiah = NumberFormat.getCurrencyInstance(localeID)

    val totalPemasukan = budgets.filter { it.type == "Pemasukan" }.sumOf { it.amount }
    val totalPengeluaran = budgets.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
    val saldoAkhir = totalPemasukan - totalPengeluaran

    var showBudgetSheet by remember { mutableStateOf(false) }
    val budgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedBudget by remember { mutableStateOf<BudgetItem?>(null) }
    var budgetTitle by remember { mutableStateOf("") }
    var budgetAmount by remember { mutableStateOf("") }
    var budgetType by remember { mutableStateOf("Pengeluaran") } // Nilai DB asli tidak boleh diterjemahkan
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var budgetToDelete by remember { mutableStateOf<BudgetItem?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Manage Budget" else "Kelola Anggaran"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtBalance = if (isEn) "Current Balance" else "Saldo Saat Ini"
    val txtIncome = if (isEn) "Income" else "Pemasukan"
    val txtExpense = if (isEn) "Expense" else "Pengeluaran"
    val txtHistory = if (isEn) "Transaction History" else "Riwayat Transaksi"
    val txtNoHistory = if (isEn) "No transaction history yet." else "Belum ada riwayat transaksi."
    val txtAddRecord = if (isEn) "Record New Transaction" else "Catat Transaksi Baru"
    val txtEditRecord = if (isEn) "Edit Transaction" else "Edit Transaksi"
    val txtDesc = if (isEn) "Description" else "Keterangan"
    val txtAmount = if (isEn) "Amount without periods (e.g. 50000)" else "Nominal Tanpa Titik (Cth: 50000)"
    val txtErrEmpty = if (isEn) "Valid description and amount must be filled!" else "Keterangan dan nominal valid harus diisi!"
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtSave = if (isEn) "Save" else "Simpan"
    val txtErrSave = if (isEn) "Failed to save transaction." else "Gagal menyimpan transaksi."
    val txtErrUpdate = if (isEn) "Failed to update transaction." else "Gagal memperbarui transaksi."
    val txtDelTitle = if (isEn) "Delete Transaction" else "Hapus Transaksi"
    val txtDelConfirm = if (isEn) "Are you sure you want to delete" else "Apakah Anda yakin ingin menghapus"
    val txtYes = if (isEn) "Yes, Delete" else "Ya, Hapus"
    val txtCancel = if (isEn) "Cancel" else "Batal"
    val txtExportSuccess = if (isEn) "Report saved successfully!" else "Laporan berhasil disimpan!"
    val txtExportFail = if (isEn) "Failed to export data" else "Gagal mengekspor data"
    val txtCsvHeader = if (isEn) "Description,Type,Amount\n" else "Keterangan,Tipe,Nominal\n"

    // Logika Export ke CSV
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = outputStream.bufferedWriter()
                    writer.write(txtCsvHeader)
                    budgets.forEach { budget ->
                        val exportedType = if (isEn) { if (budget.type == "Pemasukan") "Income" else "Expense" } else budget.type
                        writer.write("\"${budget.title}\",${exportedType},${budget.amount}\n")
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
        db.collection("budgets")
            .whereEqualTo("eventName", eventName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedBudgets = snapshot.documents.map { doc ->
                        BudgetItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            amount = doc.getDouble("amount") ?: 0.0,
                            type = doc.getString("type") ?: "Pengeluaran",
                            eventName = doc.getString("eventName") ?: ""
                        )
                    }
                    budgets = fetchedBudgets
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
                        selectedBudget = null
                        budgetTitle = ""
                        budgetAmount = ""
                        budgetType = "Pengeluaran" // Default DB value
                        showBudgetSheet = true
                    },
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Catat Transaksi")
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

                if (budgets.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val fileName = "Anggaran_${eventName.replace(" ", "_")}.csv"
                            exportLauncher.launch(fileName)
                        },
                        modifier = Modifier.background(CyberGreen.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Export Laporan", tint = CyberGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Kartu Saldo ----------
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = ElectricBlue.copy(alpha = 0.3f)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(txtBalance, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatRupiah.format(saldoAkhir),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(txtIncome, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(formatRupiah.format(totalPemasukan), fontWeight = FontWeight.Bold, color = CyberGreen)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(txtExpense, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(formatRupiah.format(totalPengeluaran), fontWeight = FontWeight.Bold, color = NeonPink)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(txtHistory, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // ---------- Daftar Transaksi ----------
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ElectricBlue)
                    }
                }
                budgets.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(txtNoHistory, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(budgets) { budget ->
                            BudgetCard(
                                budget = budget,
                                isAdmin = isAdmin,
                                formatRupiah = formatRupiah,
                                currentLanguage = currentLanguage, // Oper ke card
                                onEditClick = {
                                    selectedBudget = budget
                                    budgetTitle = budget.title
                                    budgetAmount = budget.amount.toLong().toString()
                                    budgetType = budget.type // Jangan diterjemahkan value-nya!
                                    showBudgetSheet = true
                                },
                                onDeleteClick = { budgetToDelete = budget }
                            )
                        }
                    }
                }
            }
        }

        // ---------- Bottom Sheet ----------
        if (showBudgetSheet && isAdmin) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBudgetSheet = false
                    errorMessage = null
                },
                sheetState = budgetSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = if (selectedBudget == null) txtAddRecord else txtEditRecord,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = budgetType == "Pemasukan",
                            onClick = { budgetType = "Pemasukan" }, // DB Value
                            label = { Text(txtIncome) }, // Display Label
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = budgetType == "Pengeluaran",
                            onClick = { budgetType = "Pengeluaran" }, // DB Value
                            label = { Text(txtExpense) }, // Display Label
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = budgetTitle,
                        onValueChange = { budgetTitle = it },
                        label = { Text(txtDesc) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = budgetAmount,
                        onValueChange = { budgetAmount = it },
                        label = { Text(txtAmount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val trimmedTitle = budgetTitle.trim()
                            val amountValue = budgetAmount.toDoubleOrNull()

                            if (trimmedTitle.isEmpty() || amountValue == null || amountValue <= 0) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val budgetData = hashMapOf(
                                "title" to trimmedTitle,
                                "amount" to amountValue,
                                "type" to budgetType,
                                "eventName" to eventName
                            )

                            if (selectedBudget == null) {
                                db.collection("budgets").add(budgetData)
                                    .addOnSuccessListener { showBudgetSheet = false; isSubmitting = false }
                                    .addOnFailureListener { errorMessage = txtErrSave; isSubmitting = false }
                            } else {
                                db.collection("budgets").document(selectedBudget!!.id).update(budgetData as Map<String, Any>)
                                    .addOnSuccessListener { showBudgetSheet = false; isSubmitting = false }
                                    .addOnFailureListener { errorMessage = txtErrUpdate; isSubmitting = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White)
                    ) {
                        Text(if (isSubmitting) txtSaving else txtSave, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ---------- Dialog Hapus ----------
        if (budgetToDelete != null) {
            AlertDialog(
                onDismissRequest = { budgetToDelete = null },
                title = { Text(text = txtDelTitle, fontWeight = FontWeight.Bold) },
                text = { Text(text = "$txtDelConfirm '${budgetToDelete?.title}'?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            budgetToDelete?.let { budget ->
                                db.collection("budgets").document(budget.id).delete()
                                    .addOnSuccessListener { budgetToDelete = null }
                            }
                        }
                    ) { Text(txtYes, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { budgetToDelete = null }) { Text(txtCancel) }
                }
            )
        }
    }
}

@Composable
private fun BudgetCard(
    budget: BudgetItem,
    isAdmin: Boolean,
    formatRupiah: NumberFormat,
    currentLanguage: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isIncome = budget.type == "Pemasukan"
    val cardColor = if (isIncome) CyberGreen else NeonPink
    val iconType = if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward

    val isEn = currentLanguage == "EN"
    val txtDisplayType = if (isEn) { if (isIncome) "Income" else "Expense" } else budget.type
    val txtEdit = if (isEn) "Edit Transaction" else "Edit Transaksi"
    val txtDel = if (isEn) "Delete" else "Hapus"

    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = cardColor.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconAvatar(icon = iconType, tint = cardColor, size = 42.dp)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = txtDisplayType,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if(isIncome) "+ " else "- ") + formatRupiah.format(budget.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )

                if (isAdmin) {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Opsi", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text(txtEdit) },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                onClick = { expanded = false; onEditClick() }
                            )
                            DropdownMenuItem(
                                text = { Text(txtDel, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                                onClick = { expanded = false; onDeleteClick() }
                            )
                        }
                    }
                }
            }
        }
    }
}