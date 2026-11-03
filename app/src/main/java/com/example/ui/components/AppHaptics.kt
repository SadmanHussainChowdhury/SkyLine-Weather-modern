package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

class AppHaptics(
    private val view: View,
    private val context: Context,
    private val composeHaptics: HapticFeedback
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Triggered during Pull-To-Refresh gesture activation or completion.
     * Provides a distinct, tactile pull-to-refresh haptic response.
     */
    fun performPullToRefresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            try {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator?.vibrate(effect)
            } catch (e: Exception) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasVibrator() == true) {
            val waveform = longArrayOf(0, 30, 40, 30)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(waveform, amplitudes, -1))
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**
     * Triggered when toggling forecast modes (e.g. Summary vs Detailed list, 24h vs 7D, C vs F).
     */
    fun performToggleMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            try {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator?.vibrate(effect)
            } catch (e: Exception) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    /**
     * Triggered on key button taps and chip selections for tactile engagement.
     */
    fun performKeyTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            try {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                vibrator?.vibrate(effect)
            } catch (e: Exception) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

    /**
     * Triggered when a major operation completes (e.g. data refreshed, bookmark added).
     */
    fun performSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            try {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                vibrator?.vibrate(effect)
            } catch (e: Exception) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun performError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            try {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator?.vibrate(effect)
            } catch (e: Exception) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val view = LocalView.current
    val context = LocalContext.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(view, context, composeHaptics) {
        AppHaptics(view, context, composeHaptics)
    }
}
