package br.edu.novaroma.hopemind.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Mesmos tokens do design system do site (frontend/src/styles/tokens.css).
 * Paleta tirada do logo: sálvia (mãos), areia (cérebro) e pêssego (mascote).
 */
@Immutable
data class HopeColors(
    val isDark: Boolean,
    val bg: Color,
    val bgElevated: Color,
    val bgBrand: Color,
    val fill: Color,
    val fillSoft: Color,
    val fillStrong: Color,
    val separator: Color,
    val separatorStrong: Color,
    val label: Color,
    val label2: Color,
    val label3: Color,
    val tint: Color,
    val tintPressed: Color,
    val onTint: Color,
    val tintSoft: Color,
    val tintSoftLabel: Color,
    val sage: Color,
    val sand: Color,
    val sandSoft: Color,
    val sandLabel: Color,
    val peach: Color,
    val peachSoft: Color,
    val peachLabel: Color,
    val danger: Color,
    val dangerSoft: Color,
    val material: Color,
    val washA: Color,
    val washB: Color,
    val washC: Color,
)

val LightHopeColors = HopeColors(
    isDark = false,
    bg = Color(0xFFF5F5F7),
    bgElevated = Color(0xFFFFFFFF),
    bgBrand = Color(0xFFFBF7EF),
    fill = Color(0x1F787880),
    fillSoft = Color(0x12787880),
    fillStrong = Color(0x33787880),
    separator = Color(0x213C3C43),
    separatorStrong = Color(0x383C3C43),
    label = Color(0xFF1D1D1F),
    label2 = Color(0xFF6E6E73),
    label3 = Color(0xFF86868B),
    tint = Color(0xFF3A7733),
    tintPressed = Color(0xFF2C5C27),
    onTint = Color(0xFFFFFFFF),
    tintSoft = Color(0xFFEAF4E4),
    tintSoftLabel = Color(0xFF2F6429),
    sage = Color(0xFF7CC063),
    sand = Color(0xFFE9C987),
    sandSoft = Color(0xFFFBF3E2),
    sandLabel = Color(0xFF8B6420),
    peach = Color(0xFFF2B173),
    peachSoft = Color(0xFFFDEEDF),
    peachLabel = Color(0xFF9A5418),
    danger = Color(0xFFC8342B),
    dangerSoft = Color(0xFFFDECEB),
    // Sem o desfoque do iOS, as barras ficam opacas para o conteúdo não "vazar".
    material = Color(0xFFFFFFFF),
    washA = Color(0x577CC063),
    washB = Color(0x80E9C987),
    washC = Color(0x57F2B173),
)

val DarkHopeColors = HopeColors(
    isDark = true,
    bg = Color(0xFF000000),
    bgElevated = Color(0xFF1C1C1E),
    bgBrand = Color(0xFF121211),
    fill = Color(0x3D767680),
    fillSoft = Color(0x24767680),
    fillStrong = Color(0x5C767680),
    separator = Color(0x8C545458),
    separatorStrong = Color(0xCC545458),
    label = Color(0xFFF5F5F7),
    label2 = Color(0xFFA1A1A6),
    label3 = Color(0xFF7C7C82),
    tint = Color(0xFF8FCB7A),
    tintPressed = Color(0xFF7FB96B),
    onTint = Color(0xFF0B1F07),
    tintSoft = Color(0x298FCB7A),
    tintSoftLabel = Color(0xFFA8D896),
    sage = Color(0xFF7CC063),
    sand = Color(0xFFE9C987),
    sandSoft = Color(0x24E9C987),
    sandLabel = Color(0xFFE8C37E),
    peach = Color(0xFFF2B173),
    peachSoft = Color(0x24F2B173),
    peachLabel = Color(0xFFF4BD8A),
    danger = Color(0xFFFF6961),
    dangerSoft = Color(0x29FF6961),
    material = Color(0xFF1C1C1E),
    washA = Color(0x337CC063),
    washB = Color(0x29E9C987),
    washC = Color(0x29F2B173),
)

/** Gradientes dos avatares — iguais aos do site, escolhidos pelo nome. */
val AvatarGradients = listOf(
    Color(0xFF9FD48C) to Color(0xFF3A7733),
    Color(0xFFF2C98F) to Color(0xFFC47A2C),
    Color(0xFFE9C987) to Color(0xFF9A7428),
    Color(0xFFA7D7C5) to Color(0xFF2F7A64),
    Color(0xFFF5B8A0) to Color(0xFFC0573A),
    Color(0xFFB9C9F0) to Color(0xFF4A64B0),
)
