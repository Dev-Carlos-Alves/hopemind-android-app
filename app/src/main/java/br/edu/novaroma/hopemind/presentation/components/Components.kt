package br.edu.novaroma.hopemind.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.ui.theme.AvatarGradients
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius

/* ───────────── Botões (pílula, como no site) ───────────── */

enum class ButtonVariant { FILLED, TINTED, GRAY, PLAIN, DANGER }
enum class ButtonSize { SM, MD, LG }

@Composable
fun HopeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.FILLED,
    size: ButtonSize = ButtonSize.MD,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val c = HopeTheme.colors
    val (bg, fg) = when (variant) {
        ButtonVariant.FILLED -> c.tint to c.onTint
        ButtonVariant.TINTED -> c.tintSoft to c.tintSoftLabel
        ButtonVariant.GRAY -> c.fill to c.label
        ButtonVariant.PLAIN -> Color.Transparent to c.tint
        ButtonVariant.DANGER -> c.dangerSoft to c.danger
    }
    val height = when (size) { ButtonSize.SM -> 36.dp; ButtonSize.MD -> 46.dp; ButtonSize.LG -> 54.dp }
    val style = when (size) { ButtonSize.LG -> HopeTheme.type.headline; ButtonSize.SM -> HopeTheme.type.subhead; else -> HopeTheme.type.callout }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "press")
    val active = enabled && !loading

    Row(
        modifier = modifier
            .scale(scale)
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(if (active || variant == ButtonVariant.PLAIN) bg else bg.copy(alpha = bg.alpha * 0.45f))
            .clickable(interactionSource = interaction, indication = null, enabled = active, role = Role.Button, onClick = onClick)
            .padding(horizontal = if (size == ButtonSize.SM) 16.dp else 22.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = if (active) fg else fg.copy(alpha = 0.5f)
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = fg, strokeWidth = 2.dp)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Text(text, style = style.copy(fontWeight = FontWeight.SemiBold), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!loading && trailingIcon != null) Icon(trailingIcon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

/* ───────────── Campos ───────────── */

@Composable
fun HopeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    hint: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailing: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
) {
    val c = HopeTheme.colors
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            isError = error != null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            trailingIcon = trailing,
            shape = RoundedCornerShape(Radius.md),
            textStyle = HopeTheme.type.headline.copy(fontWeight = FontWeight.Normal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.tint,
                unfocusedBorderColor = c.separatorStrong,
                focusedLabelColor = c.tint,
                unfocusedLabelColor = c.label2,
                cursorColor = c.tint,
                focusedContainerColor = c.bgElevated,
                unfocusedContainerColor = c.bgElevated,
                errorBorderColor = c.danger,
                errorLabelColor = c.danger,
            ),
        )
        val below = error ?: hint
        if (below != null) {
            Text(
                below,
                style = HopeTheme.type.footnote,
                color = if (error != null) c.danger else c.label2,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )
        }
    }
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, hint: String? = null) {
    var visible by rememberSaveable { mutableStateOf(false) }
    HopeTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        hint = hint,
        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = HopeTheme.colors.label2,
                )
            }
        },
    )
}

/* ───────────── Segmented control ───────────── */

