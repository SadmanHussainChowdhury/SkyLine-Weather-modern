package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MidnightBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B70),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1E1B4B),
    tertiary = Color(0xFFC084FC),
    onTertiary = Color(0xFF3B0764),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131C2E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155)
)

private val MidnightBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    tertiary = Color(0xFFA855F7),
    onTertiary = Color.White,
    background = Color(0xFFF0F6FC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

private val SunsetGoldDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFF97316),
    onSecondary = Color(0xFF431407),
    tertiary = Color(0xFFEF4444),
    onTertiary = Color(0xFF450A0A),
    background = Color(0xFF180E0A),
    onBackground = Color(0xFFFFFBEB),
    surface = Color(0xFF261810),
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF3D271A),
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0xFF5A3A26)
)

private val SunsetGoldLightColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFFEA580C),
    onSecondary = Color.White,
    tertiary = Color(0xFFDC2626),
    onTertiary = Color.White,
    background = Color(0xFFFFFBEB),
    onBackground = Color(0xFF291E0A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF291E0A),
    surfaceVariant = Color(0xFFFEF3C7),
    onSurfaceVariant = Color(0xFF92400E),
    outline = Color(0xFFFDE68A)
)

private val MonochromeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0),
    onPrimary = Color(0xFF0F0F10),
    primaryContainer = Color(0xFF27272A),
    onPrimaryContainer = Color(0xFFF4F4F5),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF18181B),
    tertiary = Color(0xFFD4D4D8),
    onTertiary = Color(0xFF18181B),
    background = Color(0xFF0F0F10),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF18181B),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFD4D4D8),
    outline = Color(0xFF3F3F46)
)

private val MonochromeLightColorScheme = lightColorScheme(
    primary = Color(0xFF18181B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E4E7),
    onPrimaryContainer = Color(0xFF18181B),
    secondary = Color(0xFF52525B),
    onSecondary = Color.White,
    tertiary = Color(0xFF3F3F46),
    onTertiary = Color.White,
    background = Color(0xFFF4F4F5),
    onBackground = Color(0xFF09090B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFE4E4E7),
    onSurfaceVariant = Color(0xFF3F3F46),
    outline = Color(0xFFD4D4D8)
)

private val EmeraldAuroraDarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF032830),
    background = Color(0xFF061412),
    onBackground = Color(0xFFECFDF5),
    surface = Color(0xFF0D2823),
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF133E36),
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0xFF1E5C50)
)

private val EmeraldAuroraLightColorScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color.White,
    primaryContainer = Color(0xDFD1FAE5),
    onPrimaryContainer = Color(0xFF065F46),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = Color(0xFFECFDF5),
    onBackground = Color(0xFF022C22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF022C22),
    surfaceVariant = Color(0xFFD1FAE5),
    onSurfaceVariant = Color(0xFF047857),
    outline = Color(0xFFA7F3D0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B70),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF90A4AE),
    onSecondary = Color(0xFF1E293B),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color(0xFF3E2703),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006495),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    tertiary = Color(0xFFFFB300),
    onTertiary = Color.White,
    background = Color(0xFFF0F4F8),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0F1FF),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFFDEE3EB)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "system", // "system", "light", "dark"
    aestheticTheme: String = "midnight_blue", // "midnight_blue", "sunset_gold", "monochrome", "emerald_aurora", "skyline_classic"
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (aestheticTheme) {
        "sunset_gold" -> if (darkTheme) SunsetGoldDarkColorScheme else SunsetGoldLightColorScheme
        "monochrome" -> if (darkTheme) MonochromeDarkColorScheme else MonochromeLightColorScheme
        "emerald_aurora" -> if (darkTheme) EmeraldAuroraDarkColorScheme else EmeraldAuroraLightColorScheme
        "skyline_classic" -> if (darkTheme) DarkColorScheme else LightColorScheme
        else -> if (darkTheme) MidnightBlueDarkColorScheme else MidnightBlueLightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
