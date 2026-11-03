package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WeatherData
import java.util.Locale

class WeatherTtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    var isInitialized by mutableStateOf(false)
        private set
    var isSpeaking by mutableStateOf(false)
        private set
    var speechRate by mutableFloatStateOf(1.0f)
        private set

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            var result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts?.setLanguage(Locale.US)
            }
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                tts?.setSpeechRate(speechRate)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        isSpeaking = false
                    }
                })
            } else {
                isInitialized = false
            }
        } else {
            isInitialized = false
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            Toast.makeText(context, "Text-To-Speech engine is initializing...", Toast.LENGTH_SHORT).show()
            return
        }
        if (isSpeaking) {
            stop()
        } else {
            tts?.setSpeechRate(speechRate)
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "weather_summary_utterance")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "weather_summary_utterance")
            isSpeaking = true
        }
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    fun setSpeed(rate: Float) {
        speechRate = rate
        tts?.setSpeechRate(rate)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        isSpeaking = false
        isInitialized = false
    }
}

@Composable
fun rememberWeatherTtsManager(): WeatherTtsManager {
    val context = LocalContext.current
    val ttsManager = remember { WeatherTtsManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    return ttsManager
}

fun generateWeatherSummaryText(
    weather: WeatherData,
    useCelsius: Boolean
): String {
    val tempUnit = if (useCelsius) "degrees Celsius" else "degrees Fahrenheit"
    val currentTemp = Math.round(if (useCelsius) weather.tempC else weather.tempF)
    val feelsLike = Math.round(if (useCelsius) weather.feelsLikeC else weather.feelsLikeF)
    val windSpeedUnit = if (useCelsius) "kilometers per hour" else "miles per hour"
    val windSpeed = Math.round(if (useCelsius) weather.windKph else weather.windMph)

    val todayForecast = weather.dailyForecast.firstOrNull()
    val highLowPart = if (todayForecast != null) {
        val high = Math.round(if (useCelsius) todayForecast.maxTempC else todayForecast.maxTempF)
        val low = Math.round(if (useCelsius) todayForecast.minTempC else todayForecast.minTempF)
        "Today's high is expected to reach $high $tempUnit, with a low of $low."
    } else ""

    val alertPart = if (!weather.alertHeadline.isNullOrEmpty()) {
        "Please note: A weather alert is active for ${weather.city}: ${weather.alertHeadline}."
    } else ""

    val uvPart = when {
        weather.uv >= 8.0 -> "The UV index is very high at ${weather.uv}. Remember to protect your skin outdoors."
        weather.uv >= 6.0 -> "The UV index is high at ${weather.uv}."
        else -> ""
    }

    return "Here is your weather update for ${weather.city}. Currently, it is ${weather.conditionText} with a temperature of $currentTemp $tempUnit, feeling like $feelsLike. $highLowPart Winds are coming from the ${weather.windDir} at $windSpeed $windSpeedUnit. Humidity is at ${weather.humidity} percent. $uvPart $alertPart Have a wonderful day!"
}

@Composable
fun WeatherAudioSummaryCard(
    weather: WeatherData,
    useCelsius: Boolean,
    theme: WeatherTheme,
    ttsManager: WeatherTtsManager,
    modifier: Modifier = Modifier
) {
    val summaryText = remember(weather, useCelsius) {
        generateWeatherSummaryText(weather, useCelsius)
    }

    val isDark = theme.primaryTextColor == Color.White
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFDEE3EB)
    val primaryText = theme.primaryTextColor
    val secondaryText = theme.secondaryTextColor
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF006495)

    // Animated pulse scale for speaking indicator
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("weather_audio_summary_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cardBorder),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card Header
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
                            imageVector = Icons.Outlined.RecordVoiceOver,
                            contentDescription = "Audio Summary Icon",
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Audio Weather Brief",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = if (ttsManager.isSpeaking) "Playing voice summary..." else "Listen to daily forecast summary",
                            fontSize = 12.sp,
                            color = secondaryText
                        )
                    }
                }

                // Speed selector chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(1.0f, 1.25f, 1.5f).forEach { rate ->
                        val isSelected = ttsManager.speechRate == rate
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) accentColor else accentColor.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { ttsManager.setSpeed(rate) }
                                .testTag("tts_speed_${rate}x")
                        ) {
                            Text(
                                text = "${rate}x",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else primaryText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Summary Text Transcript Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFFF0F4F8).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = summaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = primaryText.copy(alpha = 0.9f),
                    modifier = Modifier.padding(14.dp)
                )
            }

            // Main Playback Controller Button Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { ttsManager.speak(summaryText) },
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .testTag("play_audio_summary_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ttsManager.isSpeaking) Color(0xFFE53935) else accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (ttsManager.isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (ttsManager.isSpeaking) "Stop Audio Summary" else "Play Audio Summary",
                            modifier = Modifier
                                .size(20.dp)
                                .then(if (ttsManager.isSpeaking) Modifier.scale(pulseScale) else Modifier)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (ttsManager.isSpeaking) "Stop Speaking" else "Listen to Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (ttsManager.isSpeaking) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speaking indicator",
                            tint = accentColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(pulseScale)
                        )
                    }
                }
            }
        }
    }
}
