package br.edu.novaroma.hopemind.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/*
 * Modelos do domínio. Os nomes dos campos seguem exatamente o JSON da API NestJS do HopeMind
 * (backend/src), para que o app use os mesmos dados e o mesmo banco que o site.
 */

@Serializable
enum class UserType { PATIENT, PSYCHOLOGIST, ADMIN }

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val userType: UserType,
    val patientId: Int? = null,
    val psychologistId: Int? = null,
    val hasTriage: Boolean = false,
) {
    val isPsychologist get() = userType == UserType.PSYCHOLOGIST
}

@Serializable
data class Profile(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val userType: UserType,
    val hasTriage: Boolean = false,
    val patient: PatientInfo? = null,
    val psychologist: PsychologistInfo? = null,
    val cep: String? = null,
    val street: String? = null,
    val addressNumber: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
) {
    val address get() = cep?.let { ProfileAddress(it, street, addressNumber, neighborhood, city, state) }

    fun toUser() = User(id, name, email, userType, patient?.id, psychologist?.id, hasTriage)
}

@Serializable
data class PatientInfo(val id: Int, val mainComplaint: String? = null)

@Serializable
data class PsychologistInfo(
    val id: Int,
    val crp: String,
    val specialty: String,
    val therapeuticApproach: String? = null,
    val biography: String? = null,
    val sessionFee: String? = null,
)

/* ───────────── Endereço (CEP) ───────────── */

/** Endereço salvo no cadastro (resposta de PATCH auth/me/address). */
@Serializable
data class ProfileAddress(
    val cep: String? = null,
    val street: String? = null,
    val addressNumber: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val state: String? = null,
)

/** GET geo/cep/{cep}: ViaCEP/BrasilAPI + centro do bairro no OpenStreetMap. */
@Serializable
data class CepAddress(
    val cep: String,
    val street: String? = null,
    val neighborhood: String? = null,
    val city: String,
    val state: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class AddressRequest(val cep: String, val addressNumber: String? = null)

/* ───────────── Questionário ───────────── */

@Serializable
data class Questionnaire(val audience: UserType, val version: String, val sections: List<Section>)

@Serializable
data class Section(val id: String, val title: String, val subtitle: String? = null, val questions: List<Question>)

@Serializable
data class Question(
    val code: String,
    val type: QuestionType,
    val prompt: String,
    val help: String? = null,
    val required: Boolean,
    val usage: String,
    val options: List<Option> = emptyList(),
    val maxSelections: Int? = null,
    val rowsFrom: String? = null,
    val scale: ScaleDef? = null,
    val maxLength: Int? = null,
    val showIf: ShowIf? = null,
    val exclusive: List<String> = emptyList(),
)

@Serializable
enum class QuestionType {
    @SerialName("single") SINGLE,
    @SerialName("multi") MULTI,
    @SerialName("scale") SCALE,
    @SerialName("schedule") SCHEDULE,
    @SerialName("text") TEXT,
    @SerialName("matrix") MATRIX,
}

@Serializable
data class Option(val value: String, val label: String)

@Serializable
data class ScaleDef(val minLabel: String, val maxLabel: String, val allowNotApplicable: Boolean = false)

@Serializable
data class ShowIf(val code: String, @SerialName("in") val inValues: List<String>? = null, val notIn: List<String>? = null)

@Serializable
data class MySubmission(val questionnaireVersion: String, val answers: JsonObject)

/* ───────────── Segurança ───────────── */

@Serializable
enum class SafetyLevel { NONE, WANTS_TALK, ELEVATED, IMMEDIATE }

@Serializable
data class SupportResource(val name: String, val detail: String, val href: String? = null)

@Serializable
data class Safety(val level: SafetyLevel, val resources: List<SupportResource> = emptyList())

@Serializable
data class SubmitResponse(val message: String, val submissionId: Int, val questionnaireVersion: String, val safety: Safety)

/* ───────────── Match ───────────── */

@Serializable
data class MatchResponse(
    val algorithmVersion: String,
    val questionnaireVersion: String,
    val safety: Safety,
    val evaluated: Int,
    /** P20 do paciente: online, presencial ou tanto_faz. */
    val modality: String? = null,
    /** Bairro/cidade do paciente; null quando ele ainda não informou o CEP. */
    val location: MatchLocation? = null,
    val excluded: Map<String, Int> = emptyMap(),
    val results: List<MatchItem>,
)

@Serializable
data class MatchLocation(val neighborhood: String? = null, val city: String)

@Serializable
data class MatchItem(
    val psychologistId: Int,
    val score: Double,
    val band: String,
    val components: Map<String, Double>,
    /** Distância em linha reta até o consultório (km), quando os dois CEPs têm coordenadas. */
    val distanceKm: Double? = null,
    val reasons: List<String>,
    val considerations: List<String> = emptyList(),
    val psychologist: MatchPsychologist,
)

@Serializable
data class MatchPsychologist(
    val id: Int,
    val name: String,
    val crp: String,
    val specialty: String,
    val approaches: List<String> = emptyList(),
    val biography: String? = null,
    val sessionFee: String? = null,
    val modalities: List<String> = emptyList(),
    val neighborhood: String? = null,
    val city: String? = null,
)

/* ───────────── Sessões ───────────── */

@Serializable
enum class AppointmentStatus { SCHEDULED, COMPLETED, CANCELLED }

@Serializable
data class Appointment(
    val id: Int,
    val dataHora: String,
    val status: AppointmentStatus,
    val psicologoNome: String,
    val pacienteNome: String,
    val especialidade: String,
    val crp: String,
)

/** Dados mínimos para abrir a folha de agendamento. */
data class Bookable(
    val id: Int,
    val name: String,
    val crp: String,
    val specialty: String,
    val biography: String?,
    val sessionFee: String?,
)

/* ───────────── Requisições ───────────── */

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val message: String, val user: User)

@Serializable
data class RegisterRequest(
    val userType: String,
    val name: String,
    val email: String,
    val password: String,
    val cpf: String,
    val phone: String,
    val birthDate: String,
    val gender: String,
    val crp: String? = null,
    val specialty: String? = null,
    val sessionFee: String? = null,
    val cep: String? = null,
    val addressNumber: String? = null,
)

@Serializable
data class SubmitRequest(val answers: Map<String, JsonElement>)

@Serializable
data class BookRequest(val idPsicologo: Int, val dataHora: String)

@Serializable
data class MessageResponse(val message: String = "")
