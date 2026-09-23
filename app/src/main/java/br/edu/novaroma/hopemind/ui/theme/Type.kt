package br.edu.novaroma.hopemind.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Escala tipográfica da Apple HIG, a mesma usada no site. */
@Immutable
data class HopeType(
    val largeTitle: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.028).em),
    val title1: TextStyle = TextStyle(fontSize = 28.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.024).em),
    val title2: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    val title3: TextStyle = TextStyle(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.018).em),
    val headline: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.016).em),
    val body: TextStyle = TextStyle(fontSize = 16.sp, lineHeight = 23.sp, letterSpacing = (-0.011).em),
    val callout: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, letterSpacing = (-0.01).em),
    val subhead: TextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    val footnote: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    val caption: TextStyle = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    val eyebrow: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em),
)

internal fun materialTypography(t: HopeType) = Typography(
    displaySmall = t.largeTitle,
    headlineLarge = t.largeTitle,
    headlineMedium = t.title1,
    headlineSmall = t.title2,
    titleLarge = t.title3,
    titleMedium = t.headline,
    titleSmall = t.subhead.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = t.body,
    bodyMedium = t.callout,
    bodySmall = t.footnote,
    labelLarge = t.callout.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = t.footnote.copy(fontWeight = FontWeight.Medium),
    labelSmall = t.caption,
)
