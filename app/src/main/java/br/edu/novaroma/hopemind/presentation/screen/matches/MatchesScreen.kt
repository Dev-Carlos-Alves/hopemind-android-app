package br.edu.novaroma.hopemind.presentation.screen.matches

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Bookable
import br.edu.novaroma.hopemind.domain.model.MatchItem
import br.edu.novaroma.hopemind.domain.model.SafetyLevel
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.firstName
import br.edu.novaroma.hopemind.presentation.common.hopeViewModel
import br.edu.novaroma.hopemind.presentation.common.placeLabel
import br.edu.novaroma.hopemind.presentation.components.BrandWash
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.EmptyState
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.MascotTile
import br.edu.novaroma.hopemind.presentation.components.PageHeader
import br.edu.novaroma.hopemind.presentation.components.SafetyPanel
import br.edu.novaroma.hopemind.presentation.components.SegmentedControl
import br.edu.novaroma.hopemind.presentation.components.Skeleton
import br.edu.novaroma.hopemind.presentation.screen.AddressCard
import br.edu.novaroma.hopemind.presentation.screen.triage.QuestionnaireCard
import br.edu.novaroma.hopemind.presentation.screen.triage.SubmitOutcome
import br.edu.novaroma.hopemind.presentation.screen.triage.TriageState
import br.edu.novaroma.hopemind.presentation.screen.triage.TriageViewModel
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius
import kotlinx.serialization.json.JsonElement

/** Quantos matches aparecem antes do "Ver mais". */
const val TOP_MATCHES = 10

/**
 * Matches (substitui "Para você" e "Meu questionário"): questionário expansível no topo,
 * os 10 mais compatíveis e, ao tocar em "Ver mais", os de compatibilidade mais baixa.
 */
