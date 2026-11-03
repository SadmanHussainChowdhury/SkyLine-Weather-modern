package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.WeatherApplication
import com.example.data.notification.WeatherAlertNotification
import com.example.data.notification.WeatherNotificationManager
import com.example.data.repository.WeatherData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun FcmAlertsCard(
    weather: WeatherData,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as? WeatherApplication
    val notificationManager = remember {
        app?.container?.notificationManager ?: WeatherNotificationManager(context)
    }

    val fcmToken by notificationManager.fcmToken.collectAsState()
    val subscribedTopics by notificationManager.subscribedTopics.collectAsState()
    val recentAlerts by notificationManager.recentAlerts.collectAsState()
    val isAutoReadTts by notificationManager.isAutoReadFcmAlerts.collectAsState()
    val isTtsSpeaking by notificationManager.isTtsSpeaking.collectAsState()
    val currentlySpeakingAlertId by notificationManager.currentlySpeakingAlertId.collectAsState()

    // Auto-subscribe to GPS city topic when weather location changes
    LaunchedEffect(weather.city) {
        notificationManager.subscribeToGpsLocationTopic(
            cityName = weather.city,
            latitude = 37.7749,
            longitude = -122.4194
        )
    }

    // Permission handling for Android 13+
    val postNotificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    var showTokenDetails by remember { mutableStateOf(false) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val accentColor = Color(0xFFE53935) // Emergency warning red

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("fcm_alerts_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "FCM Real-time Alerts Icon",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Real-Time FCM Weather Warnings",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "GPS-targeted severe weather push system",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                // FCM Active Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FCM Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }

            // Notification permission banner if missing
            if (postNotificationPermissionState != null && !postNotificationPermissionState.status.isGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = "Permission needed",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable notifications to receive GPS severe alerts.",
                                fontSize = 12.sp,
                                color = primaryText
                            )
                        }
                        Button(
                            onClick = { postNotificationPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Enable", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Text-to-Speech Accessibility & Auto-Read Control Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color(0xFFF1F5F9).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, if (isAutoReadTts) Color(0xFF0284C7).copy(alpha = 0.5f) else cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isAutoReadTts) Color(0xFF0284C7).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoReadTts) Icons.Outlined.RecordVoiceOver else Icons.Outlined.VolumeOff,
                                contentDescription = "TTS Voice Auto-Read Toggle",
                                tint = if (isAutoReadTts) Color(0xFF0284C7) else secondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Voice Auto-Read (Screen-Free)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isAutoReadTts) Color(0xFF0284C7).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isAutoReadTts) "TTS ACTIVE" else "TTS OFF",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isAutoReadTts) Color(0xFF0284C7) else secondaryText,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Automatically synthesizes critical FCM alerts aloud for eyes-free accessibility.",
                                fontSize = 11.sp,
                                color = secondaryText,
                                lineHeight = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAutoReadTts,
                        onCheckedChange = { notificationManager.setAutoReadFcmAlerts(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        ),
                        modifier = Modifier.testTag("toggle_fcm_tts_button")
                    )
                }
            }

            // Active GPS Subscriptions Info
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF0F4F8).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CellTower,
                                contentDescription = "Location Topic",
                                tint = primaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GPS Alert Zone: ${weather.city}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }

                        Text(
                            text = "${subscribedTopics.size} Topics Subscribed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryText
                        )
                    }

                    // Topic chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subscribedTopics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Subscribed",
                                        tint = accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = topic,
                                        fontSize = 11.sp,
                                        color = primaryText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Test FCM Trigger Actions Bar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Test FCM Real-time GPS Push Warnings:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            notificationManager.simulateIncomingFcmPush(
                                title = "Tornado Warning Issued",
                                body = "A confirmed tornado was located over ${weather.city}, moving east at 35 mph. Take shelter in a basement or interior room immediately!",
                                severity = "Extreme",
                                location = weather.city
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("simulate_tornado_fcm_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Send,
                                contentDescription = "Simulate FCM Tornado Alert",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tornado Warning", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            notificationManager.simulateIncomingFcmPush(
                                title = "Flash Flood Watch",
                                body = "Torrential rainfall expected in ${weather.city}. Rapid water rises in small creeks and urban streams possible.",
                                severity = "Severe",
                                location = weather.city
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, accentColor),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("simulate_flood_fcm_button")
                    ) {
                        Text("Flash Flood Watch", fontSize = 12.sp, color = primaryText, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Recent Received Real-time Alerts List
            if (recentAlerts.isNotEmpty()) {
                HorizontalDivider(color = cardBorder)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Real-time Alerts Received (${recentAlerts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )

                    recentAlerts.take(3).forEach { alert ->
                        val isSpeakingThis = currentlySpeakingAlertId == alert.id
                        AlertItemRow(
                            alert = alert,
                            theme = theme,
                            isSpeaking = isSpeakingThis,
                            onReadAloud = {
                                if (isSpeakingThis) {
                                    notificationManager.stopTts()
                                } else {
                                    notificationManager.speakAlert(alert)
                                }
                            }
                        )
                    }
                }
            }

            // Token Details Expander
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTokenDetails = !showTokenDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "FCM Registration Details",
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FCM Token & System Info",
                        fontSize = 12.sp,
                        color = secondaryText
                    )
                }

                Icon(
                    imageVector = if (showTokenDetails) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = "Expand details",
                    tint = secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = showTokenDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FCM Registration Token:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = fcmToken ?: "Fetching token...",
                            fontSize = 10.5.sp,
                            color = secondaryText,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemRow(
    alert: WeatherAlertNotification,
    theme: WeatherTheme,
    isSpeaking: Boolean,
    onReadAloud: () -> Unit
) {
    val isDark = theme.primaryTextColor == Color.White
    val alertBg = if (isDark) Color(0xFF2C1517) else Color(0xFFFFEBEE)
    val alertBorder = Color(0xFFEF5350)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = alertBg,
        border = BorderStroke(1.dp, if (isSpeaking) Color(0xFF0284C7) else alertBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaking) Color(0xFF0284C7) else alertBorder),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isSpeaking) "🔊" else "🚨", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryTextColor,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.timestamp,
                        fontSize = 10.5.sp,
                        color = theme.secondaryTextColor
                    )
                }

                Text(
                    text = alert.body,
                    fontSize = 12.sp,
                    color = theme.primaryTextColor.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Loc: ${alert.location}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.secondaryTextColor
                        )
                        Text(
                            text = "• ${alert.severity}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = alertBorder
                        )
                    }

                    // Read Aloud / Stop Speaking Action Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSpeaking) Color(0xFFE53935) else Color(0xFF0284C7),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onReadAloud() }
                            .testTag("read_aloud_alert_${alert.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Outlined.VolumeOff else Icons.AutoMirrored.Outlined.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop Reading Alert" else "Read Alert Aloud",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpeaking) "Stop" else "Read Aloud",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
