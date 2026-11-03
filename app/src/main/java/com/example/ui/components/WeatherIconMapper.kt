package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

fun getWeatherIcon(conditionCode: Int): ImageVector {
    return when (conditionCode) {
        1000 -> Icons.Outlined.WbSunny // Sunny, Clear
        1003, 1006, 1009 -> Icons.Outlined.WbCloudy // Partly cloudy, Cloudy, Overcast
        1030, 1135, 1147 -> Icons.Outlined.Air // Mist, Fog, Freezing fog
        1063, 1150, 1153, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243 -> Icons.Outlined.Umbrella // Rain, Showers
        1087, 1273, 1276, 1279, 1282 -> Icons.Outlined.Thunderstorm // Thundery, Stormy
        1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225, 1255, 1258 -> Icons.Outlined.AcUnit // Snow, Blizzard
        else -> Icons.Outlined.Grain // Drizzle, Sleet, Hail
    }
}