@Composable
fun SegmentedControl(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, large: Boolean = false) {
    val c = HopeTheme.colors
    val height = if (large) 42.dp else 34.dp
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(10.dp))
            .background(c.fill)
            .padding(2.dp),
    ) {
        val itemWidth = maxWidth / options.size
        val offset by androidx.compose.animation.core.animateDpAsState(itemWidth * selected, label = "thumb")
        Box(
            Modifier
                .offset(x = offset)
                .width(itemWidth)
                .fillMaxHeight()
                .shadow(3.dp, RoundedCornerShape(8.dp))
                .background(if (c.isDark) Color(0xFF636366) else Color.White, RoundedCornerShape(8.dp)),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, label ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.RadioButton) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = (if (large) HopeTheme.type.subhead else HopeTheme.type.footnote)
                            .copy(fontWeight = if (i == selected) FontWeight.SemiBold else FontWeight.Medium),
                        color = c.label,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/* ───────────── Pílulas ───────────── */

enum class PillTone { NEUTRAL, TINT, SAND, PEACH, DANGER }

@Composable
fun Pill(text: String, tone: PillTone = PillTone.NEUTRAL, dot: Boolean = false, icon: ImageVector? = null) {
    val c = HopeTheme.colors
    val (bg, fg) = when (tone) {
        PillTone.NEUTRAL -> c.fill to c.label2
        PillTone.TINT -> c.tintSoft to c.tintSoftLabel
        PillTone.SAND -> c.sandSoft to c.sandLabel
        PillTone.PEACH -> c.peachSoft to c.peachLabel
        PillTone.DANGER -> c.dangerSoft to c.danger
    }
    Row(
        Modifier.height(26.dp).clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (dot) Box(Modifier.size(6.dp).background(fg, CircleShape))
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(13.dp))
        Text(text, style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.SemiBold), color = fg, maxLines = 1)
    }
}

/* ───────────── Avatar ───────────── */

fun initials(name: String): String {
    val parts = name.replace(Regex("\\(.*?\\)"), "")
        .replace(Regex("^(dra?\\.?|dr\\.?)\\s+", RegexOption.IGNORE_CASE), "")
        .trim().split(Regex("\\s+")).filter { p -> p.any { it.isLetter() } }
    val first = parts.firstOrNull()?.firstOrNull()?.toString().orEmpty()
    val last = if (parts.size > 1) parts.last().first().toString() else ""
    return (first + last).uppercase()
}

@Composable
fun Avatar(name: String, size: Dp = 44.dp) {
    val hash = name.fold(7L) { acc, ch -> (acc * 31 + ch.code) and 0xFFFFFFFFL }
    val (a, b) = AvatarGradients[(hash % AvatarGradients.size).toInt()]
    Box(
        Modifier.size(size).clip(CircleShape).background(Brush.linearGradient(listOf(a, b))),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.38f).sp)
    }
}

/* ───────────── Cartões e listas agrupadas (Ajustes do iOS) ───────────── */

@Composable
fun HopeCard(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(20.dp), content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .shadow(if (HopeTheme.colors.isDark) 0.dp else 1.dp, RoundedCornerShape(Radius.xl), clip = false)
            .clip(RoundedCornerShape(Radius.xl))
            .background(HopeTheme.colors.bgElevated)
            .padding(padding),
    ) { content() }
}

@Composable
fun GroupSection(header: String? = null, footer: String? = null, content: @Composable () -> Unit) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (header != null) {
            Text(header.uppercase(), style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.Medium), color = c.label2, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.bgElevated)) { content() }
        if (footer != null) Text(footer, style = HopeTheme.type.footnote, color = c.label2, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
fun ListRow(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconBackground: Color = HopeTheme.colors.tint,
    value: String? = null,
    subtitle: String? = null,
    showDivider: Boolean = false,
    chevron: Boolean = false,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = HopeTheme.colors
    Column {
        if (showDivider) HorizontalDivider(Modifier.padding(start = if (icon != null) 58.dp else 16.dp), thickness = 0.5.dp, color = c.separator)
        Row(
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 50.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (icon != null) {
                Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(iconBackground), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    style = HopeTheme.type.body.copy(fontWeight = if (destructive) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (destructive) c.danger else c.label,
                    modifier = if (destructive) Modifier.fillMaxWidth() else Modifier,
                    textAlign = if (destructive) androidx.compose.ui.text.style.TextAlign.Center else null,
                )
                if (subtitle != null) Text(subtitle, style = HopeTheme.type.footnote, color = c.label2)
            }
            if (value != null) Text(value, style = HopeTheme.type.body, color = c.label2, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            if (chevron) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.label3)
        }
    }
}

/* ───────────── Cabeçalho de página (título grande) ───────────── */

@Composable
fun PageHeader(title: String, eyebrow: String? = null, subtitle: String? = null) {
    val c = HopeTheme.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (eyebrow != null) Text(eyebrow.uppercase(), style = HopeTheme.type.eyebrow, color = c.tint)
        Text(title, style = HopeTheme.type.largeTitle, color = c.label, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = HopeTheme.type.headline.copy(fontWeight = FontWeight.Normal), color = c.label2)
        }
    }
}

