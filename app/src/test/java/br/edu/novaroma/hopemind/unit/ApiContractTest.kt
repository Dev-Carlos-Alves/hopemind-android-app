package br.edu.novaroma.hopemind.unit

import br.edu.novaroma.hopemind.data.remote.AppJson
import br.edu.novaroma.hopemind.domain.model.MatchResponse
import br.edu.novaroma.hopemind.domain.model.SafetyLevel
import br.edu.novaroma.hopemind.domain.model.SubmitResponse
import br.edu.novaroma.hopemind.presentation.screen.matches.upcomingDays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Contrato entre a API NestJS e o app (documento de Integração, seção 4.2): o JSON abaixo tem o
 * mesmo formato que GET /api/matches e POST /api/triage/submissions devolvem.
 */
class ApiContractTest {

    private val matchesJson = """
        {
          "algorithmVersion": "hm-match-1.1.0",
          "questionnaireVersion": "patient-2026.10",
          "safety": { "level": "NONE", "resources": [] },
          "evaluated": 3,
          "modality": "online",
          "location": { "neighborhood": "Graças", "city": "Recife" },
          "excluded": { "modalidade": 1 },
          "results": [{
            "psychologistId": 7,
            "score": 0.812,
            "band": "alta",
            "components": { "demanda": 1, "estilo": 0.75, "localizacao": 1, "experiencia": 0.8, "relacao": 0.7, "disponibilidade": 1, "preferencias": 0.5 },
            "distanceKm": 1.04,
            "reasons": ["Atende ansiedade, sua prioridade"],
            "considerations": [],
            "psychologist": {
              "id": 7, "name": "Dra. Sintética", "crp": "CRP 02/12345", "specialty": "Psicologia clínica",
              "approaches": ["Terapia Cognitivo-Comportamental"], "biography": null, "sessionFee": "150",
              "modalities": ["online"], "neighborhood": "Espinheiro", "city": "Recife",
              "contactLink": "https://wa.me/5581999990000",
              "campoNovoDaApi": "ignorado"
            }
          }]
        }
    """.trimIndent()

    @Test
    fun `le a resposta de matches da API`() {
        val r = AppJson.decodeFromString(MatchResponse.serializer(), matchesJson)
        assertEquals("hm-match-1.1.0", r.algorithmVersion)
        assertEquals(SafetyLevel.NONE, r.safety.level)
        assertEquals(mapOf("modalidade" to 1), r.excluded)
        val m = r.results.single()
        assertEquals("alta", m.band)
        assertEquals(1.04, m.distanceKm!!, 1e-9)
        assertEquals("150", m.psychologist.sessionFee)
        assertNull(m.psychologist.biography)
        assertEquals("https://wa.me/5581999990000", m.psychologist.contactLink)
        assertEquals(7, m.components.size)
    }

    @Test
    fun `aceita paciente sem CEP (location null) e risco com contatos de ajuda`() {
        val json = """
            {"message":"Respostas salvas.","submissionId":5,"questionnaireVersion":"v1",
             "safety":{"level":"IMMEDIATE","resources":[{"name":"CVV","detail":"Ligue 188","href":"tel:188"},{"name":"UPA","detail":"Procure a urgência"}]}}
        """.trimIndent()
        val s = AppJson.decodeFromString(SubmitResponse.serializer(), json)
        assertEquals(SafetyLevel.IMMEDIATE, s.safety.level)
        assertEquals("tel:188", s.safety.resources.first().href)
        assertNull(s.safety.resources.last().href)

        val noAddress = AppJson.decodeFromString(MatchResponse.serializer(), matchesJson.replace("\"location\": { \"neighborhood\": \"Graças\", \"city\": \"Recife\" }", "\"location\": null"))
        assertNull(noAddress.location)
    }

    @Test
    fun `agenda oferece os proximos 14 dias uteis, sem domingos`() {
        val saturday = LocalDate.of(2026, 11, 14)
        val days = upcomingDays(saturday)
        assertEquals(14, days.size)
        assertEquals(LocalDate.of(2026, 11, 16), days.first()) // pula o domingo 15/11
        assertFalse(days.any { it.dayOfWeek == DayOfWeek.SUNDAY })
        assertTrue(days.all { it.isAfter(saturday) })
    }
}
