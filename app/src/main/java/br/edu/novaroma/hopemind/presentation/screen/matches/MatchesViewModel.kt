package br.edu.novaroma.hopemind.presentation.screen.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.novaroma.hopemind.data.AppContainer
import br.edu.novaroma.hopemind.domain.model.MatchResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MatchSort { SCORE, DISTANCE }

data class MatchesState(
    val loading: Boolean = true,
    val data: MatchResponse? = null,
    val error: String? = null,
    val query: String = "",
    val sort: MatchSort = MatchSort.SCORE,
    /** "Ver mais": mostra os profissionais depois dos 10 primeiros. */
    val showAll: Boolean = false,
    /** Cartão "Meu questionário" aberto. */
    val questionnaireOpen: Boolean = false,
)

class MatchesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(MatchesState())
    val state: StateFlow<MatchesState> = _state

    fun load(hasTriage: Boolean) {
        if (!hasTriage) {
            _state.update { it.copy(loading = false, data = null) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            container.matches.matches()
                .onSuccess { data -> _state.update { it.copy(loading = false, data = data) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }
    fun setSort(sort: MatchSort) = _state.update { it.copy(sort = sort) }
    fun toggleShowAll() = _state.update { it.copy(showAll = !it.showAll) }
    fun setQuestionnaireOpen(open: Boolean) = _state.update { it.copy(questionnaireOpen = open) }
    fun consumeError() = _state.update { it.copy(error = null) }

    suspend fun book(psychologistId: Int, iso: String) = container.appointments.book(psychologistId, iso)
}
