package br.edu.novaroma.hopemind.unit

import br.edu.novaroma.hopemind.domain.model.Question
import br.edu.novaroma.hopemind.domain.model.QuestionType
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import br.edu.novaroma.hopemind.domain.model.ScaleDef
import br.edu.novaroma.hopemind.domain.model.Section
import br.edu.novaroma.hopemind.domain.model.ShowIf
import br.edu.novaroma.hopemind.domain.model.UserType
import br.edu.novaroma.hopemind.presentation.screen.triage.intValue
import br.edu.novaroma.hopemind.presentation.screen.triage.isAnswered
import br.edu.novaroma.hopemind.presentation.screen.triage.isVisible
import br.edu.novaroma.hopemind.presentation.screen.triage.listValue
import br.edu.novaroma.hopemind.presentation.screen.triage.matrixValue
import br.edu.novaroma.hopemind.presentation.screen.triage.stringValue
import br.edu.novaroma.hopemind.presentation.screen.triage.visibleAnswers
import kotlinx.serialization.json.JsonNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regras do questionário no app — paridade com o site (frontend/src/services/questionnaire.ts). */
class AnswersTest {

    private fun q(code: String, type: QuestionType = QuestionType.SINGLE, showIf: ShowIf? = null, rowsFrom: String? = null, scale: ScaleDef? = null) =
        Question(code = code, type = type, prompt = "?", required = true, usage = "alta", showIf = showIf, rowsFrom = rowsFrom, scale = scale)

    @Test
    fun `pergunta condicional so aparece quando a condicao e atendida`() {
        val onlyInPerson = q("P24", showIf = ShowIf("P20", inValues = listOf("presencial", "tanto_faz")))
        assertFalse(isVisible(onlyInPerson, mapOf("P20" to stringValue("online"))))
        assertTrue(isVisible(onlyInPerson, mapOf("P20" to stringValue("presencial"))))
        val notNone = q("S04", showIf = ShowIf("S03", notIn = listOf("nenhuma")))
        assertFalse(isVisible(notNone, emptyMap()))
        assertTrue(isVisible(notNone, mapOf("S03" to listValue(listOf("ansiedade")))))
    }

    @Test
    fun `decide quando a resposta esta completa`() {
        assertFalse(isAnswered(q("A"), emptyMap()))
        assertFalse(isAnswered(q("A", QuestionType.TEXT), mapOf("A" to stringValue("   "))))
        assertFalse(isAnswered(q("A", QuestionType.MULTI), mapOf("A" to listValue(emptyList()))))
        assertTrue(isAnswered(q("A", QuestionType.MULTI), mapOf("A" to listValue(listOf("x")))))
        assertTrue(isAnswered(q("A", QuestionType.SCALE, scale = ScaleDef("", "", allowNotApplicable = true)), mapOf("A" to JsonNull)))
        assertTrue(isAnswered(q("A", QuestionType.SCALE), mapOf("A" to intValue(3))))

        val matrix = q("S04", QuestionType.MATRIX, rowsFrom = "S03")
        val rows = "S03" to listValue(listOf("ansiedade", "sono"))
        assertFalse(isAnswered(matrix, mapOf(rows, "S04" to matrixValue(mapOf("ansiedade" to 5)))))
        assertTrue(isAnswered(matrix, mapOf(rows, "S04" to matrixValue(mapOf("ansiedade" to 5, "sono" to 2)))))
    }

    @Test
    fun `envia so as respostas de perguntas visiveis`() {
        val questionnaire = Questionnaire(
            UserType.PATIENT, "test",
            listOf(Section("a", "A", questions = listOf(q("P20"), q("P24", showIf = ShowIf("P20", inValues = listOf("presencial")))))),
        )
        val online = mapOf("P20" to stringValue("online"), "P24" to stringValue("perto"), "LIXO" to stringValue("x"))
        assertEquals(mapOf("P20" to stringValue("online")), visibleAnswers(questionnaire, online))
        val inPerson = mapOf("P20" to stringValue("presencial"), "P24" to stringValue("perto"))
        assertEquals(inPerson, visibleAnswers(questionnaire, inPerson))
    }
}
