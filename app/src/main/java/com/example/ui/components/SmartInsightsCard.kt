package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SmartInsightData

@Composable
fun SmartInsightsCard(
    insight: SmartInsightData?,
    isLoading: Boolean,
    theme: WeatherTheme,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor

    val aiAccentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val aiGradient = Brush.horizontalGradient(
        colors = if (isDark) listOf(Color(0xFF0284C7), Color(0xFF6366F1))
        else listOf(Color(0xFFE0F2FE), Color(0xFFEEF2FF))
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("smart_insights_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
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
                            .background(aiAccentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Smart Insights Icon",
                            tint = aiAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Insights",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Daily Personalized Weather Advice",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // AI Badge Tag
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = aiAccentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(aiAccentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (insight?.isAiGenerated == true) "GEMINI AI" else "SMART RULES",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = aiAccentColor
                        )
                    }
                }
            }

            if (isLoading) {
                // Loading Skeleton state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = aiAccentColor,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Consulting Gemini AI for daily outfit & activity advice...",
                        fontSize = 13.sp,
                        color = secondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (insight != null) {
                // Main Headline Pill Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, aiAccentColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(aiGradient)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = "Advice Headline",
                                tint = if (isDark) Color.White else Color(0xFF0369A1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "TODAY'S TOP RECOMMENDATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF0369A1).copy(alpha = 0.85f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = insight.mainHeadline,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color.White else Color(0xFF0369A1),
                                    modifier = Modifier.testTag("smart_insights_headline")
                                )
                            }
                        }
                    }
                }

                // Section 1: Outfit & Clothing Advice
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Checkroom,
                                contentDescription = "Outfit Advice",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Outfit & Layers Guidance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insight.outfitAdvice,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = primaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Section 2: Outdoor Activity & Commute Tip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsWalk,
                                contentDescription = "Activity Tip",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Outdoor Activity & Commute",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insight.activityTip,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = primaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Section 3: Sun & Rain Precautions
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = "Sun and Rain Protection",
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sun & Rain Protection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insight.sunAndRainAdvice,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = primaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Key Takeaway Chips
                if (insight.keyTakeaways.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "QUICK TIPS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = secondaryText,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            insight.keyTakeaways.take(3).forEach { tip ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, cardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = tip,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Bar / Regenerate Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, aiAccentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("regenerate_insights_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Regenerate AI Advice",
                            tint = aiAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh AI Advice",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = aiAccentColor
                        )
                    }
                }
            }
        }
    }
}
