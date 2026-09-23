package br.edu.novaroma.hopemind.presentation.screen.triage

import br.edu.novaroma.hopemind.domain.model.Question
import br.edu.novaroma.hopemind.domain.model.QuestionType
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/*
 * As respostas ficam como JSON (código da pergunta -> valor), no mesmo formato que a API espera.
 * As regras de visibilidade e de "respondida" são as mesmas do site (frontend/src/services/questionnaire.ts).
 */
typealias Answers = Map<String, JsonElement>

fun Answers.string(code: String): String? = (this[code] as? JsonPrimitive)?.takeIf { it.isString }?.content
fun Answers.list(code: String): List<String> = (this[code] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }.orEmpty()
fun Answers.int(code: String): Int? = (this[code] as? JsonPrimitive)?.intOrNull
fun Answers.matrix(code: String): Map<String, Int> =
    (this[code] as? JsonObject)?.mapNotNull { (k, v) -> v.jsonPrimitive.intOrNull?.let { k to it } }?.toMap().orEmpty()

fun stringValue(v: String) = JsonPrimitive(v)
fun listValue(v: List<String>) = JsonArray(v.map(::JsonPrimitive))
fun intValue(v: Int?) = if (v == null) JsonNull else JsonPrimitive(v)
fun matrixValue(v: Map<String, Int>) = JsonObject(v.mapValues { JsonPrimitive(it.value) })

fun isVisible(q: Question, answers: Answers): Boolean {
    val rule = q.showIf ?: return true
    val raw = answers[rule.code]
    val values = when (raw) {
        is JsonArray -> raw.mapNotNull { (it as? JsonPrimitive)?.content }
        is JsonPrimitive -> if (raw is JsonNull) emptyList() else listOf(raw.content)
        else -> emptyList()
    }
    rule.inValues?.let { allowed -> return values.any { it in allowed } }
    rule.notIn?.let { blocked -> return values.isNotEmpty() && values.none { it in blocked } }
    return true
}

fun isAnswered(q: Question, answers: Answers): Boolean {
    val v = answers[q.code]
    if (q.type == QuestionType.SCALE && q.scale?.allowNotApplicable == true && v is JsonNull) return true
    return when (v) {
        null, is JsonNull -> false
        is JsonPrimitive -> v.content.isNotBlank()
        is JsonArray -> v.isNotEmpty()
        is JsonObject -> answers.list(q.rowsFrom.orEmpty()).all { it in v.keys }
    }
}

/** Só envia respostas de perguntas visíveis (as ocultas seriam descartadas pela API). */
fun visibleAnswers(q: Questionnaire, answers: Answers): Answers =
    q.sections.flatMap { it.questions }.filter { isVisible(it, answers) }.mapNotNull { question ->
        answers[question.code]?.let { question.code to it }
    }.toMap()
