package br.edu.novaroma.hopemind.presentation.screen.triage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Question
import br.edu.novaroma.hopemind.domain.model.QuestionType
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.presentation.components.OptionRow
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

private val DAYS = listOf("seg" to R.string.day_seg, "ter" to R.string.day_ter, "qua" to R.string.day_qua, "qui" to R.string.day_qui, "sex" to R.string.day_sex, "sab" to R.string.day_sab, "dom" to R.string.day_dom)
private val PERIOD_COLUMN = 68.dp
private val PERIODS = listOf(Triple("manha", R.string.period_manha, "8–12h"), Triple("tarde", R.string.period_tarde, "12–18h"), Triple("noite", R.string.period_noite, "18–22h"))

/** Renderiza uma pergunta do questionário conforme o tipo definido pela API. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionField(
    question: Question,
    answers: Answers,
    error: Boolean?,
    rowLabels: Map<String, String>,
    onChange: (JsonElement?) -> Unit,
    scrollIntoView: Boolean = false,
) {
    val c = HopeTheme.colors
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(scrollIntoView) { if (scrollIntoView) requester.bringIntoView() }
    Column(
        Modifier.fillMaxWidth().bringIntoViewRequester(requester).padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                buildAnnotatedString {
                    append(question.prompt)
                    if (!question.required) {
                        withStyle(SpanStyle(color = c.label3, fontWeight = FontWeight.Medium, fontSize = HopeTheme.type.caption.fontSize)) {
                            append("  " + stringResource(R.string.optional))
                        }
                    }
                },
                style = HopeTheme.type.headline,
                color = if (error != null) c.danger else c.label,
            )
            question.help?.let { Text(it, style = HopeTheme.type.subhead, color = c.label2) }
        }

        when (question.type) {
            QuestionType.SINGLE -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val selected = answers.string(question.code)
                question.options.forEach { o ->
                    OptionRow(o.label, checked = selected == o.value, onClick = { onChange(stringValue(o.value)) })
                }
            }

            QuestionType.MULTI -> MultiChips(question, answers.list(question.code)) { onChange(listValue(it)) }

            QuestionType.SCALE -> ScaleSelector(
                value = answers.int(question.code),
                notApplicable = answers[question.code] is JsonNull,
                minLabel = question.scale?.minLabel.orEmpty(),
                maxLabel = question.scale?.maxLabel.orEmpty(),
                allowNotApplicable = question.scale?.allowNotApplicable == true,
                onChange = { onChange(intValue(it)) },
            )

            QuestionType.SCHEDULE -> ScheduleGrid(answers.list(question.code).toSet()) { onChange(listValue(it.toList())) }

            QuestionType.TEXT -> {
                val long = (question.maxLength ?: 0) > 120
                HopeTextField(
                    value = answers.string(question.code).orEmpty(),
                    onValueChange = { v -> onChange(if (v.isEmpty()) null else stringValue(v.take(question.maxLength ?: 1000))) },
                    label = stringResource(if (long) R.string.text_placeholder else R.string.city_label),
                    singleLine = !long,
                    minLines = if (long) 4 else 1,
                )
            }

            QuestionType.MATRIX -> {
                val rows = answers.list(question.rowsFrom.orEmpty())
                val levels = answers.matrix(question.code)
                if (rows.isEmpty()) {
                    Text(stringResource(R.string.matrix_empty), style = HopeTheme.type.callout, color = c.label2)
                } else {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).border(1.dp, c.separator, RoundedCornerShape(Radius.md))) {
                        rows.forEachIndexed { i, row ->
                            if (i > 0) HorizontalDivider(color = c.separator)
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(rowLabels[row] ?: row, style = HopeTheme.type.callout.copy(fontWeight = FontWeight.SemiBold), color = c.label)
                                ScaleSelector(
                                    value = levels[row],
                                    notApplicable = false,
                                    minLabel = question.scale?.minLabel.orEmpty(),
                                    maxLabel = question.scale?.maxLabel.orEmpty(),
                                    showLabels = i == 0,
                                    onChange = { v -> if (v != null) onChange(matrixValue(levels + (row to v))) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (error != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = c.danger, modifier = Modifier.size(15.dp))
                Text(
                    stringResource(if (error) R.string.matrix_required else R.string.answer_required),
                    style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.Medium),
                    color = c.danger,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiChips(question: Question, selected: List<String>, onChange: (List<String>) -> Unit) {
    val c = HopeTheme.colors
    val max = question.maxSelections
    val atLimit = max != null && selected.size >= max
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEach { o ->
                val on = o.value in selected
                val exclusive = o.value in question.exclusive
                val enabled = on || !atLimit || exclusive
                Row(
                    Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (on) c.tint else c.bgElevated)
                        .border(1.dp, if (on) c.tint else c.separatorStrong, RoundedCornerShape(50))
                        .clickable(enabled = enabled, role = Role.Checkbox) {
                            val next = when {
                                on -> selected - o.value
                                exclusive -> listOf(o.value)
                                else -> (selected.filterNot { it in question.exclusive }) + o.value
                            }
                            if (!on && max != null && next.size > max) return@clickable
                            onChange(next)
                        }
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (on) Icon(Icons.Rounded.Check, null, tint = c.onTint, modifier = Modifier.size(16.dp))
                    Text(
                        o.label,
                        style = HopeTheme.type.callout.copy(fontWeight = FontWeight.Medium),
                        color = when { on -> c.onTint; enabled -> c.label; else -> c.label.copy(alpha = 0.35f) },
                    )
                }
            }
        }
        if (max != null) Text(stringResource(R.string.selected_of, selected.size, max), style = HopeTheme.type.footnote, color = c.label2)
    }
}

@Composable
fun ScaleSelector(
    value: Int?,
    notApplicable: Boolean,
    minLabel: String,
    maxLabel: String,
    onChange: (Int?) -> Unit,
    allowNotApplicable: Boolean = false,
    showLabels: Boolean = true,
) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { n ->
                val on = value == n
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(Radius.md))
                        .background(if (on) c.tint else c.fillSoft)
                        .border(1.dp, if (on) c.tint else c.separator, RoundedCornerShape(Radius.md))
                        .clickable(role = Role.RadioButton) { onChange(n) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(n.toString(), style = HopeTheme.type.headline, color = if (on) c.onTint else c.label)
                }
            }
        }
        if (showLabels) {
            Row {
                Text(minLabel, style = HopeTheme.type.footnote, color = c.label2, modifier = Modifier.weight(1f))
                Text(maxLabel, style = HopeTheme.type.footnote, color = c.label2)
            }
        }
        if (allowNotApplicable) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (notApplicable) c.tint else c.bgElevated)
                    .border(1.dp, if (notApplicable) c.tint else c.separatorStrong, RoundedCornerShape(50))
                    .clickable { onChange(null) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(stringResource(R.string.not_applicable), style = HopeTheme.type.subhead, color = if (notApplicable) c.onTint else c.label)
            }
        }
    }
}

@Composable
private fun ScheduleGrid(selected: Set<String>, onChange: (Set<String>) -> Unit) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.width(PERIOD_COLUMN))
            DAYS.forEach { (_, label) ->
                Text(
                    stringResource(label),
                    style = HopeTheme.type.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = c.label2,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        PERIODS.forEach { (period, label, hint) ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.width(PERIOD_COLUMN)) {
                    Text(stringResource(label), style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.SemiBold), color = c.label, maxLines = 1, softWrap = false)
                    Text(hint, style = HopeTheme.type.caption, color = c.label3)
                }
                DAYS.forEach { (day, _) ->
                    val slot = "$day-$period"
                    val on = slot in selected
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(if (on) c.tint else c.fillSoft)
                            .border(1.dp, if (on) c.tint else c.separator, RoundedCornerShape(Radius.sm))
                            .clickable(role = Role.Checkbox) { onChange(if (on) selected - slot else selected + slot) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) Icon(Icons.Rounded.Check, null, tint = c.onTint, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
