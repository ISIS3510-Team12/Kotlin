package com.team12kotlin.juggle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.team12kotlin.juggle.R

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val SpaceGroteskFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont("Space Grotesk"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    )
)

private val RubikFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Rubik"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("Rubik"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = GoogleFont("Rubik"),
        fontProvider = googleFontProvider,
        weight = FontWeight.Bold
    )
)

private val baseline = Typography()
val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = SpaceGroteskFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = SpaceGroteskFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = SpaceGroteskFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = SpaceGroteskFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = SpaceGroteskFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = SpaceGroteskFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = RubikFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = RubikFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = RubikFamily)
)
