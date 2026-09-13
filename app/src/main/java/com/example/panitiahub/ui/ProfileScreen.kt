package com.example.panitiahub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.panitiahub.ui.components.GlowCard
import com.example.panitiahub.ui.components.IconAvatar
import com.example.panitiahub.ui.theme.ElectricBlue
import com.example.panitiahub.ui.theme.NeonPink
import com.example.panitiahub.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    userRole: String,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    currentLanguage: String, // <--- PARAMETER BAHASA (ID / EN)
    onLanguageChange: (String) -> Unit, // <--- AKSI GANTI BAHASA
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val isAdmin = userRole == "Administrator"
    val roleBadgeColor = if (isAdmin) NeonPink else ElectricBlue
    val scrollState = rememberScrollState()

    // State untuk menampilkan Pop-up Dialog Bahasa
    var showLanguageDialog by remember { mutableStateOf(false) }

    // --- KAMUS TERJEMAHAN MANUAL BERDASARKAN STATE ---
    val isEn = currentLanguage == "EN"

    val txtHeader = if (isEn) "Profile Settings" else "Pengaturan Profil"
    val txtAccount = if (isEn) "Account" else "Akun"
    val txtAccess = if (isEn) "System Access Rights" else "Hak Akses Sistem"
    val txtAccessAdmin = if (isEn) "Full Access (Edit, Delete, Add)" else "Akses Penuh (Edit, Hapus, Tambah)"
    val txtAccessUser = if (isEn) "Limited Access (View Data)" else "Akses Terbatas (Melihat Data)"
    val txtPref = if (isEn) "Application Preferences" else "Preferensi Aplikasi"
    val txtDarkSub = if (isEn) "Change appearance to dark mode" else "Ubah tampilan menjadi gelap"
    val txtNotif = if (isEn) "Notifications" else "Notifikasi"
    val txtNotifSub = if (isEn) "Meeting schedule & task deadline reminders" else "Pengingat jadwal rapat & tenggat tugas"
    val txtLang = if (isEn) "Language" else "Bahasa"
    val txtLangValue = if (isEn) "English" else "Indonesia"
    val txtOthers = if (isEn) "Others" else "Lainnya"
    val txtHelp = if (isEn) "Help Center" else "Pusat Bantuan"
    val txtAppStatus = if (isEn) "Application Status" else "Status Aplikasi"
    val txtLogout = if (isEn) "Logout" else "Keluar Aplikasi"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Header ----------
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = txtHeader, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            IconAvatar(icon = Icons.Filled.AccountCircle, tint = ElectricBlue, size = 96.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = username, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(color = roleBadgeColor.copy(alpha = 0.15f), contentColor = roleBadgeColor, shape = RoundedCornerShape(50)) {
                Text(text = userRole.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- Kategori: Akun ----------
            Box(modifier = Modifier.fillMaxWidth()) { Text(txtAccount, style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) }
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = ElectricBlue.copy(alpha = 0.05f)) {
                Column {
                    ProfileSettingItem(icon = Icons.Filled.Shield, title = txtAccess, subtitle = if (isAdmin) txtAccessAdmin else txtAccessUser)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Kategori: Preferensi Aplikasi ----------
            Box(modifier = Modifier.fillMaxWidth()) { Text(txtPref, style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) }
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = ElectricBlue.copy(alpha = 0.05f)) {
                Column {
                    ProfileSettingItem(icon = Icons.Filled.DarkMode, title = "Dark Theme", subtitle = txtDarkSub, trailing = { Switch(checked = isDarkTheme, onCheckedChange = onDarkThemeChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricBlue)) })
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileSettingItem(icon = Icons.Filled.Notifications, title = txtNotif, subtitle = txtNotifSub, trailing = { Switch(checked = isNotificationsEnabled, onCheckedChange = onNotificationsChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricBlue)) })
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    // TOMBOL BAHASA (Membuka Dialog)
                    ProfileSettingItem(
                        icon = Icons.Filled.Language,
                        title = txtLang,
                        modifier = Modifier.clickable { showLanguageDialog = true },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(txtLangValue, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Kategori: Lainnya ----------
            Box(modifier = Modifier.fillMaxWidth()) { Text(txtOthers, style = MaterialTheme.typography.labelLarge, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)) }
            GlowCard(modifier = Modifier.fillMaxWidth(), glowColor = ElectricBlue.copy(alpha = 0.05f)) {
                Column {
                    ProfileSettingItem(icon = Icons.Filled.HelpOutline, title = txtHelp, trailing = { Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary) })
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileSettingItem(icon = Icons.Filled.Info, title = txtAppStatus, subtitle = "PanitiaHub v1.0 (Production)")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- Tombol Keluar ----------
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(txtLogout, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ---------- DIALOG PILIH BAHASA DENGAN RADIO BUTTON ----------
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(text = if (isEn) "Select Language" else "Pilih Bahasa", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        // Opsi Bahasa Indonesia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                onLanguageChange("ID")
                                showLanguageDialog = false
                            }.padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguage == "ID",
                                onClick = {
                                    onLanguageChange("ID")
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bahasa Indonesia", style = MaterialTheme.typography.bodyLarge)
                        }

                        // Opsi English
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable {
                                onLanguageChange("EN")
                                showLanguageDialog = false
                            }.padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguage == "EN",
                                onClick = {
                                    onLanguageChange("EN")
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("English", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(if (isEn) "Cancel" else "Batal", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}