package br.edu.novaroma.hopemind

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import br.edu.novaroma.hopemind.data.Prefs
import br.edu.novaroma.hopemind.data.remote.PersistentCookieJar
import br.edu.novaroma.hopemind.presentation.screen.KeepSignedInDialog
import br.edu.novaroma.hopemind.ui.theme.HopeMindTheme
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import com.github.takahirom.roborazzi.captureScreenRoboImage
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Manter conectado" (pedido do professor de mobile): depois do login o app pergunta se a sessão
 * deve ficar salva no aparelho; com "sim", o app reabre direto no perfil.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "pt-rBR-w393dp-h852dp-xxhdpi")
class KeepSignedInTest {
    @get:Rule val compose = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val api = "https://hopemind-api.onrender.com/api/auth/login".toHttpUrl()
    private val session = listOf(
        Cookie.parse(api, "access_token=abc; Path=/; Max-Age=900; HttpOnly")!!,
        Cookie.parse(api, "refresh_token=def; Path=/; Max-Age=2592000; HttpOnly")!!,
    )

    @Before fun clean() {
        context.getSharedPreferences("hopemind.cookies", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("hopemind.prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    /** Simula o app sendo fechado e aberto de novo: um CookieJar novo lendo o que ficou no aparelho. */
    private fun reopen(persist: Boolean) = PersistentCookieJar(context, persist).loadForRequest("https://hopemind-api.onrender.com/api/auth/me".toHttpUrl())

    @Test fun `com manter conectado a sessao sobrevive ao fechar o app`() {
        PersistentCookieJar(context, persist = true).saveFromResponse(api, session)
        assertEquals(setOf("access_token", "refresh_token"), reopen(persist = true).map { it.name }.toSet())
    }

    @Test fun `sem manter conectado a sessao fica so na memoria`() {
        val jar = PersistentCookieJar(context, persist = false)
        jar.saveFromResponse(api, session)
        assertEquals(2, jar.loadForRequest(api).size) // o app aberto continua logado
        assertTrue(reopen(persist = false).isEmpty())
        assertTrue(reopen(persist = true).isEmpty()) // nada foi gravado no aparelho
    }

    @Test fun `responder sim depois do login grava a sessao que estava so na memoria`() {
        val jar = PersistentCookieJar(context, persist = false) // login: ainda sem resposta
        jar.saveFromResponse(api, session)
        jar.persist = true // "Manter conectado"
        assertEquals(2, reopen(persist = true).size)
    }

    @Test fun `desligar em Ajustes apaga a sessao salva sem deslogar o app aberto`() {
        val jar = PersistentCookieJar(context, persist = true)
        jar.saveFromResponse(api, session)
        jar.persist = false
        assertEquals(2, jar.loadForRequest(api).size)
        assertTrue(reopen(persist = true).isEmpty())
    }

    @Test fun `sair da conta apaga tudo`() {
        val jar = PersistentCookieJar(context, persist = true)
        jar.saveFromResponse(api, session)
        jar.clear()
        assertTrue(jar.loadForRequest(api).isEmpty())
        assertTrue(reopen(persist = true).isEmpty())
    }

    @Test fun `a escolha fica guardada nas preferencias`() {
        val prefs = Prefs(context)
        assertTrue(prefs.keepSignedIn) // instalações antigas já guardavam a sessão
        prefs.keepSignedIn = false
        assertFalse(Prefs(context).keepSignedIn)
        prefs.keepSignedIn = true
        assertTrue(Prefs(context).keepSignedIn)
    }

    @Test fun `dialogo devolve a escolha da pessoa`() {
        var choice: Boolean? = null
        compose.setContent { HopeMindTheme(ThemeMode.LIGHT) { KeepSignedInDialog("Ana Souza") { choice = it } } }
        compose.onNodeWithText("Manter conectado?").assertExists()
        captureScreenRoboImage("build/outputs/roborazzi/23_manter_conectado.png")
        compose.onNodeWithText("Manter conectado").performClick()
        assertEquals(true, choice)
    }

    @Test fun `agora nao recusa`() {
        var choice: Boolean? = null
        compose.setContent { HopeMindTheme(ThemeMode.LIGHT) { KeepSignedInDialog("Ana Souza") { choice = it } } }
        compose.onNodeWithText("Agora não").performClick()
        assertEquals(false, choice)
    }
}
