package com.personal.financeapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Serif display — used for large numbers and editorial titles
    displayLarge  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 48.sp, letterSpacing = (-0.5).sp, lineHeight = 52.sp),
    displayMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 40.sp, letterSpacing = (-0.5).sp, lineHeight = 44.sp),
    displaySmall  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 30.sp, letterSpacing = (-0.3).sp, lineHeight = 36.sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 26.sp, letterSpacing = (-0.2).sp, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 22.sp, letterSpacing = (-0.1).sp, lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 24.sp),
    // Sans-serif UI text
    titleLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 15.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 13.sp),
    bodyLarge   = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium  = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall   = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp),
    // Eyebrow labels: small all-caps with wide tracking
    labelSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 10.sp, letterSpacing = 1.2.sp),
)
