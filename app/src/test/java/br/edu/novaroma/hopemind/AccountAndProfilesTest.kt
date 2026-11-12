package br.edu.novaroma.hopemind

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.edu.novaroma.hopemind.data.remote.AppJson
import br.edu.novaroma.hopemind.domain.model.Appointment
import br.edu.novaroma.hopemind.domain.model.MatchPsychologist
import br.edu.novaroma.hopemind.domain.model.PatientCard
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.ToastState
import br.edu.novaroma.hopemind.presentation.screen.ForgotPasswordScreen
import br.edu.novaroma.hopemind.presentation.screen.PatientProfileSheet
import br.edu.novaroma.hopemind.presentation.screen.PsychologistProfileSheet
import br.edu.novaroma.hopemind.presentation.screen.resetFormError
import br.edu.novaroma.hopemind.ui.theme.HopeMindTheme
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import androidx.compose.ui.test.onRoot
import androidx.compose.runtime.Composable
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Login por e-mail ou CPF, "esqueci minha senha" e perfis detalhados (pedidos de 12/11/2026). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "pt-rBR-w393dp-h852dp-xxhdpi")
class AccountAndProfilesTest {
    @get:Rule val compose = createComposeRule()

    private fun show(content: @Composable () -> Unit) = compose.setContent {
        HopeMindTheme(ThemeMode.LIGHT) { CompositionLocalProvider(LocalToast provides remember { ToastState() }) { content() } }
    }

    private val psychologist = MatchPsychologist(
        7, "Rafael Moura", "CRP 02/12345", "Psicologia clínica", listOf("Terapia Cognitivo-Comportamental", "ACT"),
        "Atendo adultos com ansiedade e estresse no trabalho, com uma abordagem estruturada e acolhedora.", "150.00",
        listOf("online", "presencial"), "Espinheiro", "Recife", "https://wa.me/5581988887777",
        therapeuticApproach = "TCC", gender = "Masculino", experience = "5 a 10 anos", audiences = listOf("Adultos"),
        demands = listOf("Ansiedade", "Trabalho / carreira", "Sono"), experiencedWith = listOf("Mais reservado", "Orientado a objetivos"),
    )

    private val patient = PatientCard(
        "Ana Souza", 28, "Feminino", "Ando muito ansiosa com o trabalho e quero aprender a lidar melhor com isso.",
        listOf("Ansiedade", "Trabalho / carreira"), listOf("Sono"), "Online", "Graças", "Recife",
    )

    @Test fun `regras do formulario de nova senha`() {
        assertEquals(R.string.forgot_error_code, resetFormError("123", "SenhaForte1", "SenhaForte1"))
        assertEquals(R.string.forgot_error_password, resetFormError("123456", "curta", "curta"))
        assertEquals(R.string.forgot_error_confirm, resetFormError("123456", "SenhaForte1", "SenhaForte2"))
        assertNull(resetFormError("123 456", "SenhaForte1", "SenhaForte1"))
    }

    @Test fun `le o perfil do paciente e do psicologo nas sessoes`() {
        val json = """
            [{"id":1,"dataHora":"2030-01-10T19:00:00.000Z","status":"SCHEDULED","psicologoNome":"Rafael Moura","pacienteNome":"Ana Souza",
              "especialidade":"Clínica","crp":"CRP 02/12345",
              "paciente":{"nome":"Ana Souza","idade":28,"genero":"Feminino","sobre":"Ando ansiosa.","prioridades":["Ansiedade"],"outrosTemas":[],"modalidade":"Online","bairro":"Graças","cidade":"Recife"}},
             {"id":2,"dataHora":"2030-01-11T19:00:00.000Z","status":"SCHEDULED","psicologoNome":"Rafael Moura","pacienteNome":"Ana Souza",
              "especialidade":"Clínica","crp":"CRP 02/12345",
              "psicologo":{"id":7,"nome":"Rafael Moura","crp":"CRP 02/12345","especialidade":"Clínica","abordagem":"TCC","biografia":null,"valorSessao":"150","contato":null,"bairro":"Espinheiro","cidade":"Recife"}}]
        """.trimIndent()
        val list = AppJson.decodeFromString(ListSerializer(Appointment.serializer()), json)
        assertEquals("Ando ansiosa.", list[0].paciente!!.sobre)
        assertNull(list[0].psicologo)
        assertEquals("TCC", list[1].psicologo!!.abordagem)
        assertNull(list[1].paciente)
    }

    @Test fun `perfil do psicologo mostra tudo o que ele contou`() {
        show { PsychologistProfileSheet(psychologist, 1.1, onBook = {}, onDismiss = {}) }
        compose.onNodeWithText("Atendo adultos com ansiedade", substring = true).assertExists()
        compose.onNodeWithText("5 a 10 anos").assertExists()
        compose.onNodeWithText("Mais reservado").assertExists()
        captureScreenRoboImage("build/outputs/roborazzi/27_perfil_psicologo.png")
    }

    @Test fun `perfil do paciente mostra a descricao que ele deu de si`() {
        show { PatientProfileSheet(patient, onDismiss = {}) }
        compose.onNodeWithText("Ando muito ansiosa com o trabalho", substring = true).assertExists()
        compose.onNodeWithText("28 anos", substring = true).assertExists()
        captureScreenRoboImage("build/outputs/roborazzi/28_perfil_paciente.png")
    }

    @Test fun `esqueci minha senha comeca pedindo e-mail ou CPF`() {
        show { ForgotPasswordScreen(onBack = {}, onDone = {}, initialLogin = "529.982.247-25") }
        compose.onNodeWithText("Enviar código").assertExists()
        compose.onNodeWithText("529.982.247-25").assertExists()
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/29_esqueci_senha.png")
    }
}
