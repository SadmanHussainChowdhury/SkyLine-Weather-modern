package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.repository.AllergyRiskLevel
import com.example.data.repository.PollenData
import com.example.data.repository.PollenItem
import com.example.data.repository.PollenRepository
import com.example.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun PollenAllergyCard(
    city: String,
    theme: WeatherTheme,
    pollenRepository: PollenRepository? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pollenData by remember(city) { mutableStateOf<PollenData?>(null) }
    var isLoading by remember(city) { mutableStateOf(true) }

    val actualRepository = remember {
        pollenRepository ?: (context.applicationContext as? android.app.Application)?.let {
            // Retrieve via AppContainer if available
            AppContainer(it.applicationContext).pollenRepository
        }
    }

    // Load Pollen Data whenever city changes or refresh requested
    fun fetchPollen() {
        scope.launch {
            isLoading = true
            pollenData = actualRepository?.getPollenDataForCity(context, city)
            isLoading = false
        }
    }

    LaunchedEffect(city) {
        fetchPollen()
    }

    val data = pollenData ?: return
    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val overallRiskColor = Color(data.overallRiskLevel.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("pollen_allergy_risk_card"),
        shape = RoundedCornerShape(24.dp),
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
                            .background(overallRiskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Grass,
                            contentDescription = "Pollen & Allergy",
                            tint = overallRiskColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Pollen & Allergy Risk",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Live Open-Meteo Pollen Forecast",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Overall Risk Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = overallRiskColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(overallRiskColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = data.overallRiskLevel.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = overallRiskColor
                        )
                    }
                }
            }

            // Primary Allergen Alert Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = overallRiskColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, overallRiskColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DOMINANT ALLERGEN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = overallRiskColor,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = data.primaryAllergen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${data.overallIndex}/100",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = overallRiskColor
                        )
                        Text(
                            text = "Allergy Index",
                            fontSize = 10.5.sp,
                            color = secondaryText
                        )
                    }
                }
            }

            // Breakdown Grid for 4 Pollen Categories
            Text(
                text = "Pollen Breakdown & Species Activity:",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = secondaryText
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PollenCategoryRow(item = data.treePollen, isDark = isDark, primaryText = primaryText, secondaryText = secondaryText)
                PollenCategoryRow(item = data.grassPollen, isDark = isDark, primaryText = primaryText, secondaryText = secondaryText)
                PollenCategoryRow(item = data.weedPollen, isDark = isDark, primaryText = primaryText, secondaryText = secondaryText)
                PollenCategoryRow(item = data.moldSpores, isDark = isDark, primaryText = primaryText, secondaryText = secondaryText)
            }

            HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))

            // Health Advice Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HealthAndSafety,
                        contentDescription = "Health Protection",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Protective Advice:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = data.healthAdvice,
                            fontSize = 12.sp,
                            color = primaryText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Sync Refresh Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: Open-Meteo Pollen API (${data.city})",
                    fontSize = 10.5.sp,
                    color = secondaryText
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { fetchPollen() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Pollen",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLoading) "Updating..." else "Refresh",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PollenCategoryRow(
    item: PollenItem,
    isDark: Boolean,
    primaryText: Color,
    secondaryText: Color
) {
    val riskColor = Color(item.riskLevel.colorHex)
    val progress = (item.countGrainsPerM3.toFloat() / item.maxThreshold.toFloat()).coerceIn(0.05f, 1.0f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${item.dominantSpecies})",
                    fontSize = 11.sp,
                    color = secondaryText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.countGrainsPerM3} gr/m³",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = riskColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.riskLevel.label.replace(" Risk", ""),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = riskColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = riskColor,
            trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    }
}
