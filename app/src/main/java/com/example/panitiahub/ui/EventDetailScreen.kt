package com.example.panitiahub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.*

@Composable
fun EventDetailScreen(
    eventName: String,
    eventDate: String,
    currentLanguage: String, // <--- PARAMETER BAHASA
    onBackClick: () -> Unit,
    onTaskClick: () -> Unit,
    onBudgetClick: () -> Unit,
    onVendorClick: () -> Unit,
    onLinkClick: () -> Unit,
    onCpClick: () -> Unit,
    onTemplateClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onInventoryClick: () -> Unit
) {
    // --- KAMUS TERJEMAHAN ---
    val isEn = currentLanguage == "EN"
    val txtWorkspace = if (isEn) "Committee Workspace" else "Ruang Kerja Kepanitiaan"
    val txtTask = if (isEn) "Task\nManagement" else "Manajemen\nTugas"
    val txtBudget = if (isEn) "Manage\nBudget" else "Kelola\nAnggaran"
    val txtVendor = if (isEn) "Vendor\nStatus" else "Status\nVendor"
    val txtLink = if (isEn) "Link\nCollection" else "Kumpulan\nLink"
    val txtCp = if (isEn) "Contact\nPerson" else "Contact\nPerson"
    val txtTemplate = if (isEn) "Design\nTemplates" else "Template\nDesain"
    val txtAttendance = if (isEn) "Meeting\nAttendance" else "Absensi\nRapat"
    val txtInventory = if (isEn) "Equipment\nBorrowing" else "Peminjaman\nAlat"

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = eventName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = eventDate, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = txtWorkspace, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()
        ) {
            item { EventMenuCard(txtTask, Icons.Filled.Assignment, Color(0xFFF5A623), onTaskClick) }
            item { EventMenuCard(txtBudget, Icons.Filled.AccountBalanceWallet, ElectricBlue, onBudgetClick) }
            item { EventMenuCard(txtVendor, Icons.Filled.Storefront, NeonPink, onVendorClick) }
            item { EventMenuCard(txtLink, Icons.Filled.Link, CyberGreen, onLinkClick) }
            item { EventMenuCard(txtCp, Icons.Filled.Contacts, Color(0xFF9B51E0), onCpClick) }
            item { EventMenuCard(txtTemplate, Icons.Filled.DesignServices, Color(0xFFE2B93B), onTemplateClick) }
            item { EventMenuCard(txtAttendance, Icons.Filled.FactCheck, Color(0xFFE91E63), onAttendanceClick) }
            item { EventMenuCard(txtInventory, Icons.Filled.Build, Color(0xFF00BCD4), onInventoryClick) }
        }
    }
}

@Composable
private fun EventMenuCard(label: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    GlowCard(
        modifier = Modifier.height(120.dp).clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        glowColor = accent.copy(alpha = 0.2f),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconAvatar(icon = icon, tint = accent, size = 36.dp)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}