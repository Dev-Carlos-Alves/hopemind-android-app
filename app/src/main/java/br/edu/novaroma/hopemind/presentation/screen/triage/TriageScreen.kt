package br.edu.novaroma.hopemind.presentation.screen.triage

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.hopeViewModel
import br.edu.novaroma.hopemind.presentation.components.Banner
import br.edu.novaroma.hopemind.presentation.components.BannerTone
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.EmptyState
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.PageHeader
import br.edu.novaroma.hopemind.presentation.components.ProgressBar
import br.edu.novaroma.hopemind.presentation.components.Skeleton
import br.edu.novaroma.hopemind.presentation.screen.AddressCard
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

@Composable
fun TriageRoute(user: User, padding: PaddingValues, onSaved: () -> Unit, onNeedsSupport: () -> Unit) {
    val vm = hopeViewModel { TriageViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val context = LocalContext.current
    val list = rememberLazyListState()

    LaunchedEffect(state.error) {
        state.error?.let { toast.error(it); vm.consumeError() }
    }
    LaunchedEffect(state.step) { list.animateScrollToItem(0) }

    TriageContent(
        user = user,
        state = state,
        padding = padding,
        listState = list,
        onAnswer = vm::setAnswer,
        onBack = vm::back,
        onNext = {
            vm.next { outcome ->
                when (outcome) {
                    is SubmitOutcome.NeedsSupport -> onNeedsSupport()
                    SubmitOutcome.Saved -> {
                        toast.success(context.getString(if (user.isPsychologist) R.string.triage_saved_psy else R.string.triage_saved_patient))
                        onSaved()
                    }
                }
            }
        },
    )
}

@Composable
fun TriageContent(
    user: User,
    state: TriageState,
    padding: PaddingValues,
    listState: LazyListState = rememberLazyListState(),
    onAnswer: (String, kotlinx.serialization.json.JsonElement?) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val c = HopeTheme.colors

    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 20.dp, 16.dp, padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeader(
                eyebrow = stringResource(if (user.isPsychologist) R.string.triage_eyebrow_psy else R.string.triage_eyebrow_patient),
                title = stringResource(if (user.isPsychologist) R.string.triage_title_psy else R.string.triage_title_patient),
                subtitle = stringResource(if (user.isPsychologist) R.string.triage_subtitle_psy else R.string.triage_subtitle_patient),
            )
        }

        if (user.isPsychologist) item { AddressCard(psychologist = true) }

        item { QuestionnaireCard(user, state, onAnswer, onBack, onNext) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 4.dp)) {
                Icon(Icons.Rounded.Lock, null, tint = c.label2, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                Text(stringResource(R.string.privacy_note), style = HopeTheme.type.footnote, color = c.label2)
            }
        }
    }
}


/** Questionário em etapas (carregado da API). Usado no Perfil de atendimento e dentro de Matches. */
@Composable
fun QuestionnaireCard(
    user: User,
    state: TriageState,
    onAnswer: (String, kotlinx.serialization.json.JsonElement?) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = HopeTheme.colors
    val q = state.questionnaire
    val sections = q?.sections.orEmpty()
    val section = sections.getOrNull(state.step)
    val isLast = state.step == sections.lastIndex
    val labelsByCode = remember(q) {
        q?.sections.orEmpty().flatMap { it.questions }.associate { question -> question.code to question.options.associate { it.value to it.label } }
    }
    HopeCard(modifier) {
        when {
            state.loading -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Skeleton(Modifier.fillMaxWidth(), height = 4.dp)
                Skeleton(Modifier.fillMaxWidth(0.55f), height = 26.dp)
                repeat(3) { Skeleton(Modifier.fillMaxWidth(), height = 54.dp, radius = 12.dp) }
            }
            section == null -> EmptyState(stringResource(R.string.empty_matches_title))
            else -> AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn()) togetherWith
                        (slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut())
                },
                label = "step",
            ) { step ->
                val current = sections[step]
                Column {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            Text(stringResource(R.string.step_of, step + 1, sections.size), style = HopeTheme.type.footnote, color = c.label2, modifier = Modifier.weight(1f))
                            Text(current.title, style = HopeTheme.type.footnote, color = c.label2)
                        }
                        ProgressBar((step + 1f) / sections.size)
                    }
                    Column(Modifier.padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(current.title, style = HopeTheme.type.title1, color = c.label)
                        current.subtitle?.let { Text(it, style = HopeTheme.type.callout, color = c.label2) }
                    }
                    if (current.id == "seguranca" && !user.isPsychologist) {
                        Banner(stringResource(R.string.cvv_banner), Icons.Rounded.Favorite, BannerTone.TINT, Modifier.padding(top = 16.dp))
                    }
                    current.questions.filter { isVisible(it, state.answers) }.forEachIndexed { i, question ->
                        if (i > 0) HorizontalDivider(color = c.separator)
                        QuestionField(
                            question = question,
                            answers = state.answers,
                            error = state.errors[question.code],
                            scrollIntoView = question.code == state.errors.keys.firstOrNull(),
                            rowLabels = question.rowsFrom?.let { labelsByCode[it] }.orEmpty(),
                            onChange = { onAnswer(question.code, it) },
                        )
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HopeButton(
                            stringResource(R.string.back), onBack, Modifier.weight(1f),
                            variant = ButtonVariant.GRAY, icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft, enabled = step > 0,
                        )
                        HopeButton(
                            stringResource(if (isLast) R.string.send_answers else R.string.continue_), onNext, Modifier.weight(1f),
                            trailingIcon = if (isLast) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            loading = state.submitting,
                        )
                    }
                }
            }
        }
    }
}
