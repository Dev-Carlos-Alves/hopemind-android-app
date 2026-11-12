package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.MatchPsychologist
import br.edu.novaroma.hopemind.domain.model.PatientCard
import br.edu.novaroma.hopemind.domain.model.PsychologistCard
import br.edu.novaroma.hopemind.presentation.common.formatCurrency
import br.edu.novaroma.hopemind.presentation.common.formatKm
import br.edu.novaroma.hopemind.presentation.common.placeLabel
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.Pill
import br.edu.novaroma.hopemind.presentation.components.PillTone
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/*
 * Perfis detalhados: tocar na foto ou no nome de alguém abre tudo o que a pessoa contou sobre si,
 * para o paciente escolher com mais segurança e para o psicólogo conhecer quem agendou.
 */

/** Psicólogo, a partir de um card de Matches. */
@Composable
fun PsychologistProfileSheet(p: MatchPsychologist, distanceKm: Double?, onBook: () -> Unit, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val lessThanOne = stringResource(R.string.km_less_than_one)
    ProfileSheet(
        name = p.name,
        lines = listOfNotNull(p.specialty, p.crp),
        onDismiss = onDismiss,
        actions = {
            p.contactLink?.takeIf { it.isNotBlank() }?.let { link ->
                HopeButton(
                    stringResource(R.string.contact_action), { runCatching { uriHandler.openUri(link) } }, Modifier.weight(1f),
                    variant = ButtonVariant.GRAY, size = ButtonSize.LG, icon = Icons.AutoMirrored.Rounded.Chat,
                )
            }
            HopeButton(
                stringResource(R.string.see_times), { onDismiss(); onBook() }, Modifier.weight(1f),
                variant = ButtonVariant.FILLED, size = ButtonSize.LG, icon = Icons.Rounded.CalendarMonth,
            )
        },
    ) {
        Chips(buildList {
            if ("online" in p.modalities) add(stringResource(R.string.online) to Icons.Rounded.Videocam)
            if ("presencial" in p.modalities && p.neighborhood != null) {
                val km = distanceKm?.let { " · " + formatKm(it, lessThanOne) }.orEmpty()
                add(p.neighborhood + km to Icons.Rounded.LocationOn)
            }
        })
        Section(stringResource(R.string.profile_about_psy), p.biography ?: stringResource(R.string.profile_no_bio))
        if (p.approaches.isEmpty()) p.therapeuticApproach?.let { Section(stringResource(R.string.approach_label), it) }
        if (p.approaches.isNotEmpty()) ChipSection(stringResource(R.string.profile_approaches), p.approaches)
        p.experience?.let { Section(stringResource(R.string.profile_experience), it) }
        if (p.audiences.isNotEmpty()) ChipSection(stringResource(R.string.profile_audiences), p.audiences)
        if (p.demands.isNotEmpty()) ChipSection(stringResource(R.string.profile_demands), p.demands)
        if (p.experiencedWith.isNotEmpty()) ChipSection(stringResource(R.string.profile_experienced_with), p.experiencedWith)
        Section(stringResource(R.string.session_fee_label), "${formatCurrency(p.sessionFee)} ${stringResource(R.string.per_session)}")
    }
}

/** Psicólogo, a partir de uma sessão do paciente. */
@Composable
fun PsychologistCardSheet(p: PsychologistCard, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    ProfileSheet(
        name = p.nome,
        lines = listOf(p.especialidade, p.crp),
        onDismiss = onDismiss,
        actions = {
            p.contato?.takeIf { it.isNotBlank() }?.let { link ->
                HopeButton(
                    stringResource(R.string.contact_action), { runCatching { uriHandler.openUri(link) } }, Modifier.weight(1f),
                    size = ButtonSize.LG, icon = Icons.AutoMirrored.Rounded.Chat,
                )
            }
        },
    ) {
        Section(stringResource(R.string.profile_about_psy), p.biografia ?: stringResource(R.string.profile_no_bio))
        p.abordagem?.let { Section(stringResource(R.string.approach_label), it) }
        placeLabel(p.bairro, p.cidade).takeIf { it.isNotBlank() }?.let { Section(stringResource(R.string.profile_office), it) }
        Section(stringResource(R.string.session_fee_label), "${formatCurrency(p.valorSessao)} ${stringResource(R.string.per_session)}")
    }
}

/** Paciente, para o psicólogo com quem ele agendou. */
@Composable
fun PatientProfileSheet(p: PatientCard, onDismiss: () -> Unit) {
    val ageAndGender = listOfNotNull(
        p.idade?.let { stringResource(R.string.profile_age, it) },
        p.genero?.takeIf { it.isNotBlank() && it != "Não informado" },
    ).joinToString(" · ")
    ProfileSheet(
        name = p.nome,
        lines = listOfNotNull(stringResource(R.string.role_patient), ageAndGender.takeIf { it.isNotBlank() }),
        onDismiss = onDismiss,
    ) {
        Section(stringResource(R.string.profile_about_patient), p.sobre?.takeIf { it.isNotBlank() } ?: stringResource(R.string.profile_no_about))
        if (p.prioridades.isNotEmpty()) ChipSection(stringResource(R.string.profile_priorities), p.prioridades)
        if (p.outrosTemas.isNotEmpty()) ChipSection(stringResource(R.string.profile_other_topics), p.outrosTemas)
        p.modalidade?.let { Section(stringResource(R.string.profile_modality), it) }
        placeLabel(p.bairro, p.cidade).takeIf { it.isNotBlank() }?.let { Section(stringResource(R.string.profile_region), it) }
    }
}

/* ───────────── building blocks ───────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSheet(
    name: String,
    lines: List<String>,
    onDismiss: () -> Unit,
    actions: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val c = HopeTheme.colors
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = c.bgElevated) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(name, 72.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(name, style = HopeTheme.type.title2, color = c.label)
                    lines.forEach { Text(it, style = HopeTheme.type.subhead, color = c.label2) }
                }
            }
            content()
            if (actions != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), content = actions)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Section(title: String, text: String) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title.uppercase(), style = HopeTheme.type.caption, color = c.label2)
        Text(text, style = HopeTheme.type.body, color = c.label)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipSection(title: String, items: List<String>) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), style = HopeTheme.type.caption, color = c.label2)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { Pill(it) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chips(items: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>) {
    if (items.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (text, icon) -> Pill(text, PillTone.PEACH, icon = icon) }
    }
}
