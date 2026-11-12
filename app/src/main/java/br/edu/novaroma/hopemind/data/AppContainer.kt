package br.edu.novaroma.hopemind.data

import android.content.Context
import br.edu.novaroma.hopemind.data.remote.ApiClient
import br.edu.novaroma.hopemind.data.remote.HopeMindApi
import br.edu.novaroma.hopemind.data.repository.AppointmentRepository
import br.edu.novaroma.hopemind.data.repository.AuthRepository
import br.edu.novaroma.hopemind.data.repository.MatchRepository
import br.edu.novaroma.hopemind.data.repository.TriageRepository
import br.edu.novaroma.hopemind.domain.model.Safety

/**
 * Injeção de dependências manual (sem bibliotecas), simples para fins didáticos.
 * Guarda um único cliente HTTP e os repositórios usados pelas telas.
 */
class AppContainer(private val context: Context) {
    val prefs = Prefs(context)

    var client: ApiClient = ApiClient(context, prefs.apiUrl, prefs.keepSignedIn)
        private set
    val api: HopeMindApi get() = client.api

    val auth = AuthRepository(this)
    val triage = TriageRepository(this)
    val matches = MatchRepository(this)
    val appointments = AppointmentRepository(this)

    /** Resultado do fluxo de segurança do último envio do questionário (lido pela tela de apoio). */
    var lastSafety: Safety? = null

    /** Pedido para abrir Matches com "Meu questionário" expandido (consumido pela tela). */
    var openQuestionnaire: Boolean = false

    /** Troca o endereço da API (útil para testar em celular físico na mesma rede). */
    fun changeServer(url: String?) {
        if (url.isNullOrBlank()) prefs.resetApiUrl() else prefs.apiUrl = url
        client = ApiClient(context, prefs.apiUrl, prefs.keepSignedIn)
    }
}
