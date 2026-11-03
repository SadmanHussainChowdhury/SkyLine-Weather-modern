# Skyline Weather 🌤️

**Skyline Weather** is a modern, feature-rich, high-fidelity Android weather application engineered with **Kotlin**, **Jetpack Compose (Material 3)**, and **Clean MVVM Architecture**. It combines multi-source real-time meteorological data, generative AI insights powered by Gemini, interactive Canvas visualizations, and local data persistence to deliver a responsive weather monitoring experience.

---

## 🌟 Key Features & Highlights

### 1. 🎨 Dynamic Weather Canvas & Motion Architecture
- **60 FPS Canvas Particle System**: Real-time rendering of falling raindrops, floating snowflakes, moving cloud masses, and ambient solar halos reacting to weather conditions.
- **Animated Weather Vector Illustrations**: Multi-layered, condition-adaptive visual illustrations for Sunny, Overcast, Rainy, Snowy, and Thunderstorm states with dynamic color halos.
- **Material 3 Dynamic Theming**: Adaptive color palettes that transition smoothly based on active weather, time of day (day/night cycles), and atmospheric intensity.

### 2. 🌍 Weather Data & Multi-Source Engine
- **Live Meteorological Sync**: Integrates real-time weather forecasts, hourly timelines, 7-day outlooks, wind metrics, humidity levels, and atmospheric pressure.
- **Air Quality & Pollen Insights**: Real-time AQI monitoring (PM2.5, PM10, NO2, O3) and detailed pollen risk tracking (Tree, Grass, Weed, Ragweed pollen counts).
- **Sun & Moon Astronomy**: Interactive solar arc timeline tracking sunrise, sunset, solar noon, daylight progression, and complete lunar phase calendars.
- **UV Index & Thermal Comfort Cards**: Real-time UV exposure risk meters with sun protection recommendations and clothing advisory metrics.

### 3. 🤖 AI Weather Assistant & Smart Insights
- **Gemini AI Weather Summaries**: Generates intelligent natural-language daily weather summaries and actionable activity recommendations.
- **Audio Weather Summaries**: Interactive audio playback card for hearing daily weather briefings on the go.
- **Smart Micro-Climate Alerts**: Automatic detection of sudden temperature drops, high wind gusts, rain probability spikes, or severe UV levels.

### 4. 🗺️ Interactive Maps & Historical Climate Trends
- **Live Interactive Weather Map**: Interactive radar map support with selectable layers for Precipitation Radar, Temperature Heatmaps, Wind Speed, and Cloud Coverage.
- **Climate Trend Charts**: Bezier curve canvas charts visualizing temperature trends, monthly climate averages, and historical comparisons against multi-year baselines.

### 5. 🏠 Home Screen Widgets & Local Sync
- **Jetpack Glance Android Widgets**: Native home screen widgets displaying current temperature, weather icons, high/low ranges, and quick location updates directly from the launcher.
- **Saved Locations & Favorites**: Add, organize, and quickly jump between favorite cities across the globe.
- **Offline Room Caching**: Fully functional offline mode using SQLite Room database caching to provide instant app launch and zero-latency location switching.

---

## 🏗️ Architecture & Technical Stack

```text
com.example/
├── data/
│   ├── local/              # Room Database, DataStore Preferences & Entities
│   ├── remote/             # Retrofit API Services (WeatherAPI, Open-Meteo, Gemini)
│   ├── repository/         # Data Repositories & Data Models
│   ├── location/           # Geofencing & Geocoding Services
│   └── notification/       # FCM & Weather Alert Notification Managers
├── di/                     # Dependency Injection Container (AppContainer)
├── service/                # Background Services (Firebase Messaging)
├── ui/
│   ├── components/         # Reusable Jetpack Compose Design System & Cards
│   ├── hooks/              # Browser & Device Geolocation Handlers
│   ├── screens/            # Primary Navigation Screens
│   ├── theme/              # Material Design 3 Palette, Typography & Shapes
│   └── viewmodel/          # WeatherViewModel & App State Flow
└── widget/                 # Jetpack Glance Home Screen Widget Implementations
```

### Technology Matrix

| Layer | Component | Technology / Library |
| :--- | :--- | :--- |
| **Language** | Core | Kotlin 1.9+ |
| **UI Framework** | Declarative UI | Jetpack Compose (Material 3) |
| **Architecture** | Pattern | MVVM (Model-View-ViewModel) + Clean Architecture |
| **Networking** | REST Clients | Retrofit 2 + OkHttp 4 + Kotlinx Serialization |
| **Local Storage** | Database & Prefs | Room Database (KSP) + DataStore Preferences |
| **AI Integration** | Generative AI | Gemini REST API (`generativelanguage.googleapis.com`) |
| **Widgets** | Home Screen | Jetpack Glance Compose |
| **Graphics** | Canvas & Animation | Compose Canvas + Jetpack Graphics Pipeline |

---

## 🌐 API Integrations & Fallback Strategy

The application employs a **resilient multi-provider fallback architecture**:

1. **Open-Meteo API (Primary No-Key Provider)**:
   - *Forecast API*: `https://api.open-meteo.com/v1/forecast`
   - *Geocoding API*: `https://geocoding-api.open-meteo.com/v1/search`
   - *Air Quality & Pollen API*: `https://air-quality-api.open-meteo.com/v1/air-quality`
2. **WeatherAPI.com (Secondary Keyed Provider)**:
   - Supports live synced global forecasts when an API key is configured in the app preferences.
3. **Google Gemini API (AI Insights Engine)**:
   - Generates contextual weather summaries, clothing advice, and trip suitability scores.

---

## 🛠️ Setup & Installation

### Prerequisites
- Android Studio Jellyfish / Ladybug or newer
- JDK 17
- Android SDK (Target SDK 34+, Min SDK 24+)

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/skyline-weather.git
   cd skyline-weather
   ```

2. **Open in Android Studio**:
   - Open Android Studio and choose **Open an Existing Project**.
   - Select the root directory of `skyline-weather`.

3. **Build and Run**:
   - Select your target device or emulator.
   - Click **Run 'app'** (`Shift + F10`) or execute from command line:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📱 Screen Guide & Navigation

- **Home Screen (`HomeScreen.kt`)**: Displays current condition hero illustration, quick metrics, Gemini AI audio/text summary, 24-hour timeline, 7-day forecast, air quality, pollen counts, and interactive weather cards.
- **Favorites Screen (`FavoritesScreen.kt`)**: Manage pinned global cities, search new locations, and view quick weather chips.
- **Live Radar Map (`LiveWeatherMapScreen.kt`)**: Interactive weather map with radar controls and metric layers.
- **Smart Widgets (`SmartWidgetsScreen.kt`)**: Preview and configure Jetpack Glance home screen widgets.
- **Settings Screen (`SettingsScreen.kt`)**: Configure temperature units (°C / °F), set API keys, manage backup/restore device migration, and adjust notification alerts.

---

## 📄 License & Credits

Built with ❤️ using **Kotlin** & **Jetpack Compose**.
- Weather Data provided by **Open-Meteo** and **WeatherAPI**.
- Generative AI capabilities powered by **Google Gemini API**.
