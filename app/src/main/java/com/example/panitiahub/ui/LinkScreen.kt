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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
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
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.TextSecondary
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class LinkItem(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val eventName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkScreen(
    userRole: String,
    eventName: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit
) {
    val db = Firebase.firestore
    val context = LocalContext.current

    var links by remember { mutableStateOf(listOf<LinkItem>()) }
    var isLoading by remember { mutableStateOf(true) }

    var showAddLinkSheet by remember { mutableStateOf(false) }
    val addLinkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var linkTitle by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtHeader = if (isEn) "Link Collection" else "Kumpulan Tautan"
    val txtEvent = if (isEn) "Event: $eventName" else "Acara: $eventName"
    val txtNoLink = if (isEn) "No workspace links uploaded yet." else "Belum ada link pengerjaan diupload."
    val txtInvalidLink = if (isEn) "Invalid link format!" else "Format link salah atau tidak valid!"
    val txtAddLinkTitle = if (isEn) "Share Workspace Link" else "Bagikan Tautan Pengerjaan"
    val txtDocName = if (isEn) "Document Name (e.g. Budget Spreadsheet)" else "Nama Dokumen (Cth: Google Spreadsheet Anggaran)"
    val txtUrl = if (isEn) "URL Address (Must start with https://)" else "Alamat URL (Harus diawali https://)"
    val txtErrEmpty = if (isEn) "Title and URL cannot be empty!" else "Judul dan alamat URL tidak boleh kosong!"
    val txtErrSave = if (isEn) "Failed to save link." else "Gagal menyimpan tautan."
    val txtSaving = if (isEn) "Saving..." else "Menyimpan..."
    val txtSave = if (isEn) "Save Link" else "Simpan Tautan"

    LaunchedEffect(Unit) {
        db.collection("links")
            .whereEqualTo("eventName", eventName)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedLinks = snapshot.documents.map { doc ->
                        LinkItem(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            url = doc.getString("url") ?: "",
                            eventName = doc.getString("eventName") ?: ""
                        )
                    }
                    links = fetchedLinks
                    isLoading = false
                }
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLinkSheet = true },
                containerColor = CyberGreen,
                contentColor = Color(0xFF06110D),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Link")
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
                        CircularProgressIndicator(color = ElectricBlue)
                    }
                }
                links.isEmpty() -> {
                    GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = CyberGreen.copy(alpha = 0.15f)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = txtNoLink,
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
                        items(links) { link ->
                            GlowCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                                            context.startActivity(urlIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, txtInvalidLink, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                glowColor = CyberGreen.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconAvatar(icon = Icons.Filled.Link, tint = CyberGreen, size = 42.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = link.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = link.url,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddLinkSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddLinkSheet = false
                    errorMessage = null
                },
                sheetState = addLinkSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(txtAddLinkTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = linkTitle,
                        onValueChange = { linkTitle = it },
                        label = { Text(txtDocName) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text(txtUrl) },
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
                            val trimmedTitle = linkTitle.trim()
                            var trimmedUrl = linkUrl.trim()

                            if (trimmedTitle.isEmpty() || trimmedUrl.isEmpty()) {
                                errorMessage = txtErrEmpty
                                return@Button
                            }

                            if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
                                trimmedUrl = "https://$trimmedUrl"
                            }

                            errorMessage = null
                            isSubmitting = true

                            val linkData = hashMapOf(
                                "title" to trimmedTitle,
                                "url" to trimmedUrl,
                                "eventName" to eventName
                            )

                            db.collection("links").add(linkData)
                                .addOnSuccessListener {
                                    linkTitle = ""
                                    linkUrl = ""
                                    isSubmitting = false
                                    showAddLinkSheet = false
                                }
                                .addOnFailureListener {
                                    errorMessage = txtErrSave
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
    }
}