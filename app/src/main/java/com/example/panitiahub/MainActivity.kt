package com.example.panitiahub

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.panitiahub.ui.*
import com.example.panitiahub.ui.theme.PanitiaHubTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val CHANNEL_ID = "panitiahub_notifications"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        val db = Firebase.firestore
        enableEdgeToEdge()

        // Daftarkan jalur notifikasi Android
        createNotificationChannel()

        setContent {
            // State Kontrol Global Aplikasi
            var isDarkTheme by remember { mutableStateOf(true) }
            var isNotificationsEnabled by remember { mutableStateOf(true) }
            var appLanguage by remember { mutableStateOf("ID") } // Default Bahasa Indonesia

            PanitiaHubTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUser by remember { mutableStateOf(auth.currentUser) }
                    var userRole by remember { mutableStateOf("") }
                    var currentUsername by remember { mutableStateOf("") }
                    var isCheckingRole by remember { mutableStateOf(false) }
                    var currentScreen by remember { mutableStateOf("dashboard") }
                    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }

                    // Pencegat Tombol Back HP
                    BackHandler(enabled = currentScreen != "dashboard" && currentScreen != "login") {
                        when (currentScreen) {
                            "profile" -> currentScreen = "dashboard"
                            "event_detail" -> {
                                currentScreen = "dashboard"
                                selectedEvent = null
                            }
                            "task", "budget", "vendor", "link", "cp", "template", "attendance", "inventory" -> {
                                currentScreen = "event_detail"
                            }
                        }
                    }

                    // Tarik data User Role dari Firebase
                    LaunchedEffect(currentUser) {
                        currentUser?.email?.let { email ->
                            isCheckingRole = true
                            db.collection("users").document(email).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        userRole = document.getString("role") ?: "Aktivis"
                                        currentUsername = document.getString("username") ?: email.substringBefore("@")
                                    } else {
                                        userRole = "Aktivis"
                                        currentUsername = email.substringBefore("@")
                                    }
                                    isCheckingRole = false
                                }
                                .addOnFailureListener {
                                    userRole = "error"
                                    currentUsername = "Panitia"
                                    isCheckingRole = false
                                }
                        }
                    }

                    // TAMPILAN UTAMA (ROUTER)
                    if (currentUser == null) {
                        LoginScreen(
                            onLoginClick = { email, inputUsername, password ->
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this@MainActivity) { task ->
                                            if (task.isSuccessful) {
                                                val userData = hashMapOf(
                                                    "email" to email,
                                                    "username" to inputUsername
                                                )
                                                db.collection("users").document(email)
                                                    .set(userData, SetOptions.merge())

                                                currentUser = auth.currentUser
                                                Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@MainActivity, "Login Gagal. Cek email/password.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                        )
                    } else {
                        // JIKA SEDANG MENUNGGU DATA FIREBASE, TAMPILKAN SPINNER LOADING
                        if (isCheckingRole || userRole.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            // JIKA DATA SIAP, ARAHKAN KE HALAMAN YANG TEPAT
                            when (currentScreen) {
                                "dashboard" -> {
                                    DashboardScreen(
                                        username = currentUsername,
                                        userRole = userRole,
                                        currentLanguage = appLanguage,
                                        onEventClick = { event ->
                                            selectedEvent = event
                                            currentScreen = "event_detail"
                                        },
                                        onProfileClick = { currentScreen = "profile" }
                                    )
                                }
                                "event_detail" -> {
                                    EventDetailScreen(
                                        eventName = selectedEvent?.name ?: "",
                                        eventDate = selectedEvent?.date ?: "",
                                        currentLanguage = appLanguage,
                                        onBackClick = {
                                            currentScreen = "dashboard"
                                            selectedEvent = null
                                        },
                                        onTaskClick = { currentScreen = "task" },
                                        onBudgetClick = { currentScreen = "budget" },
                                        onVendorClick = { currentScreen = "vendor" },
                                        onLinkClick = { currentScreen = "link" },
                                        onCpClick = { currentScreen = "cp" },
                                        onTemplateClick = { currentScreen = "template" },
                                        onAttendanceClick = { currentScreen = "attendance" },
                                        onInventoryClick = { currentScreen = "inventory" }
                                    )
                                }
                                "task" -> TaskScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "budget" -> BudgetScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "vendor" -> VendorScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "link" -> LinkScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "cp" -> CpScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "template" -> TemplateScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "attendance" -> AttendanceScreen(userRole = userRole, username = currentUsername, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "inventory" -> InventoryScreen(userRole = userRole, eventName = selectedEvent?.name ?: "", currentLanguage = appLanguage, onBackClick = { currentScreen = "event_detail" })
                                "profile" -> {
                                    ProfileScreen(
                                        username = currentUsername,
                                        userRole = userRole,
                                        isDarkTheme = isDarkTheme,
                                        onDarkThemeChange = { isDarkTheme = it },
                                        isNotificationsEnabled = isNotificationsEnabled,
                                        onNotificationsChange = { enabled ->
                                            isNotificationsEnabled = enabled
                                            if (enabled) {
                                                triggerAndroidNotification(
                                                    "Notifikasi Aktif 🔔",
                                                    "Halo $currentUsername, kamu akan menerima info pleno & logistik langsung dari atas sini!"
                                                )
                                            } else {
                                                Toast.makeText(this@MainActivity, "Semua pemberitahuan disenyapkan.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        currentLanguage = appLanguage,
                                        onLanguageChange = { appLanguage = it },
                                        onBackClick = { currentScreen = "dashboard" },
                                        onLogoutClick = {
                                            auth.signOut()
                                            currentUser = null
                                            userRole = ""
                                            currentUsername = ""
                                            currentScreen = "dashboard"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } // <--- INI DIA! TUTUP onCreate YANG BENAR

    // Fungsi Sistem Menembakkan Notifikasi Melayang
    @SuppressLint("MissingPermission")
    private fun triggerAndroidNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Registrasi Jalur Saluran Notifikasi Sistem
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pemberitahuan Agenda PanitiaHub"
            val descriptionText = "Saluran informasi real-time koordinasi kepanitiaan"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}