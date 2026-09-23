package br.edu.novaroma.hopemind.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Tema do HopeMind.
 * - As cores "Apple-like" ficam em [HopeColors] (acessadas via HopeTheme.colors).
 * - O MaterialTheme recebe as mesmas cores para que componentes Material 3 (campos,
 *   diálogos, bottom sheets) fiquem coerentes com o resto do app.
 */
enum class ThemeMode { AUTO, LIGHT, DARK }

private val LocalHopeColors = staticCompositionLocalOf { LightHopeColors }
private val LocalHopeType = staticCompositionLocalOf { HopeType() }

object HopeTheme {
    val colors: HopeColors @Composable get() = LocalHopeColors.current
    val type: HopeType @Composable get() = LocalHopeType.current
}

object Radius {
    val sm = 10.dp
    val md = 12.dp
    val lg = 18.dp
    val xl = 24.dp
    val xxl = 32.dp
}

@Composable
fun HopeMindTheme(mode: ThemeMode = ThemeMode.AUTO, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val c = if (dark) DarkHopeColors else LightHopeColors
    val type = HopeType()

    val scheme = if (dark) {
        darkColorScheme(
            primary = c.tint, onPrimary = c.onTint, primaryContainer = c.tintSoft, onPrimaryContainer = c.tintSoftLabel,
            secondary = c.sand, tertiary = c.peach, background = c.bg, onBackground = c.label,
            surface = c.bgElevated, onSurface = c.label, surfaceVariant = c.bgElevated, onSurfaceVariant = c.label2,
            surfaceContainerLow = c.bgElevated, surfaceContainer = c.bgElevated, surfaceContainerHigh = c.bgElevated,
            outline = c.separatorStrong, outlineVariant = c.separator, error = c.danger,
        )
    } else {
        lightColorScheme(
            primary = c.tint, onPrimary = c.onTint, primaryContainer = c.tintSoft, onPrimaryContainer = c.tintSoftLabel,
            secondary = c.sand, tertiary = c.peach, background = c.bg, onBackground = c.label,
            surface = c.bgElevated, onSurface = c.label, surfaceVariant = c.bgElevated, onSurfaceVariant = c.label2,
            surfaceContainerLow = c.bgElevated, surfaceContainer = c.bgElevated, surfaceContainerHigh = c.bgElevated,
            outline = c.separatorStrong, outlineVariant = c.separator, error = c.danger,
        )
    }

    CompositionLocalProvider(LocalHopeColors provides c, LocalHopeType provides type) {
        MaterialTheme(
            colorScheme = scheme,
            typography = materialTypography(type),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(Radius.sm),
                medium = RoundedCornerShape(Radius.md),
                large = RoundedCornerShape(Radius.lg),
                extraLarge = RoundedCornerShape(Radius.xl),
            ),
            content = content,
        )
    }
}
