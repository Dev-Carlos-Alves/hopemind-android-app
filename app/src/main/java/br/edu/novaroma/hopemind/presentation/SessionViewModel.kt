package br.edu.novaroma.hopemind.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.novaroma.hopemind.data.AppContainer
import br.edu.novaroma.hopemind.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Estado de sessão compartilhado por todas as telas (vive na Activity). */
class SessionViewModel(private val container: AppContainer) : ViewModel() {
    val user: StateFlow<User?> = container.auth.user

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    init {
        viewModelScope.launch {
            container.auth.restoreSession()
            _ready.value = true
        }
    }

    fun refreshUser() = viewModelScope.launch { container.auth.refreshUser() }

    fun logout(onDone: () -> Unit) = viewModelScope.launch {
        container.auth.logout()
        onDone()
    }
}
