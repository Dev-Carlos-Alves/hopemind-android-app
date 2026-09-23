package br.edu.novaroma.hopemind.presentation.screen.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Bookable
import br.edu.novaroma.hopemind.domain.model.MatchItem
import br.edu.novaroma.hopemind.presentation.common.formatCurrency
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val HOURS = listOf(8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 19)

/** Próximos 14 dias sem domingos — mesma regra do site. */
fun upcomingDays(from: LocalDate = LocalDate.now()): List<LocalDate> =
    generateSequence(from.plusDays(1)) { it.plusDays(1) }.filter { it.dayOfWeek != DayOfWeek.SUNDAY }.take(14).toList()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingSheet(psychologist: Bookable, onDismiss: () -> Unit, onConfirm: suspend (LocalDateTime) -> Boolean) {
    val c = HopeTheme.colors
    val locale = Locale.getDefault()
    val days = remember { upcomingDays() }
    var day by remember { mutableStateOf(days.first()) }
    var hour by remember { mutableStateOf<Int?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = c.bgElevated) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.booking_title), style = HopeTheme.type.title3, color = c.label)

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(psychologist.name, 60.dp)
                Column {
                    Text(psychologist.name, style = HopeTheme.type.headline, color = c.label)
                    Text("${psychologist.specialty} · ${psychologist.crp}", style = HopeTheme.type.subhead, color = c.label2)
                    Text("${formatCurrency(psychologist.sessionFee)} ${stringResource(R.string.per_session)}", style = HopeTheme.type.subhead, color = c.label)
                }
            }
            psychologist.biography?.let { Text(it, style = HopeTheme.type.callout, color = c.label2) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.booking_day), style = HopeTheme.type.headline, color = c.label)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
                    items(days) { d ->
                        val active = d == day
                        Column(
                            Modifier
                                .width(58.dp)
                                .height(74.dp)
                                .clip(RoundedCornerShape(Radius.md))
                                .background(if (active) c.tint else c.fillSoft)
                                .clickable { day = d; hour = null },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", "").uppercase(),
                                style = HopeTheme.type.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = if (active) c.onTint else c.label2,
                            )
                            Text(d.dayOfMonth.toString(), style = HopeTheme.type.title3.copy(fontWeight = FontWeight.Bold), color = if (active) c.onTint else c.label)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.booking_time), style = HopeTheme.type.headline, color = c.label)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 4) {
                    HOURS.forEach { h ->
                        val active = h == hour
                        Box(
                            Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (active) c.tint else c.bgElevated)
                                .border(1.dp, if (active) c.tint else c.separatorStrong, RoundedCornerShape(50))
                                .clickable { hour = h },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("%02d:00".format(h), style = HopeTheme.type.callout.copy(fontWeight = FontWeight.Medium), color = if (active) c.onTint else c.label)
                        }
                    }
                    repeat((4 - HOURS.size % 4) % 4) { Spacer(Modifier.weight(1f)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = c.label2, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.booking_note), style = HopeTheme.type.footnote, color = c.label2)
                }
            }

            val slot = hour?.let { day.atTime(it, 0) }
            val label = slot?.let {
                val weekday = it.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replace(".", "")
                val month = it.month.getDisplayName(TextStyle.SHORT, locale).replace(".", "")
                "$weekday, ${it.dayOfMonth} $month · ${it.hour}h"
            }
            HopeButton(
                text = if (label != null) stringResource(R.string.booking_confirm, label) else stringResource(R.string.booking_choose),
                onClick = {
                    val chosen = slot ?: return@HopeButton
                    saving = true
                    scope.launch {
                        val ok = onConfirm(chosen)
                        saving = false
                        if (ok) sheet.hide().also { onDismiss() }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                size = ButtonSize.LG,
                enabled = slot != null,
                loading = saving,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

fun LocalDateTime.toIsoInstant(): String = atZone(ZoneId.systemDefault()).toInstant().toString()

fun LocalDateTime.longLabel(locale: Locale = Locale.getDefault()): String =
    format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.FULL).withLocale(locale)) + " · " +
        format(DateTimeFormatter.ofPattern("HH:mm", locale))

private val COMPONENTS = listOf(
    // Mesmos pesos do motor do site (hm-match-1.1.0).
    "demanda" to (R.string.comp_demanda to 25),
    "estilo" to (R.string.comp_estilo to 18),
    "localizacao" to (R.string.comp_modalidade to 15),
    "experiencia" to (R.string.comp_experiencia to 14),
    "relacao" to (R.string.comp_relacao to 13),
    "disponibilidade" to (R.string.comp_disponibilidade to 10),
    "preferencias" to (R.string.comp_preferencias to 5),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplainSheet(match: MatchItem, algorithm: String, onDismiss: () -> Unit) {
    val c = HopeTheme.colors
    val (bandText, _) = bandLabel(match.band)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.bgElevated) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(stringResource(R.string.why_recommendation), style = HopeTheme.type.title3, color = c.label)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(match.psychologist.name)
                Column {
                    Text(match.psychologist.name, style = HopeTheme.type.headline, color = c.label)
                    Text(bandText, style = HopeTheme.type.footnote, color = c.label2)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                match.reasons.forEach { ReasonLine(it) }
                match.considerations.forEach { ReasonLine(it, note = true) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(stringResource(R.string.how_we_calculate), style = HopeTheme.type.headline, color = c.label)
                COMPONENTS.forEach { (key, meta) ->
                    val value = (match.components[key] ?: 0.0).toFloat().coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(stringResource(meta.first), style = HopeTheme.type.subhead, color = c.label, modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.weight_format, meta.second), style = HopeTheme.type.caption, color = c.label3)
                        }
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(c.fill)) {
                            Box(Modifier.fillMaxWidth(value).fillMaxHeight().clip(RoundedCornerShape(50)).background(c.tint))
                        }
                    }
                }
            }
            Text(stringResource(R.string.explain_footer, algorithm), style = HopeTheme.type.footnote, color = c.label2, textAlign = TextAlign.Start)
            Spacer(Modifier.height(12.dp))
        }
    }
}
