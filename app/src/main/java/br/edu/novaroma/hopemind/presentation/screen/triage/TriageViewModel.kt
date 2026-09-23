package br.edu.novaroma.hopemind.presentation.screen.triage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.novaroma.hopemind.data.AppContainer
import br.edu.novaroma.hopemind.domain.model.QuestionType
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import br.edu.novaroma.hopemind.domain.model.Safety
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement

data class TriageState(
    val loading: Boolean = true,
    val questionnaire: Questionnaire? = null,
    val answers: Answers = emptyMap(),
    val step: Int = 0,
    /** Código da pergunta -> tipo de erro (true = matriz incompleta). */
    val errors: Map<String, Boolean> = emptyMap(),
    val submitting: Boolean = false,
    val error: String? = null,
)

sealed interface SubmitOutcome {
    data object Saved : SubmitOutcome
    data class NeedsSupport(val safety: Safety) : SubmitOutcome
}

class TriageViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(TriageState())
    val state: StateFlow<TriageState> = _state

    init {
        viewModelScope.launch {
            val q = container.triage.questionnaire()
            val mine = container.triage.mySubmission().getOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    questionnaire = q.getOrNull(),
                    answers = mine?.answers.orEmpty(),
                    error = q.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun setAnswer(code: String, value: JsonElement?) = _state.update {
        val next = if (value == null) it.answers - code else it.answers + (code to value)
        it.copy(answers = next, errors = it.errors - code)
    }

    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), errors = emptyMap()) }

    /** Valida a etapa atual; avança ou, na última, envia. Retorna o código da 1ª pergunta com erro. */
    fun next(onSubmitted: (SubmitOutcome) -> Unit): String? {
        val s = _state.value
        val section = s.questionnaire?.sections?.getOrNull(s.step) ?: return null
        val errors = section.questions
            .filter { it.required && isVisible(it, s.answers) && !isAnswered(it, s.answers) }
            .associate { it.code to (it.type == QuestionType.MATRIX) }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return errors.keys.first()
        }
        val isLast = s.step == s.questionnaire.sections.lastIndex
        if (!isLast) {
            _state.update { it.copy(step = it.step + 1, errors = emptyMap()) }
            return null
        }
        submit(onSubmitted)
        return null
    }

    private fun submit(onSubmitted: (SubmitOutcome) -> Unit) {
        val s = _state.value
        val q = s.questionnaire ?: return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            container.triage.submit(visibleAnswers(q, s.answers))
                .onSuccess { res ->
                    container.auth.refreshUser()
                    container.lastSafety = res.safety
                    _state.update { it.copy(submitting = false) }
                    val level = res.safety.level
                    onSubmitted(
                        if (level == br.edu.novaroma.hopemind.domain.model.SafetyLevel.IMMEDIATE ||
                            level == br.edu.novaroma.hopemind.domain.model.SafetyLevel.ELEVATED
                        ) SubmitOutcome.NeedsSupport(res.safety) else SubmitOutcome.Saved,
                    )
                }
                .onFailure { e -> _state.update { it.copy(submitting = false, error = e.message) } }
        }
    }

    fun consumeError() = _state.update { it.copy(error = null) }

    /** Volta para a 1ª etapa (depois de enviar, o questionário fica pronto para ser revisado de novo). */
    fun restart() = _state.update { it.copy(step = 0, errors = emptyMap()) }
}
