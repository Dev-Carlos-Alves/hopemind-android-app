package br.edu.novaroma.hopemind.data.remote

import br.edu.novaroma.hopemind.domain.model.AddressRequest
import br.edu.novaroma.hopemind.domain.model.Appointment
import br.edu.novaroma.hopemind.domain.model.CepAddress
import br.edu.novaroma.hopemind.domain.model.BookRequest
import br.edu.novaroma.hopemind.domain.model.LoginRequest
import br.edu.novaroma.hopemind.domain.model.LoginResponse
import br.edu.novaroma.hopemind.domain.model.MatchResponse
import br.edu.novaroma.hopemind.domain.model.MessageResponse
import br.edu.novaroma.hopemind.domain.model.Profile
import br.edu.novaroma.hopemind.domain.model.ProfileAddress
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import br.edu.novaroma.hopemind.domain.model.RegisterRequest
import br.edu.novaroma.hopemind.domain.model.SubmitRequest
import br.edu.novaroma.hopemind.domain.model.SubmitResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Mesmas rotas usadas pelo site (ver docs/projeto/ARQUITETURA-WEB.md do hopemind). */
interface HopeMindApi {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): MessageResponse
    @POST("auth/logout") suspend fun logout(): MessageResponse
    @GET("auth/me") suspend fun me(): Profile
    @PATCH("auth/me/address") suspend fun updateAddress(@Body body: AddressRequest): ProfileAddress

    @GET("geo/cep/{cep}") suspend fun cep(@Path("cep") cep: String): CepAddress

    @GET("triage/questionnaire") suspend fun questionnaire(): Questionnaire

    /** A API devolve `null` quando ainda não há respostas, por isso lemos o corpo cru. */
    @GET("triage/me") suspend fun mySubmission(): ResponseBody
    @POST("triage/submissions") suspend fun submit(@Body body: SubmitRequest): SubmitResponse

    @GET("matches") suspend fun matches(): MatchResponse

    @GET("sessoes") suspend fun appointments(): List<Appointment>
    @POST("sessoes/agendar") suspend fun book(@Body body: BookRequest): MessageResponse
}
