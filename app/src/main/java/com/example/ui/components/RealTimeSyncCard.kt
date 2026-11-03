package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealTimeSyncCard(
    isSyncEnabled: Boolean,
    syncIntervalSec: Int,
    lastSyncTimestampMillis: Long,
    isRefreshing: Boolean,
    theme: WeatherTheme,
    onToggleSync: (Boolean) -> Unit,
    onSetIntervalSec: (Int) -> Unit,
    onForceSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfigSheet by remember { mutableStateOf(false) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live seconds tick for relative duration update
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val elapsedSeconds = ((currentTimeMillis - lastSyncTimestampMillis) / 1000L).coerceAtLeast(0L)
    val relativeTimeString = when {
        elapsedSeconds < 5 -> "Just synced"
        elapsedSeconds < 60 -> "${elapsedSeconds}s ago"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ago"
        else -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(lastSyncTimestampMillis))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_rotate")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_rotation"
    )

    Surface(
        modifier = modifier
            .testTag("realtime_sync_card")
            .clip(RoundedCornerShape(14.dp))
            .clickable { showConfigSheet = true },
        color = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.75f),
        border = BorderStroke(
            1.dp,
            if (isSyncEnabled) Color(0xFF10B981).copy(alpha = 0.4f) else theme.secondaryTextColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing Status Indicator Dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSyncEnabled) {
                                Color(0xFF10B981).copy(alpha = pulseAlpha)
                            } else {
                                Color(0xFFF59E0B)
                            }
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isSyncEnabled) "LIVE REAL-TIME SYNC" else "SYNC PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSyncEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isSyncEnabled) {
                            Text(
                                text = "(${syncIntervalSec}s)",
                                fontSize = 10.sp,
                                color = theme.secondaryTextColor
                            )
                        }
                    }
                    Text(
                        text = "Last update: $relativeTimeString",
                        fontSize = 12.sp,
                        color = theme.primaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onForceSyncNow() },
                    modifier = Modifier
                        .testTag("force_sync_button")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync Now",
                        tint = if (theme.primaryTextColor == Color.White) Color(0xFF38BDF8) else Color(0xFF006495),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (isRefreshing) spinRotation else 0f)
                    )
                }

                IconButton(
                    onClick = { showConfigSheet = true },
                    modifier = Modifier
                        .testTag("open_sync_config_button")
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure Realtime Sync",
                        tint = theme.secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Live Sync Configuration
    if (showConfigSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfigSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = if (theme.primaryTextColor == Color.White) Color(0xFF1E293B) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Real-Time Weather Sync",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryTextColor
                            )
                            Text(
                                text = "Instant live data streaming & background updates",
                                fontSize = 12.sp,
                                color = theme.secondaryTextColor
                            )
                        }
                    }
                    Switch(
                        checked = isSyncEnabled,
                        onCheckedChange = { onToggleSync(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier.testTag("realtime_sync_switch")
                    )
                }

                Text(
                    text = "Live Refresh Interval",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primaryTextColor
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intervals = listOf(
                        30 to "30 sec",
                        60 to "1 min",
                        120 to "2 min",
                        300 to "5 min",
                        900 to "15 min"
                    )
                    intervals.forEach { (sec, label) ->
                        val selected = isSyncEnabled && syncIntervalSec == sec
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onToggleSync(true)
                                onSetIntervalSec(sec)
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("sync_interval_chip_${sec}s")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (theme.primaryTextColor == Color.White) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Real-time sync keeps widgets, temperature graphs, wind radar, and AI narrative aligned instantly across all screen changes.",
                            fontSize = 12.sp,
                            color = theme.secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
