package br.edu.novaroma.hopemind.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.novaroma.hopemind.HopeMindApplication
import br.edu.novaroma.hopemind.data.AppContainer

/** Cria um ViewModel recebendo o [AppContainer] do app (injeção de dependência manual). */
@Composable
inline fun <reified VM : ViewModel> hopeViewModel(noinline create: (AppContainer) -> VM): VM {
    val app = LocalContext.current.applicationContext as HopeMindApplication
    return viewModel(factory = viewModelFactory { initializer { create(app.container) } })
}

@Composable
fun appContainer(): AppContainer = (LocalContext.current.applicationContext as HopeMindApplication).container
