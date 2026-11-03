package com.example.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R

@RawRes
fun getLottieResForCondition(conditionCode: Int): Int {
    return when (conditionCode) {
        1000 -> R.raw.sunny // Sunny, Clear
        1003, 1006, 1009, 1030, 1135, 1147 -> R.raw.cloudy // Partly cloudy, Cloudy, Overcast, Fog
        1063, 1150, 1153, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201, 1240, 1243, 1246 -> R.raw.rainy // Rain, Drizzle
        1066, 1069, 1072, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264 -> R.raw.snowy // Snow, Sleet
        1087, 1273, 1276, 1279, 1282 -> R.raw.thunderstorm // Thunderstorm
        else -> R.raw.cloudy // Default fallback
    }
}

fun getConditionGlowColor(conditionCode: Int): Color {
    return when (conditionCode) {
        1000 -> Color(0xFFF59E0B) // Amber Gold for Sun
        1003, 1006, 1009, 1030, 1135, 1147 -> Color(0xFF38BDF8) // Vibrant Sky Blue Glow for Overcast & Clouds
        1063, 1150, 1153, 1180, 1183, 1186, 1189, 1192, 1195, 1198, 1201, 1240, 1243, 1246 -> Color(0xFF0284C7) // Sky Blue for Rain
        1066, 1069, 1072, 1114, 1117, 1204, 1207, 1210, 1213, 1216, 1219, 1222, 1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264 -> Color(0xFFE2E8F0) // Ice White for Snow
        1087, 1273, 1276, 1279, 1282 -> Color(0xFFA855F7) // Purple for Thunderstorm
        else -> Color(0xFF38BDF8)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherLottieAnimation(
    conditionCode: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    speed: Float = 1.0f,
    showGlowHalo: Boolean = true,
    tintColor: Color = Color.Unspecified
) {
    val rawRes = getLottieResForCondition(conditionCode)
    val glowColor = getConditionGlowColor(conditionCode)

    // Animated glow color transition buffer for ambient halo cross-fade
    val animatedGlowColor by animateColorAsState(
        targetValue = glowColor,
        animationSpec = tween(durationMillis = 550, easing = LinearOutSlowInEasing),
        label = "weather_glow_color_transition"
    )

    // AnimatedContent transition buffer combining cross-fade and scale expansion/reduction
    AnimatedContent(
        targetState = rawRes,
        transitionSpec = {
            (fadeIn(animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)) +
             scaleIn(initialScale = 0.92f, animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)))
                .togetherWith(
                    fadeOut(animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing)) +
                    scaleOut(targetScale = 1.08f, animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing))
                )
        },
        label = "lottie_condition_transition"
    ) { currentRes ->
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(currentRes))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            speed = speed
        )

        Box(
            modifier = modifier
                .size(size)
                .testTag("weather_lottie_animation_$conditionCode"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient condition halo circle container
            if (showGlowHalo) {
                Box(
                    modifier = Modifier
                        .size(size * 0.9f)
                        .clip(CircleShape)
                        .background(animatedGlowColor.copy(alpha = 0.20f))
                        .blur(16.dp)
                )
                Box(
                    modifier = Modifier
                        .size(size * 0.82f)
                        .clip(CircleShape)
                        .background(animatedGlowColor.copy(alpha = 0.12f))
                )
            }

            // Always display the relevant weather logo icon inside the circle so it STAYS visible permanently
            Icon(
                imageVector = getWeatherIcon(conditionCode),
                contentDescription = "Weather condition icon",
                tint = if (tintColor != Color.Unspecified) tintColor else animatedGlowColor,
                modifier = Modifier.size(size * 0.52f)
            )

            // Overlay active Lottie vector weather effects when loaded
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}

@Composable
fun WeatherLottieShowcaseCard(
    currentConditionCode: Int,
    theme: WeatherTheme,
    modifier: Modifier = Modifier
) {
    var selectedConditionCode by remember(currentConditionCode) { mutableIntStateOf(currentConditionCode) }
    var animSpeed by remember { mutableFloatStateOf(1.0f) }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.92f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val conditions = remember {
        listOf(
            Triple(1000, "Sun", "☀️"),
            Triple(1003, "Cloud", "☁️"),
            Triple(1189, "Rain", "🌧️"),
            Triple(1213, "Snow", "❄️"),
            Triple(1087, "Storm", "🌩️")
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("weather_lottie_showcase_card"),
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
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Animation,
                            contentDescription = "Dynamic Lottie Weather Animation",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dynamic Weather Lottie",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Real-Time Vector Atmosphere FX",
                            fontSize = 11.5.sp,
                            color = secondaryText
                        )
                    }
                }

                // Speed Selector Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(0.75f, 1.0f, 1.5f).forEach { speedVal ->
                            val isSelected = animSpeed == speedVal
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accentColor else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { animSpeed = speedVal }
                                    .testTag("lottie_speed_$speedVal")
                            ) {
                                Text(
                                    text = "${speedVal}x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else secondaryText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Center Stage: Animated Lottie Display
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WeatherLottieAnimation(
                        conditionCode = selectedConditionCode,
                        size = 140.dp,
                        speed = animSpeed,
                        showGlowHalo = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (selectedConditionCode) {
                            1000 -> "Sunny & Clear Sky"
                            1003 -> "Overcast & Cloudy Atmosphere"
                            1189 -> "Active Precipitation & Rain"
                            1213 -> "Snowfall & Freezing Chill"
                            1087 -> "Thunderstorm & Lightning"
                            else -> "Live Condition Animated"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.testTag("lottie_condition_label")
                    )
                }
            }

            // Interactive Condition Switcher Buttons
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PREVIEW CONDITION ANIMATIONS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = secondaryText,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    conditions.forEach { (code, name, emoji) ->
                        val isSelected = selectedConditionCode == code
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) accentColor else (if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)),
                            border = BorderStroke(1.dp, if (isSelected) accentColor else cardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedConditionCode = code }
                                .testTag("lottie_condition_chip_$code")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else primaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