@Composable
fun MatchesRoute(user: User, padding: PaddingValues, openQuestionnaire: Boolean, onNeedsSupport: () -> Unit) {
    val vm = hopeViewModel { MatchesViewModel(it) }
    val triageVm = hopeViewModel { TriageViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val triage by triageVm.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val context = LocalContext.current
    var booking by remember { mutableStateOf<Bookable?>(null) }
    var explaining by remember { mutableStateOf<MatchItem?>(null) }
    val list = rememberLazyListState()

    LaunchedEffect(Unit) { if (openQuestionnaire || !user.hasTriage) vm.setQuestionnaireOpen(true) }
    LaunchedEffect(user.hasTriage) { vm.load(user.hasTriage) }
    LaunchedEffect(state.error) { state.error?.let { toast.error(it); vm.consumeError() } }
    LaunchedEffect(triage.error) { triage.error?.let { toast.error(it); triageVm.consumeError() } }

    MatchesContent(
        user = user,
        state = state,
        triage = triage,
        padding = padding,
        listState = list,
        onQuery = vm::setQuery,
        onSort = vm::setSort,
        onToggleQuestionnaire = { vm.setQuestionnaireOpen(!state.questionnaireOpen) },
        onToggleMore = vm::toggleShowAll,
        onBook = { m -> booking = m.toBookable() },
        onExplain = { explaining = it },
        onAnswer = triageVm::setAnswer,
        onBack = triageVm::back,
        onNext = {
            triageVm.next { outcome ->
                triageVm.restart()
                vm.setQuestionnaireOpen(false)
                vm.load(true)
                when (outcome) {
                    is SubmitOutcome.NeedsSupport -> onNeedsSupport()
                    SubmitOutcome.Saved -> toast.success(context.getString(R.string.triage_saved_patient))
                }
            }
        },
        onAddressSaved = { vm.load(true) },
    )

    booking?.let { b ->
        BookingSheet(b, onDismiss = { booking = null }) { slot ->
            val result = vm.book(b.id, slot.toIsoInstant())
            result.onSuccess { toast.success(context.getString(R.string.booking_success, slot.longLabel())) }
                .onFailure { toast.error(it.message ?: "") }
            result.isSuccess
        }
    }
    explaining?.let { m -> ExplainSheet(m, state.data?.algorithmVersion.orEmpty(), onDismiss = { explaining = null }) }
}

private fun MatchItem.toBookable() = Bookable(
    psychologist.id, psychologist.name, psychologist.crp, psychologist.specialty, psychologist.biography, psychologist.sessionFee,
)

private val EXCLUSION_TEXT = mapOf(
    "modalidade" to R.string.excl_modalidade,
    "regiao" to R.string.excl_regiao,
    "faixa_etaria" to R.string.excl_faixa_etaria,
    "demanda_nao_atendida" to R.string.excl_demanda_nao_atendida,
    "sem_horario" to R.string.excl_sem_horario,
    "protocolo_seguranca" to R.string.excl_protocolo_seguranca,
)

/** Conteúdo sem ViewModel (facilita pré-visualizações e screenshots). */
@Composable
fun MatchesContent(
    user: User,
    state: MatchesState,
    triage: TriageState,
    padding: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onQuery: (String) -> Unit = {},
    onSort: (MatchSort) -> Unit = {},
    onToggleQuestionnaire: () -> Unit = {},
    onToggleMore: () -> Unit = {},
    onBook: (MatchItem) -> Unit = {},
    onExplain: (MatchItem) -> Unit = {},
    onAnswer: (String, JsonElement?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onNext: () -> Unit = {},
    onAddressSaved: () -> Unit = {},
) {
    val c = HopeTheme.colors
    val data = state.data
    val all = data?.results.orEmpty()
    val rank = remember(data) { all.mapIndexed { i, m -> m.psychologistId to i + 1 }.toMap() }
    val q = state.query.trim().lowercase()
    fun List<MatchItem>.filtered() = filter {
        q.isEmpty() || "${it.psychologist.name} ${it.psychologist.specialty} ${it.psychologist.approaches.joinToString(" ")} ${it.psychologist.neighborhood.orEmpty()}"
            .lowercase().contains(q)
    }
    fun List<MatchItem>.ordered() =
        if (state.sort == MatchSort.DISTANCE) sortedWith(compareBy<MatchItem> { it.distanceKm ?: Double.MAX_VALUE }.thenByDescending { it.score }) else this
    // Os 10 primeiros são sempre os de maior compatibilidade; a ordenação vale dentro de cada grupo.
    val top = all.take(TOP_MATCHES).filtered().ordered()
    val rest = all.drop(TOP_MATCHES).filtered().ordered()

    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = padding.calculateTopPadding() + 20.dp,
            bottom = padding.calculateBottomPadding() + 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeader(
                eyebrow = stringResource(R.string.matches_eyebrow),
                title = if (user.hasTriage) stringResource(R.string.matches_title, firstName(user.name)) else stringResource(R.string.matches_title_empty),
                subtitle = stringResource(if (user.hasTriage) R.string.matches_subtitle_ready else R.string.matches_subtitle_empty),
            )
        }

        if (data != null && data.safety.level != SafetyLevel.NONE) {
            item { SafetyPanel(data.safety, compact = true) }
        }

        if (!user.hasTriage && !state.questionnaireOpen) {
            item { Hero(onToggleQuestionnaire) }
        }

        item { QuestionnaireDisclosure(answered = user.hasTriage, open = state.questionnaireOpen, onToggle = onToggleQuestionnaire) }
        if (state.questionnaireOpen) {
            item(key = "questionnaire") { QuestionnaireCard(user, triage, onAnswer, onBack, onNext) }
        }

        if (data != null && data.location == null) {
            item {
                AddressCard(
                    psychologist = false,
                    title = stringResource(R.string.address_missing_title),
                    footer = stringResource(if (data.modality == "online") R.string.address_missing_online else R.string.address_missing_inperson),
                    startEditing = true,
                    onSaved = { onAddressSaved() },
                )
            }
        }

        if (!user.hasTriage) return@LazyColumn

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.top_matches), style = HopeTheme.type.title2, color = c.label)
                    data?.location?.let { loc ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, null, tint = c.label2, modifier = Modifier.size(13.dp))
                            Text(stringResource(R.string.distances_from, placeLabel(loc.neighborhood, loc.city)), style = HopeTheme.type.footnote, color = c.label2)
                        }
                    }
                }
                if (all.any { it.distanceKm != null }) {
                    SegmentedControl(
                        options = listOf(stringResource(R.string.sort_compat), stringResource(R.string.sort_distance)),
                        selected = state.sort.ordinal,
                        onSelect = { onSort(MatchSort.entries[it]) },
                    )
                }
                SearchField(state.query, onQuery)
            }
        }

        when {
            state.loading -> items3 { MatchSkeleton() }
            top.isEmpty() && rest.isEmpty() -> item {
                HopeCard {
                    EmptyState(
                        title = stringResource(if (q.isNotEmpty()) R.string.empty_search_title else R.string.empty_matches_title),
                        text = if (q.isNotEmpty()) stringResource(R.string.empty_search_text, state.query) else stringResource(R.string.empty_matches_text),
                        action = if (q.isEmpty()) {
                            { HopeButton(stringResource(R.string.review_answers), onToggleQuestionnaire, variant = ButtonVariant.TINTED) }
                        } else null,
                    )
                }
            }
            else -> {
                if (top.isEmpty()) item { Text(stringResource(R.string.none_in_top), style = HopeTheme.type.callout, color = c.label2) }
                itemsIndexed(top, key = { _, m -> m.psychologistId }) { _, m ->
                    MatchCard(m, rank[m.psychologistId] ?: 0, onBook = { onBook(m) }, onExplain = { onExplain(m) })
                }
                if (rest.isNotEmpty()) {
                    item { MoreToggle(rest.size, state.showAll, onToggleMore) }
                    if (state.showAll) {
                        item { Text(stringResource(R.string.other_options), style = HopeTheme.type.title3, color = c.label, modifier = Modifier.padding(top = 4.dp)) }
                        itemsIndexed(rest, key = { _, m -> m.psychologistId }) { _, m ->
                            MatchCard(m, rank[m.psychologistId] ?: 0, onBook = { onBook(m) }, onExplain = { onExplain(m) })
                        }
                    }
                }
            }
        }

        if (data != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                    val total = data.excluded.values.sum()
                    if (total > 0) {
                        val why = data.excluded.keys.mapNotNull { EXCLUSION_TEXT[it] }.map { stringResource(it) }.joinToString(", ")
                        FootNote(Icons.Rounded.Tune, pluralStringResource(R.plurals.excluded_note, total, total, why))
                    }
                    FootNote(Icons.Rounded.Info, stringResource(R.string.estimate_note))
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.items3(content: @Composable () -> Unit) =
    items(3) { content() }

@Composable
private fun QuestionnaireDisclosure(answered: Boolean, open: Boolean, onToggle: () -> Unit) {
    val c = HopeTheme.colors
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "chevron")
    HopeCard(Modifier.clip(RoundedCornerShape(Radius.xl)).clickable(onClick = onToggle), padding = PaddingValues(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(c.tintSoft), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Assignment, null, tint = c.tintSoftLabel, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.my_questionnaire), style = HopeTheme.type.headline, color = c.label)
                Text(
                    stringResource(if (answered) R.string.my_questionnaire_done else R.string.my_questionnaire_todo),
                    style = HopeTheme.type.footnote,
                    color = c.label2,
                )
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (open) R.string.q_close else if (answered) R.string.q_open_edit else R.string.q_answer),
                        style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.SemiBold),
                        color = c.tint,
                    )
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = c.tint, modifier = Modifier.size(20.dp).rotate(turn))
                }
            }
        }
    }
}

