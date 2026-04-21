package com.tryout.hopefinder.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tryout.hopefinder.data.DevicePreferences
import com.tryout.hopefinder.ui.theme.*
import com.tryout.hopefinder.viewmodel.DeviceConnectionManager

/**
 * Profile Screen
 */
@Composable
fun ProfileScreen(
    context: android.content.Context? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onLogout: () -> Unit
) {
    val appContext = context ?: LocalContext.current
    var deviceIp by remember {
        mutableStateOf(
            DevicePreferences.getSavedDeviceIp(appContext) ?: DeviceConnectionManager.deviceIp
        )
    }
    var connectionMessage by remember { mutableStateOf<String?>(null) }

    fun connectToDevice() {
        val trimmedIp = deviceIp.trim()
        if (trimmedIp.isBlank()) {
            connectionMessage = "Enter the ESP32 IP address first"
            return
        }

        DevicePreferences.saveDeviceIp(appContext, trimmedIp)
        DeviceConnectionManager.initialize(trimmedIp)
        connectionMessage = "Connected to $trimmedIp"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(paddingValues)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Lead Operator",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = PrimaryDeepBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PrimaryDeepBlue)
                ) {
                    Text(
                        text = "MISSION SPECIALIST",
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        item {
            SectionHeader("DEVICE CONNECTION")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Point the app at your ESP32 MicroPython server.",
                        color = TextSecondaryGray,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = deviceIp,
                        onValueChange = { deviceIp = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("ESP32 IP Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = BorderSubtle,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondaryGray,
                            cursorColor = AccentCyan
                        )
                    )

                    Button(
                        onClick = { connectToDevice() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save and Connect", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = connectionMessage ?: "Saved target: ${DeviceConnectionManager.deviceIp}",
                        color = if (connectionMessage == null) TextTertiaryGray else AccentCyan,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Stats Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    label = "MISSIONS",
                    value = "24",
                    icon = Icons.Default.TaskAlt,
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                ProfileStatCard(
                    label = "ALERTS",
                    value = "142",
                    icon = Icons.Default.Warning,
                    color = WarningYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Information Sections
        item {
            SectionHeader("MISSION IDENTITY")
            InfoRow(label = "Email", value = "operator@mission.control", icon = Icons.Default.Email)
            InfoRow(label = "User ID", value = "MF-882-OP", icon = Icons.Default.Badge)
            InfoRow(label = "Radar Unit", value = "HF-V2 PRO", icon = Icons.Default.Radar)
        }

        item {
            SectionHeader("OPERATIONAL SETTINGS")
            ToggleRow(label = "Night Ops Mode", initialValue = true)
            ToggleRow(label = "Critical Alerts", initialValue = true)
            ToggleRow(label = "Telemetry Sync", initialValue = false)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCriticalRed),
                border = BorderStroke(1.dp, StatusCriticalRed)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TERMINATE SESSION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, color = TextSecondaryGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondaryGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 12.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondaryGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = TextSecondaryGray, fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ToggleRow(label: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.LightGray, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentCyan,
                checkedTrackColor = AccentCyan.copy(alpha = 0.5f),
                uncheckedThumbColor = TextSecondaryGray,
                uncheckedTrackColor = SurfaceSlate
            )
        )
    }
}
