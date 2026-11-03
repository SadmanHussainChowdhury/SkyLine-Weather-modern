package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.repository.WeatherData
import com.example.ui.components.WeatherTheme
import com.example.ui.components.WindDashboardCard
import com.example.ui.components.UvDashboardCard
import com.example.ui.components.MoonPhaseCard
import com.example.ui.components.SunCycleCard
import com.example.ui.components.DailyForecastList
import com.example.ui.components.HourlyForecastRow
import com.example.data.repository.DailyForecastItem
import com.example.data.repository.HourlyForecastItem
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class DashboardComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockWeatherData = WeatherData(
        city = "London",
        tempC = 18.0,
        tempF = 64.4,
        conditionText = "Partly Cloudy",
        conditionCode = 1003,
        humidity = 65,
        windKph = 15.0,
        windMph = 9.3,
        windDegree = 240,
        windDir = "WSW",
        pressureMb = 1015.0,
        uv = 4.5,
        sunrise = "05:15 AM",
        sunset = "08:45 PM",
        feelsLikeC = 17.5,
        feelsLikeF = 63.5,
        aqi = 2,
        alertHeadline = null,
        alertDesc = null,
        hourlyForecast = emptyList(),
        dailyForecast = emptyList()
    )

    @Test
    fun testWindDashboardCard_rendersCorrectly() {
        composeTestRule.setContent {
            MyApplicationTheme {
                WindDashboardCard(
                    weather = mockWeatherData,
                    useCelsius = true,
                    theme = WeatherTheme.Cloudy
                )
            }
        }

        // Verify the main card and key metrics are displayed correctly
        composeTestRule.onNodeWithTag("wind_dashboard_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("animated_compass_dial").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wind_speed_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("15.0 km/h").assertIsDisplayed()
        composeTestRule.onNodeWithText("WSW (240°)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gentle Breeze").assertIsDisplayed()
    }

    @Test
    fun testUvDashboardCard_rendersCorrectly() {
        composeTestRule.setContent {
            MyApplicationTheme {
                UvDashboardCard(
                    weather = mockWeatherData,
                    theme = WeatherTheme.Sunny
                )
            }
        }

        // Verify UV details and categories are rendered correctly
        composeTestRule.onNodeWithTag("uv_dashboard_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("animated_uv_gauge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("uv_index_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("4.5").assertIsDisplayed()
        composeTestRule.onNodeWithText("MODERATE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apply SPF 15+ sunscreen. Seek shade around midday. Wear protective clothing & a hat.").assertIsDisplayed()
    }

    @Test
    fun testMoonPhaseCard_rendersCorrectly() {
        val testLunarData = mockWeatherData.copy(
            moonPhase = "Waxing Gibbous",
            moonIllumination = 72,
            moonrise = "06:30 PM",
            moonset = "04:15 AM"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                MoonPhaseCard(
                    weather = testLunarData,
                    theme = WeatherTheme.Thunderstorm
                )
            }
        }

        // Verify key text and elements are displayed
        composeTestRule.onNodeWithTag("moon_phase_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("moon_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("moon_phase_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Waxing Gibbous").assertIsDisplayed()
        composeTestRule.onNodeWithText("72% Illumination").assertIsDisplayed()
        composeTestRule.onNodeWithText("06:30 PM").assertIsDisplayed()
        composeTestRule.onNodeWithText("04:15 AM").assertIsDisplayed()
    }

    @Test
    fun testSunCycleCard_rendersCorrectly() {
        val testSolarData = mockWeatherData.copy(
            sunrise = "05:15 AM",
            sunset = "08:45 PM"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                SunCycleCard(
                    weather = testSolarData,
                    theme = WeatherTheme.Sunny
                )
            }
        }

        // Verify key text and elements of the sun cycle component exist in hierarchy
        composeTestRule.onNodeWithTag("sun_cycle_card").assertExists()
        composeTestRule.onNodeWithTag("sun_cycle_canvas").assertExists()
        composeTestRule.onNodeWithTag("sunrise_time_text").assertExists()
        composeTestRule.onNodeWithTag("sunset_time_text").assertExists()
        composeTestRule.onNodeWithTag("sun_relative_desc_text").assertExists()
        
        composeTestRule.onNodeWithText("05:15 AM").assertExists()
        composeTestRule.onNodeWithText("08:45 PM").assertExists()
    }

    @Test
    fun testDailyForecastList_rendersCorrectly() {
        val testDailyForecast = listOf(
            DailyForecastItem(
                date = "Today",
                maxTempC = 22.0,
                maxTempF = 71.6,
                minTempC = 12.0,
                minTempF = 53.6,
                conditionText = "Sunny",
                conditionCode = 1000,
                uv = 5.0
            ),
            DailyForecastItem(
                date = "Monday",
                maxTempC = 25.0,
                maxTempF = 77.0,
                minTempC = 14.0,
                minTempF = 57.2,
                conditionText = "Partly Cloudy",
                conditionCode = 1003,
                uv = 6.0
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                DailyForecastList(
                    dailyList = testDailyForecast,
                    useCelsius = true,
                    theme = WeatherTheme.Sunny,
                    currentTemp = 18.0
                )
            }
        }

        // Verify key text and elements are displayed
        composeTestRule.onNodeWithTag("daily_forecast_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_forecast_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_forecast_item_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monday").assertIsDisplayed()
        
        // Temperatures
        composeTestRule.onNodeWithText("12°").assertIsDisplayed()
        composeTestRule.onNodeWithText("22°").assertIsDisplayed()
        composeTestRule.onNodeWithText("14°").assertIsDisplayed()
        composeTestRule.onNodeWithText("25°").assertIsDisplayed()
    }

    @Test
    fun testHourlyForecastRow_rendersCorrectly() {
        val testHourlyList = listOf(
            HourlyForecastItem(
                time = "10:00 AM",
                tempC = 19.0,
                tempF = 66.2,
                conditionText = "Sunny",
                conditionCode = 1000,
                isDay = true,
                epoch = 1600000000
            ),
            HourlyForecastItem(
                time = "11:00 AM",
                tempC = 21.0,
                tempF = 69.8,
                conditionText = "Partly Cloudy",
                conditionCode = 1003,
                isDay = true,
                epoch = 1600003600
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                HourlyForecastRow(
                    hourlyList = testHourlyList,
                    useCelsius = true,
                    theme = WeatherTheme.Sunny
                )
            }
        }

        // Verify row tag and items
        composeTestRule.onNodeWithTag("hourly_forecast_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hourly_forecast_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hourly_forecast_item_1").assertIsDisplayed()

        // Verify current hour label "Now" for first item and time for second
        composeTestRule.onNodeWithText("Now").assertIsDisplayed()
        composeTestRule.onNodeWithText("11:00 AM").assertIsDisplayed()

        // Verify temperature readings
        composeTestRule.onNodeWithText("19°").assertIsDisplayed()
        composeTestRule.onNodeWithText("21°").assertIsDisplayed()
    }
}
