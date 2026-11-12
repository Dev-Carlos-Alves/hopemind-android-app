package br.edu.novaroma.hopemind.presentation.screen.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.MatchItem
import br.edu.novaroma.hopemind.presentation.common.formatCurrency
import br.edu.novaroma.hopemind.presentation.common.formatKm
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.Pill
import br.edu.novaroma.hopemind.presentation.components.PillTone
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

@Composable
fun bandLabel(band: String): Pair<String, PillTone> = when (band) {
    "alta" -> stringResource(R.string.band_alta) to PillTone.TINT
    "boa" -> stringResource(R.string.band_boa) to PillTone.SAND
    else -> stringResource(R.string.band_possivel) to PillTone.NEUTRAL
}

@Composable
fun ReasonLine(text: String, note: Boolean = false) {
    val c = HopeTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (note) Icons.Rounded.Info else Icons.Rounded.Check,
            null,
            tint = if (note) c.label3 else c.tint,
            modifier = Modifier.size(17.dp).padding(top = 2.dp),
        )
        Text(text, style = HopeTheme.type.subhead, color = if (note) c.label2 else c.label)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchCard(match: MatchItem, rank: Int, onBook: () -> Unit, onExplain: () -> Unit, onProfile: () -> Unit = {}) {
    val c = HopeTheme.colors
    val p = match.psychologist
    val (bandText, bandTone) = bandLabel(match.band)

    HopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                val openProfile = stringResource(R.string.profile_open, p.name)
                Box(Modifier.clip(CircleShape).clickable(onClickLabel = openProfile, onClick = onProfile)) { Avatar(p.name, 60.dp) }
                Column(
                    Modifier.weight(1f).clickable(onClickLabel = openProfile, onClick = onProfile),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(p.name, style = HopeTheme.type.headline, color = c.label)
                    Text(p.specialty, style = HopeTheme.type.footnote, color = c.label2)
                    Text(p.crp, style = HopeTheme.type.caption, color = c.label3)
                    Row(Modifier.padding(top = 6.dp)) { Pill(bandText, bandTone, dot = true) }
                }
                if (rank > 0) {
                    Box(Modifier.size(30.dp).clip(CircleShape).background(c.fillSoft), contentAlignment = Alignment.Center) {
                        Text("$rank", style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.Bold), color = c.label2)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                match.reasons.take(3).forEach { ReasonLine(it) }
                Row(
                    Modifier.clickable(onClick = onExplain).padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.why_recommendation), style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.SemiBold), color = c.tint)
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = c.tint, modifier = Modifier.size(18.dp))
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if ("presencial" in p.modalities && p.neighborhood != null) {
                    val km = match.distanceKm?.let { " · " + formatKm(it, stringResource(R.string.km_less_than_one)) }.orEmpty()
                    Pill(p.neighborhood + km, PillTone.PEACH, icon = Icons.Rounded.LocationOn)
                }
                if ("online" in p.modalities) Pill(stringResource(R.string.online), icon = Icons.Rounded.Videocam)
                p.approaches.take(2).forEach { Pill(it) }
            }

            HorizontalDivider(color = c.separator)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatCurrency(p.sessionFee), style = HopeTheme.type.headline, color = c.label)
                    Text(stringResource(R.string.per_session), style = HopeTheme.type.caption, color = c.label2)
                }
                p.contactLink?.takeIf { it.isNotBlank() }?.let { link ->
                    val uriHandler = LocalUriHandler.current
                    val label = stringResource(R.string.contact_professional, p.name)
                    Box(
                        Modifier.padding(end = 8.dp).size(44.dp).clip(CircleShape).background(c.fill)
                            .clickable(onClickLabel = label) { runCatching { uriHandler.openUri(link) } }
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = c.label, modifier = Modifier.size(20.dp))
                    }
                }
                HopeButton(stringResource(R.string.see_times), onBook, variant = ButtonVariant.TINTED, icon = Icons.Rounded.CalendarMonth)
            }
        }
    }
}