/* ───────────── Banner, mascote, vazio, skeleton ───────────── */

enum class BannerTone { NEUTRAL, TINT, DANGER }

@Composable
fun Banner(text: String, icon: ImageVector, tone: BannerTone = BannerTone.NEUTRAL, modifier: Modifier = Modifier) {
    val c = HopeTheme.colors
    val (bg, fg) = when (tone) {
        BannerTone.NEUTRAL -> c.fillSoft to c.label
        BannerTone.TINT -> c.tintSoft to c.tintSoftLabel
        BannerTone.DANGER -> c.dangerSoft to c.danger
    }
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(bg).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp).offset(y = 1.dp))
        Text(text, style = HopeTheme.type.subhead, color = fg)
    }
}

@Composable
fun MascotTile(size: Dp, modifier: Modifier = Modifier, tilt: Float = 0f) {
    Image(
        painter = painterResource(R.drawable.mascote),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .rotate(tilt)
            .size(size)
            .shadow(10.dp, RoundedCornerShape(size * 0.28f))
            .clip(RoundedCornerShape(size * 0.28f))
            .background(Color(0xFFF3F3F3)),
    )
}

@Composable
fun EmptyState(title: String, text: String? = null, action: @Composable (() -> Unit)? = null) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MascotTile(104.dp)
        Spacer(Modifier.height(4.dp))
        Text(title, style = HopeTheme.type.title3, color = HopeTheme.colors.label, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (text != null) Text(text, style = HopeTheme.type.callout, color = HopeTheme.colors.label2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        action?.invoke()
    }
}

@Composable
fun Skeleton(modifier: Modifier = Modifier, height: Dp = 16.dp, radius: Dp = 8.dp) {
    val c = HopeTheme.colors
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(modifier.height(height).clip(RoundedCornerShape(radius)).background(c.fill.copy(alpha = c.fill.alpha * alpha * 1.6f)))
}

@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val c = HopeTheme.colors
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(400), label = "progress")
    Box(modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)).background(c.fill)) {
        Box(Modifier.fillMaxWidth(animated).fillMaxHeight().clip(RoundedCornerShape(50)).background(c.tint))
    }
}

/** Opção de escolha (rádio ou caixa) em formato de cartão, como no questionário do site. */
@Composable
fun OptionRow(text: String, checked: Boolean, onClick: () -> Unit, multi: Boolean = false) {
    val c = HopeTheme.colors
    val border by animateColorAsState(if (checked) c.tint else c.separatorStrong, label = "border")
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(Radius.md))
            .background(if (checked) c.tintSoft else c.bgElevated)
            .border(if (checked) 2.dp else 1.dp, border, RoundedCornerShape(Radius.md))
            .clickable(role = if (multi) Role.Checkbox else Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val markShape = if (multi) RoundedCornerShape(7.dp) else CircleShape
        Box(
            Modifier.size(24.dp).clip(markShape)
                .background(if (checked) c.tint else Color.Transparent)
                .border(2.dp, if (checked) Color.Transparent else c.separatorStrong, markShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Icons.Rounded.Check, null, tint = c.onTint, modifier = Modifier.size(16.dp))
        }
        Text(text, style = HopeTheme.type.body, color = c.label, modifier = Modifier.weight(1f))
    }
}
