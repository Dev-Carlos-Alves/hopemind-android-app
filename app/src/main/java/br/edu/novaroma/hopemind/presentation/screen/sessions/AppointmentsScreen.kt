package br.edu.novaroma.hopemind.presentation.screen.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.data.AppContainer
import br.edu.novaroma.hopemind.domain.model.Appointment
import br.edu.novaroma.hopemind.domain.model.AppointmentStatus
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.hopeViewModel
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.EmptyState
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.PageHeader
import br.edu.novaroma.hopemind.presentation.components.Pill
import br.edu.novaroma.hopemind.presentation.components.PillTone
import br.edu.novaroma.hopemind.presentation.components.SegmentedControl
import br.edu.novaroma.hopemind.presentation.components.Skeleton
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class AppointmentsState(val loading: Boolean = true, val items: List<Appointment> = emptyList(), val error: String? = null)

class AppointmentsViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AppointmentsState())
    val state: StateFlow<AppointmentsState> = _state

    fun load() = viewModelScope.launch {
        container.appointments.list()
            .onSuccess { list -> _state.update { it.copy(loading = false, items = list) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
    }
}

private fun Appointment.localDateTime(): LocalDateTime =
    Instant.parse(dataHora).atZone(ZoneId.systemDefault()).toLocalDateTime()

@Composable
fun AppointmentsRoute(user: User, padding: PaddingValues, onSeeRecommendations: () -> Unit) {
    val vm = hopeViewModel { AppointmentsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(state.error) { state.error?.let(toast::error) }
    AppointmentsContent(user, state, padding, onSeeRecommendations)
}

@Composable
fun AppointmentsContent(user: User, state: AppointmentsState, padding: PaddingValues, onSeeRecommendations: () -> Unit) {
    val c = HopeTheme.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val now = LocalDateTime.now()
    val upcoming = tab == 0
    val list = state.items
        .map { it to it.localDateTime() }
        .filter { (_, t) -> if (upcoming) !t.isBefore(now) else t.isBefore(now) }
        .sortedBy { (_, t) -> t }
        .let { if (upcoming) it else it.reversed() }
    val groups = list.groupBy { (_, t) -> t.toLocalDate() }
    val upcomingCount = state.items.count { !it.localDateTime().isBefore(now) && it.status == AppointmentStatus.SCHEDULED }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 20.dp, 16.dp, padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeader(
                eyebrow = stringResource(if (user.isPsychologist) R.string.tab_agenda else R.string.sessions_eyebrow),
                title = stringResource(if (user.isPsychologist) R.string.agenda_title else R.string.sessions_title),
                subtitle = when {
                    state.loading -> null
                    upcomingCount == 0 -> stringResource(R.string.sessions_none)
                    else -> pluralStringResource(R.plurals.sessions_count, upcomingCount, upcomingCount)
                },
            )
        }
        item {
            SegmentedControl(listOf(stringResource(R.string.upcoming), stringResource(R.string.past)), tab, { tab = it })
        }

        when {
            state.loading -> item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.bgElevated).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Skeleton(Modifier.width(52.dp), height = 26.dp)
                            Skeleton(Modifier.width(40.dp), height = 40.dp, radius = 20.dp)
                            Skeleton(Modifier.fillMaxWidth(0.6f), height = 14.dp)
                        }
                    }
                }
            }
            groups.isEmpty() -> item {
                HopeCard {
                    EmptyState(
                        title = stringResource(if (upcoming) R.string.empty_upcoming_title else R.string.empty_past_title),
                        text = stringResource(
                            when {
                                !upcoming -> R.string.empty_past_text
                                user.isPsychologist -> R.string.empty_upcoming_psy
                                else -> R.string.empty_upcoming_patient
                            },
                        ),
                        action = if (upcoming && !user.isPsychologist) {
                            { HopeButton(stringResource(R.string.see_recommendations), onSeeRecommendations) }
                        } else null,
                    )
                }
            }
            else -> groups.forEach { (date, entries) ->
                item(key = date.toString()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(dayLabel(date).uppercase(), style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.Medium), color = c.label2, modifier = Modifier.padding(horizontal = 16.dp))
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.bgElevated)) {
                            entries.forEachIndexed { i, (item, time) ->
                                if (i > 0) HorizontalDivider(Modifier.padding(start = 138.dp), thickness = 0.5.dp, color = c.separator)
                                AppointmentRow(item, time, user.isPsychologist)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentRow(item: Appointment, time: LocalDateTime, isPsychologist: Boolean) {
    val c = HopeTheme.colors
    val other = if (isPsychologist) item.pacienteNome else item.psicologoNome
    val (status, tone) = when (item.status) {
        AppointmentStatus.SCHEDULED -> R.string.status_scheduled to PillTone.TINT
        AppointmentStatus.COMPLETED -> R.string.status_completed to PillTone.NEUTRAL
        AppointmentStatus.CANCELLED -> R.string.status_cancelled to PillTone.DANGER
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            time.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = HopeTheme.type.title3.copy(fontWeight = FontWeight.Bold),
            color = c.label,
            modifier = Modifier.width(58.dp),
        )
        Avatar(other, 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(other, style = HopeTheme.type.headline, color = c.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                if (isPsychologist) stringResource(R.string.role_patient) else item.especialidade,
                style = HopeTheme.type.footnote,
                color = c.label2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.padding(top = 4.dp)) { Pill(stringResource(status), tone) }
        }
    }
}

@Composable
private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (ChronoUnit.DAYS.between(today, date)) {
        0L -> stringResource(R.string.today)
        1L -> stringResource(R.string.tomorrow)
        -1L -> stringResource(R.string.yesterday)
        else -> {
            val locale = Locale.getDefault()
            val dayMonth = if (locale.language == "pt") "EEEE, d 'de' MMMM" else "EEEE, MMMM d"
            val pattern = if (date.year == today.year) DateTimeFormatter.ofPattern(dayMonth, locale)
            else DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
            date.format(pattern)
        }
    }
}