@Composable
private fun MoreToggle(count: Int, open: Boolean, onClick: () -> Unit) {
    val c = HopeTheme.colors
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "more")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.lg))
            .border(1.dp, c.separatorStrong, RoundedCornerShape(Radius.lg))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (open) stringResource(R.string.hide_more) else pluralStringResource(R.plurals.see_more, count, count),
                style = HopeTheme.type.headline,
                color = c.label,
            )
            Text(stringResource(R.string.more_subtitle), style = HopeTheme.type.footnote, color = c.label2)
        }
        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = c.tint, modifier = Modifier.size(24.dp).rotate(turn))
    }
}

@Composable
private fun FootNote(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = HopeTheme.colors.label2, modifier = Modifier.size(15.dp).padding(top = 2.dp))
        Text(text, style = HopeTheme.type.footnote, color = HopeTheme.colors.label2)
    }
}

@Composable
private fun Hero(onStart: () -> Unit) {
    val c = HopeTheme.colors
    BrandWash(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xl))) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            MascotTile(84.dp, tilt = -5f)
            Text(stringResource(R.string.hero_title), style = HopeTheme.type.title1, color = c.label)
            Text(stringResource(R.string.hero_text), style = HopeTheme.type.callout, color = c.label2)
            HopeButton(stringResource(R.string.hero_cta), onStart, size = ButtonSize.LG, trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward)
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val c = HopeTheme.colors
    Row(
        Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(Radius.sm)).background(c.fill).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = c.label3, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(stringResource(R.string.search_placeholder), style = HopeTheme.type.body, color = c.label3)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = HopeTheme.type.body.copy(color = c.label),
                cursorBrush = SolidColor(c.tint),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MatchSkeleton() {
    HopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Skeleton(Modifier.size(60.dp), height = 60.dp, radius = 30.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Skeleton(Modifier.fillMaxWidth(0.6f), height = 18.dp)
                    Skeleton(Modifier.fillMaxWidth(0.4f), height = 12.dp)
                }
            }
            Skeleton(Modifier.fillMaxWidth(), height = 12.dp)
            Skeleton(Modifier.fillMaxWidth(0.8f), height = 12.dp)
        }
    }
}
