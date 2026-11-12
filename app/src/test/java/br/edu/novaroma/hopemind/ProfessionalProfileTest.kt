package br.edu.novaroma.hopemind

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import br.edu.novaroma.hopemind.domain.model.MatchItem
import br.edu.novaroma.hopemind.domain.model.MatchPsychologist
import br.edu.novaroma.hopemind.presentation.screen.matches.MatchCard
import br.edu.novaroma.hopemind.domain.model.PsychologistInfo
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.ToastState
import br.edu.novaroma.hopemind.presentation.screen.ProfessionalProfileSheet
import br.edu.novaroma.hopemind.presentation.screen.feeForInput
import br.edu.novaroma.hopemind.presentation.screen.professionalRequest
import br.edu.novaroma.hopemind.ui.theme.HopeMindTheme
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** US-11: o psicólogo edita o perfil profissional no app. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "pt-rBR-w393dp-h852dp-xxhdpi")
class ProfessionalProfileTest {
    @get:Rule val compose = createComposeRule()

    private val profile = PsychologistInfo(1, "CRP 02/12345", "Psicologia clínica", "TCC", "Atendo adultos.", "150.00", "https://wa.me/5581988887777")

    @Test fun `valor da sessao aparece como a pessoa digita`() {
        assertEquals("150", feeForInput("150.00"))
        assertEquals("150,50", feeForInput("150.5"))
        assertEquals("", feeForInput(null))
    }

    @Test fun `monta o pedido para a API com decimal em ponto e textos aparados`() {
        val r = professionalRequest(" Clínica ", "ACT", "  ", "1.500,50", " https://wa.me/55 ")
        assertEquals("Clínica", r.specialty)
        assertEquals("", r.biography) // vazio apaga a biografia
        assertEquals("1500.50", r.sessionFee)
        assertEquals("https://wa.me/55", r.contactLink)
        assertEquals("150.50", professionalRequest("a", "b", "", "150.50", "").sessionFee)
        assertEquals("0", professionalRequest("a", "b", "", "", "").sessionFee)
    }

    @Test fun `formulario abre preenchido com o perfil atual`() {
        compose.setContent {
            HopeMindTheme(ThemeMode.LIGHT) {
                CompositionLocalProvider(LocalToast provides remember { ToastState() }) {
                    ProfessionalProfileSheet(profile, onDismiss = {}, onSaved = {})
                }
            }
        }
        compose.onNodeWithText("Perfil profissional").assertExists()
        compose.onNodeWithText("Psicologia clínica").assertExists()
        compose.onNodeWithText("https://wa.me/5581988887777").assertExists()
        captureScreenRoboImage("build/outputs/roborazzi/26_editar_perfil_profissional.png")
    }

    private fun match(link: String?) = MatchItem(
        7, 0.82, "alta", mapOf("demanda" to 1.0), 1.1, listOf("Atende ansiedade"), emptyList(),
        MatchPsychologist(7, "Rafael Moura", "CRP 02/12345", "Clínica", sessionFee = "150.00", modalities = listOf("online"), contactLink = link),
    )

    @Test fun `card do match mostra o botao de contato quando ha link`() {
        compose.setContent { HopeMindTheme(ThemeMode.LIGHT) { MatchCard(match("https://wa.me/5581"), 1, onBook = {}, onExplain = {}) } }
        compose.onNodeWithContentDescription("Falar com Rafael Moura").assertExists()
    }

    @Test fun `sem link o botao de contato nao aparece`() {
        compose.setContent { HopeMindTheme(ThemeMode.LIGHT) { MatchCard(match(null), 1, onBook = {}, onExplain = {}) } }
        compose.onNodeWithContentDescription("Falar com Rafael Moura").assertDoesNotExist()
    }
}
