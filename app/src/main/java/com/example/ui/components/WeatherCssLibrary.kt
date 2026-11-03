package com.example.ui.components

import android.util.Base64

object WeatherCssLibrary {

    fun getWeatherHtml(conditionCode: Int): String {
        val overlayType = getOverlayType(conditionCode)
        val styleBuilder = StringBuilder()
        val bodyBuilder = StringBuilder()

        // Common HTML skeleton structure
        styleBuilder.append("""
            html, body {
                margin: 0;
                padding: 0;
                width: 100vw;
                height: 100vh;
                overflow: hidden;
                background: transparent;
                pointer-events: none;
                user-select: none;
                -webkit-user-select: none;
            }
            .container {
                position: relative;
                width: 100%;
                height: 100%;
                overflow: hidden;
            }
        """.trimIndent())

        when (overlayType) {
            OverlayType.SUNNY -> generateSunny(styleBuilder, bodyBuilder)
            OverlayType.CLOUDY -> generateCloudy(styleBuilder, bodyBuilder)
            OverlayType.RAINY -> generateRainy(styleBuilder, bodyBuilder)
            OverlayType.SNOWY -> generateSnowy(styleBuilder, bodyBuilder)
            OverlayType.THUNDERSTORM -> generateThunderstorm(styleBuilder, bodyBuilder)
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                <style>
                    ${styleBuilder}
                </style>
            </head>
            <body>
                <div class="container">
                    ${bodyBuilder}
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun getWeatherHtmlBase64(conditionCode: Int): String {
        val html = getWeatherHtml(conditionCode)
        return Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun getOverlayType(code: Int): OverlayType {
        return when (code) {
            1000 -> OverlayType.SUNNY
            1003, 1006, 1009 -> OverlayType.CLOUDY
            1063, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243 -> OverlayType.RAINY
            1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225, 1255 -> OverlayType.SNOWY
            1087, 1273, 1276, 1279, 1282 -> OverlayType.THUNDERSTORM
            else -> OverlayType.CLOUDY // Fallback
        }
    }

    private fun generateSunny(style: StringBuilder, body: StringBuilder) {
        style.append("""
            .sun-core {
                position: absolute;
                top: 15%;
                right: 15%;
                width: 130px;
                height: 130px;
                border-radius: 50%;
                background: radial-gradient(circle, rgba(255, 235, 59, 1) 0%, rgba(255, 224, 130, 0.4) 45%, rgba(255, 224, 130, 0) 70%);
                animation: pulse-sun 6s ease-in-out infinite alternate;
                z-index: 2;
            }
            .rays-container {
                position: absolute;
                top: 15%;
                right: 15%;
                width: 130px;
                height: 130px;
                animation: rotate-rays 60s linear infinite;
                z-index: 1;
            }
            .ray {
                position: absolute;
                top: -35px;
                left: 64px;
                width: 2px;
                height: 200px;
                background: linear-gradient(transparent, rgba(255, 235, 59, 0.2) 50%, transparent);
            }
            .dust {
                position: absolute;
                background: rgba(255, 235, 59, 0.35);
                border-radius: 50%;
                animation: float-dust linear infinite;
            }
            @keyframes pulse-sun {
                0% { transform: scale(0.9); opacity: 0.7; }
                100% { transform: scale(1.15); opacity: 1.0; }
            }
            @keyframes rotate-rays {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
            @keyframes float-dust {
                0% { transform: translateY(100vh) translateX(0) scale(1); opacity: 0; }
                10% { opacity: 0.6; }
                90% { opacity: 0.6; }
                100% { transform: translateY(-50px) translateX(50px) scale(1.5); opacity: 0; }
            }
        """.trimIndent())

        // Add Sun and Rays
        body.append("""
            <div class="sun-core"></div>
            <div class="rays-container">
        """.trimIndent())
        for (i in 0 until 12) {
            val angle = i * 30
            body.append("<div class=\"ray\" style=\"transform: rotate(${angle}deg);\"></div>\n")
        }
        body.append("</div>\n")

        // Add floaty dust particles
        for (i in 0 until 15) {
            val left = (5..95).random()
            val size = (3..8).random()
            val delay = -(0..10000).random() / 1000f
            val duration = 8.0f + (0..6000).random() / 1000f
            body.append("<div class=\"dust\" style=\"left: ${left}%; width: ${size}px; height: ${size}px; animation-delay: ${delay}s; animation-duration: ${duration}s;\"></div>\n")
        }
    }

    private fun generateCloudy(style: StringBuilder, body: StringBuilder) {
        style.append("""
            .cloud {
                position: absolute;
                width: 140px;
                height: 45px;
                background: rgba(255, 255, 255, 0.45);
                border-radius: 50px;
                filter: blur(2px);
                animation: drift-cloud linear infinite, float-cloud ease-in-out infinite alternate;
            }
            .cloud-b1 {
                position: absolute;
                top: -25px;
                left: 20px;
                width: 55px;
                height: 55px;
                background: inherit;
                border-radius: 50%;
            }
            .cloud-b2 {
                position: absolute;
                top: -35px;
                left: 55px;
                width: 65px;
                height: 65px;
                background: inherit;
                border-radius: 50%;
            }
            @keyframes drift-cloud {
                0% { left: -180px; }
                100% { left: 100vw; }
            }
            @keyframes float-cloud {
                0% { margin-top: 0px; }
                100% { margin-top: 15px; }
            }
        """.trimIndent())

        for (i in 0 until 6) {
            val top = 5 + i * 8 + (0..4).random()
            val delay = -(0..40000).random() / 1000f
            val duration = 25.0f + (0..20000).random() / 1000f
            val floatDuration = 4.0f + (0..3000).random() / 1000f
            val scale = 0.6f + (0..6).random() / 10f
            val opacity = 0.25f + (0..3).random() / 10f
            body.append("""
                <div class="cloud" style="top: ${top}%; animation-delay: ${delay}s, 0s; animation-duration: ${duration}s, ${floatDuration}s; transform: scale($scale); opacity: $opacity;">
                    <div class="cloud-b1"></div>
                    <div class="cloud-b2"></div>
                </div>
            """.trimIndent())
        }
    }

    private fun generateRainy(style: StringBuilder, body: StringBuilder) {
        style.append("""
            .drop {
                position: absolute;
                background: linear-gradient(transparent, rgba(0, 100, 149, 0.45));
                width: 1.5px;
                height: 45px;
                animation: fall-rain linear infinite;
            }
            @keyframes fall-rain {
                0% { transform: translateY(-60px) rotate(-12deg); }
                100% { transform: translateY(100vh) rotate(-12deg); }
            }
        """.trimIndent())

        for (i in 0 until 65) {
            val left = (0..100).random()
            val delay = -(0..2000).random() / 1000f
            val duration = 0.7f + (0..800).random() / 1000f
            val height = 35 + (0..25).random()
            val opacity = 0.3f + (0..5).random() / 10f
            body.append("<div class=\"drop\" style=\"left: ${left}%; animation-delay: ${delay}s; animation-duration: ${duration}s; height: ${height}px; opacity: $opacity;\"></div>\n")
        }
    }

    private fun generateSnowy(style: StringBuilder, body: StringBuilder) {
        style.append("""
            .flake {
                position: absolute;
                background: white;
                border-radius: 50%;
                animation: fall-snow linear infinite, sway-snow ease-in-out infinite alternate;
            }
            @keyframes fall-snow {
                0% { transform: translateY(-15px); }
                100% { transform: translateY(100vh); }
            }
            @keyframes sway-snow {
                0% { margin-left: -25px; }
                100% { margin-left: 25px; }
            }
        """.trimIndent())

        for (i in 0 until 50) {
            val left = (0..100).random()
            val delay = -(0..12000).random() / 1000f
            val duration = 3.5f + (0..4000).random() / 1000f
            val swayDuration = 2.0f + (0..2500).random() / 1000f
            val size = 3 + (0..6).random()
            val opacity = 0.4f + (0..5).random() / 10f
            body.append("<div class=\"flake\" style=\"left: ${left}%; width: ${size}px; height: ${size}px; animation-name: fall-snow, sway-snow; animation-delay: ${delay}s, 0s; animation-duration: ${duration}s, ${swayDuration}s; opacity: $opacity;\"></div>\n")
        }
    }

    private fun generateThunderstorm(style: StringBuilder, body: StringBuilder) {
        // Lightning flashes and rain falling!
        style.append("""
            .lightning {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: white;
                opacity: 0;
                animation: lightning-flash 9s infinite;
                z-index: 1;
            }
            .drop {
                position: absolute;
                background: linear-gradient(transparent, rgba(144, 164, 174, 0.5));
                width: 1.8px;
                height: 50px;
                animation: fall-rain linear infinite;
                z-index: 2;
            }
            @keyframes fall-rain {
                0% { transform: translateY(-60px) rotate(-15deg); }
                100% { transform: translateY(100vh) rotate(-15deg); }
            }
            @keyframes lightning-flash {
                0%, 93%, 95%, 97%, 100% { opacity: 0; }
                94% { opacity: 0.65; }
                96% { opacity: 0.85; }
            }
        """.trimIndent())

        body.append("<div class=\"lightning\"></div>\n")

        for (i in 0 until 75) {
            val left = (0..100).random()
            val delay = -(0..1800).random() / 1000f
            val duration = 0.6f + (0..700).random() / 1000f
            val height = 40 + (0..25).random()
            val opacity = 0.35f + (0..5).random() / 10f
            body.append("<div class=\"drop\" style=\"left: ${left}%; animation-delay: ${delay}s; animation-duration: ${duration}s; height: ${height}px; opacity: $opacity;\"></div>\n")
        }
    }

    enum class OverlayType {
        SUNNY, CLOUDY, RAINY, SNOWY, THUNDERSTORM
    }
}
