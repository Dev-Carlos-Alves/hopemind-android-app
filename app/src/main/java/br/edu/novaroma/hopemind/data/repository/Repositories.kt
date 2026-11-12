package br.edu.novaroma.hopemind.data.repository

import br.edu.novaroma.hopemind.data.AppContainer
import br.edu.novaroma.hopemind.data.apiCall
import br.edu.novaroma.hopemind.data.remote.AppJson
import br.edu.novaroma.hopemind.domain.model.AddressRequest
import br.edu.novaroma.hopemind.domain.model.Appointment
import br.edu.novaroma.hopemind.domain.model.CepAddress
import br.edu.novaroma.hopemind.domain.model.BookRequest
import br.edu.novaroma.hopemind.domain.model.LoginRequest
import br.edu.novaroma.hopemind.domain.model.ProfessionalProfileRequest
import br.edu.novaroma.hopemind.domain.model.PsychologistInfo
import br.edu.novaroma.hopemind.domain.model.RefreshRequest
import br.edu.novaroma.hopemind.domain.model.MatchResponse
import br.edu.novaroma.hopemind.domain.model.MySubmission
import br.edu.novaroma.hopemind.domain.model.Profile
import br.edu.novaroma.hopemind.domain.model.ProfileAddress
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import br.edu.novaroma.hopemind.domain.model.RegisterRequest
import br.edu.novaroma.hopemind.domain.model.SubmitRequest
import br.edu.novaroma.hopemind.domain.model.SubmitResponse
import br.edu.novaroma.hopemind.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement

/** Sessão do usuário logado. `null` = deslogado. */
class AuthRepository(private val container: AppContainer) {
    private val api get() = container.api

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    suspend fun restoreSession(): User? {
        val profile = apiCall { api.me() }.getOrNull()
        _user.value = profile?.toUser()
        return _user.value
    }

    /** A sessão de um novo login fica só na memória até a pessoa responder "manter conectado?". */
    suspend fun login(email: String, password: String): Result<User> {
        container.client.cookies.persist = false
        return apiCall("Não foi possível entrar. Verifique suas credenciais.") { api.login(LoginRequest(email.trim(), password)).user }
            .onSuccess { _user.value = it }
    }

    val keepSignedIn: Boolean get() = container.prefs.keepSignedIn

    /**
     * "Manter conectado": grava (ou apaga) a sessão no aparelho e avisa a API, que passa a
     * renovar a sessão por 30 dias (ou volta aos 7 dias padrão). Falha de rede não desfaz a escolha.
     */
    suspend fun setKeepSignedIn(keep: Boolean) {
        container.prefs.keepSignedIn = keep
        container.client.cookies.persist = keep
        apiCall { api.refresh(RefreshRequest(remember = keep)) }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> =
        apiCall("Não foi possível concluir o cadastro.") { api.register(request); Unit }

    suspend fun refreshUser() {
        apiCall { api.me() }.onSuccess { _user.value = it.toUser() }
    }

    suspend fun profile(): Result<Profile> = apiCall { api.me() }

    /** US-11: o psicólogo atualiza o perfil exibido nas recomendações. */
    suspend fun updateProfessionalProfile(request: ProfessionalProfileRequest): Result<PsychologistInfo> =
        apiCall("Não foi possível salvar o perfil.") { api.updateProfessionalProfile(request) }

    /** O servidor resolve o CEP e calcula as coordenadas; o app só manda o CEP. */
    suspend fun updateAddress(cep: String, number: String?): Result<ProfileAddress> =
        apiCall("Não foi possível salvar o endereço.") { api.updateAddress(AddressRequest(cep.filter(Char::isDigit), number?.takeIf { it.isNotBlank() })) }

    suspend fun lookupCep(cep: String): Result<CepAddress> =
        apiCall("Não encontramos esse CEP.") { api.cep(cep.filter(Char::isDigit)) }

    suspend fun logout() {
        apiCall { api.logout() }
        container.client.cookies.clear()
        container.prefs.keepSignedIn = false
        _user.value = null
    }
}

class TriageRepository(private val container: AppContainer) {
    private val api get() = container.api

    suspend fun questionnaire(): Result<Questionnaire> =
        apiCall("Não foi possível carregar o questionário.") { api.questionnaire() }

    suspend fun mySubmission(): Result<MySubmission?> = apiCall {
        val raw = api.mySubmission().string()
        if (raw.isBlank() || raw == "null") null else AppJson.decodeFromString(MySubmission.serializer(), raw)
    }

    suspend fun submit(answers: Map<String, JsonElement>): Result<SubmitResponse> =
        apiCall("Não foi possível enviar suas respostas.") { api.submit(SubmitRequest(answers)) }
}

class MatchRepository(private val container: AppContainer) {
    suspend fun matches(): Result<MatchResponse> =
        apiCall("Não foi possível carregar suas recomendações.") { container.api.matches() }
}

class AppointmentRepository(private val container: AppContainer) {
    suspend fun list(): Result<List<Appointment>> =
        apiCall("Não foi possível carregar suas sessões.") { container.api.appointments() }

    suspend fun book(psychologistId: Int, isoDateTime: String): Result<Unit> =
        apiCall("Não foi possível agendar a sessão.") { container.api.book(BookRequest(psychologistId, isoDateTime)); Unit }
}
