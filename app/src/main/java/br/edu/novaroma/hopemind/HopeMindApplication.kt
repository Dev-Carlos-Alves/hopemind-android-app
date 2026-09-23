package br.edu.novaroma.hopemind

import android.app.Application
import br.edu.novaroma.hopemind.data.AppContainer

/** Cria as dependências (API, repositórios, preferências) uma única vez para o app inteiro. */
class HopeMindApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
