package br.edu.novaroma.hopemind

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.edu.novaroma.hopemind.domain.model.Appointment
import br.edu.novaroma.hopemind.domain.model.AppointmentStatus
import br.edu.novaroma.hopemind.domain.model.MatchItem
import br.edu.novaroma.hopemind.domain.model.MatchLocation
import br.edu.novaroma.hopemind.domain.model.MatchPsychologist
import br.edu.novaroma.hopemind.domain.model.MatchResponse
import br.edu.novaroma.hopemind.domain.model.Option
import br.edu.novaroma.hopemind.domain.model.PatientInfo
import br.edu.novaroma.hopemind.domain.model.Profile
import br.edu.novaroma.hopemind.domain.model.Question
import br.edu.novaroma.hopemind.domain.model.QuestionType
import br.edu.novaroma.hopemind.domain.model.Questionnaire
import br.edu.novaroma.hopemind.domain.model.Safety
import br.edu.novaroma.hopemind.domain.model.SafetyLevel
import br.edu.novaroma.hopemind.domain.model.ScaleDef
import br.edu.novaroma.hopemind.domain.model.Section
import br.edu.novaroma.hopemind.domain.model.SupportResource
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.domain.model.UserType
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.ToastState
import br.edu.novaroma.hopemind.presentation.navigation.Routes
import br.edu.novaroma.hopemind.presentation.navigation.TabScaffold
import br.edu.novaroma.hopemind.presentation.screen.AboutScreen
import br.edu.novaroma.hopemind.presentation.screen.LoginScreen
import br.edu.novaroma.hopemind.presentation.screen.RegisterScreen
import br.edu.novaroma.hopemind.presentation.screen.SafetyScreen
import br.edu.novaroma.hopemind.presentation.screen.SettingsContent
import br.edu.novaroma.hopemind.presentation.screen.WelcomeScreen
import br.edu.novaroma.hopemind.presentation.screen.home.HomeScreen
import br.edu.novaroma.hopemind.presentation.screen.matches.MatchesContent
import br.edu.novaroma.hopemind.presentation.screen.matches.MatchesState
import br.edu.novaroma.hopemind.presentation.screen.sessions.AppointmentsContent
import br.edu.novaroma.hopemind.presentation.screen.sessions.AppointmentsState
import br.edu.novaroma.hopemind.presentation.screen.triage.TriageContent
import br.edu.novaroma.hopemind.presentation.screen.triage.TriageState
import br.edu.novaroma.hopemind.presentation.screen.triage.intValue
import br.edu.novaroma.hopemind.presentation.screen.triage.listValue
import br.edu.novaroma.hopemind.presentation.screen.triage.stringValue
import br.edu.novaroma.hopemind.ui.theme.HopeMindTheme
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * Gera imagens de cada tela com dados iguais aos do seed da API (sem emulador).
 * As imagens saem em app/build/outputs/roborazzi/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "pt-rBR-w393dp-h852dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private val ana = User(1, "Ana Souza", "paciente@hopemind.local", UserType.PATIENT, patientId = 1, hasTriage = true)
    private val rafael = User(2, "Rafael Moura", "rafael@hopemind.local", UserType.PSYCHOLOGIST, psychologistId = 1, hasTriage = true)

    private fun shot(name: String, mode: ThemeMode = ThemeMode.LIGHT, content: @Composable () -> Unit) {
        compose.setContent {
            HopeMindTheme(mode) {
                CompositionLocalProvider(LocalToast provides remember { ToastState() }) {
                    Box(Modifier.fillMaxSize().background(HopeTheme.colors.bg)) { content() }
                }
            }
        }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    @Composable
    private fun InTabs(user: User, route: String, content: @Composable (PaddingValues) -> Unit) =
        TabScaffold(user, route, onTab = {}, onAvatar = {}, content = content)

    @Test fun t01_welcome() = shot("01_boas_vindas") { WelcomeScreen({}, {}) }
    @Test fun t02_login() = shot("02_login") { LoginScreen({}, {}, {}) }
    @Test fun t03_register() = shot("03_cadastro") { RegisterScreen({}, {}, {}) }
    @Test fun t04_matches() = shot("04_matches") { InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = false, data = matches), triageState(0), it) } }
    @Test fun t05_matchesNoTriage() = shot("05_matches_sem_questionario") {
        val u = ana.copy(hasTriage = false)
        InTabs(u, Routes.MATCHES) { MatchesContent(u, MatchesState(loading = false), triageState(0), it) }
    }
    @Test fun t06_triageStyle() = shot("06_questionario_escalas") { InTabs(ana, Routes.MATCHES) { TriageContent(ana, triageState(step = 0), it, onAnswer = { _, _ -> }, onBack = {}, onNext = {}) } }
    @Test fun t07_triageRoutine() = shot("07_questionario_rotina") { InTabs(ana, Routes.MATCHES) { TriageContent(ana, triageState(step = 1), it, onAnswer = { _, _ -> }, onBack = {}, onNext = {}) } }
    @Test fun t08_sessions() = shot("08_sessoes") { InTabs(ana, Routes.SESSIONS) { AppointmentsContent(ana, AppointmentsState(loading = false, items = appointments), it) {} } }
    @Test fun t09_agendaPsy() = shot("09_agenda_psicologo") { InTabs(rafael, Routes.SESSIONS) { AppointmentsContent(rafael, AppointmentsState(loading = false, items = appointments), it) {} } }
    @Test fun t10_settings() = shot("10_ajustes") { InTabs(ana, Routes.SETTINGS) { SettingsContent(ana, profile, ThemeMode.AUTO, "http://10.0.2.2:3000/api/", it, {}, {}, {}, {}, {}) } }
    @Test fun t11_safety() = shot("11_apoio") { InTabs(ana, Routes.MATCHES) { SafetyScreen(safety, it) {} } }
    @Test fun t12_about() = shot("12_sobre") { AboutScreen {} }
    @Test fun t13_matchesDark() = shot("13_matches_escuro", ThemeMode.DARK) { InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = false, data = matches), triageState(0), it) } }
    @Test fun t14_welcomeDark() = shot("14_boas_vindas_escuro", ThemeMode.DARK) { WelcomeScreen({}, {}) }
    @Test fun t15_loading() = shot("15_carregando") { InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = true), triageState(0), it) } }
    @Test fun t16_matchesQuestionnaireOpen() = shot("16_matches_questionario_aberto") {
        InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = false, data = matches, questionnaireOpen = true), triageState(1), it) }
    }
    @Test fun t17_home() = shot("17_home") { InTabs(ana, Routes.HOME) { HomeScreen(ana, it, {}, {}, {}) } }
    @Test fun t18_homePsyDark() = shot("18_home_psicologo_escuro", ThemeMode.DARK) { InTabs(rafael, Routes.HOME) { HomeScreen(rafael, it, {}, {}, {}) } }
    @Test fun t19_matchesShowAll() = shot("19_matches_ver_mais") {
        InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = false, data = matches, showAll = true), triageState(0), it) }
    }

    // Conta sem CEP (ex.: criada antes do endereço existir): Ajustes abria e o app fechava.
    @Test fun t22_settingsWithoutAddress() = shot("22_ajustes_sem_cep") {
        val noAddress = profile.copy(cep = null, street = null, addressNumber = null, neighborhood = null, city = null, state = null)
        InTabs(ana, Routes.SETTINGS) { SettingsContent(ana, noAddress, ThemeMode.AUTO, "http://10.0.2.2:3000/api/", it, {}, {}, {}, {}, {}) }
    }

    // Telas inteiras (altura grande) para ver todas as seções de uma vez.
    @Config(qualifiers = "pt-rBR-w393dp-h3600dp-mdpi")
    @Test fun t24_settingsKeepSignedIn() = shot("24_ajustes_manter_conectado") {
        InTabs(ana, Routes.SETTINGS) { SettingsContent(ana, profile, ThemeMode.AUTO, "http://10.0.2.2:3000/api/", it, {}, {}, {}, {}, {}, keepSignedIn = true) }
    }
    @Test fun t20_homeFull() = shot("20_home_inteira") { InTabs(ana, Routes.HOME) { HomeScreen(ana, it, {}, {}, {}) } }

    @Config(qualifiers = "pt-rBR-w393dp-h3600dp-mdpi")
    @Test fun t21_matchesFull() = shot("21_matches_inteira") {
        InTabs(ana, Routes.MATCHES) { MatchesContent(ana, MatchesState(loading = false, data = matches, query = "Pina", showAll = true), triageState(0), it) }
    }

    /* ───────────── Dados (espelham o seed e a resposta real da API) ───────────── */

    private fun km(value: Double) = if (value < 1) "menos de 1 km" else String.format(Locale("pt", "BR"), "%.1f km", value)

    // Resultado que a Ana (Graças, Recife) recebe do seed: nome, bairro, distância, só presencial?
    private val seed = listOf(
        listOf("Rafael Moura", "Espinheiro", 1.1, false), listOf("Vinícius Arruda", "Santo Amaro", 2.2, false),
        listOf("Larissa Cavalcanti", "Graças", 0.0, false), listOf("Gabriel Wanderley", "Madalena", 1.3, false),
        listOf("Eduardo Pessoa", "Tamarineira", 1.4, true), listOf("Carolina Menezes", "Poço", 2.8, false),
        listOf("André Farias", "Casa Forte", 2.3, false), listOf("Sofia Guerra", "Jaqueira", 1.0, false),
        listOf("Felipe Andrade", "Derby", 1.4, false), listOf("Lucas Siqueira", "Várzea", 7.5, false),
        listOf("Isabela Moraes", "Boa Vista", 2.4, false), listOf("Mariana Correia", "Pina", 5.7, false),
    )

    private val matches = MatchResponse(
        algorithmVersion = "hm-match-1.1.0",
        questionnaireVersion = "hm-paciente-2026.2",
        safety = Safety(SafetyLevel.NONE),
        evaluated = 26,
        modality = "tanto_faz",
        location = MatchLocation("Graças", "Recife"),
        excluded = mapOf("sem_horario" to 2, "faixa_etaria" to 1, "demanda_nao_atendida" to 1),
        results = seed.mapIndexed { i, row ->
            val name = row[0] as String
            val bairro = row[1] as String
            val distance = row[2] as Double
            val inPersonOnly = row[3] as Boolean
            val score = 0.94 - i * 0.03
            MatchItem(
                psychologistId = i + 1,
                score = score,
                band = if (score >= 0.75) "alta" else if (score >= 0.55) "boa" else "possivel",
                components = mapOf(
                    "demanda" to 1.0 - i * 0.05, "estilo" to 0.9 - i * 0.02, "experiencia" to 0.8, "relacao" to 0.86,
                    "disponibilidade" to 1.0, "localizacao" to if (distance < 1.5) 1.0 else 0.9, "preferencias" to 1.0,
                ),
                distanceKm = distance,
                reasons = listOf(
                    "Experiência declarada em ansiedade e trabalho / carreira",
                    "Estilo de condução parecido com o que você prefere",
                    "Consultório em $bairro, a ${km(distance)} de você",
                ),
                psychologist = MatchPsychologist(
                    i + 1, name, "CRP 02/${12345 + i}", "Terapia Cognitivo-Comportamental", listOf("TCC", "ACT"), null, "150.00",
                    if (inPersonOnly) listOf("presencial") else listOf("online", "presencial"), bairro, "Recife",
                ),
            )
        },
    )

    private val questionnaire = Questionnaire(
        UserType.PATIENT, "hm-paciente-2026.2",
        listOf(
            Section(
                "estilo", "Seu jeito na terapia", "Não existe resposta certa. Marque o quanto cada frase combina com você.",
                listOf(
                    Question("P03", QuestionType.SCALE, "Quão importante é perceber mudanças práticas no dia a dia?", required = true, usage = "media", scale = ScaleDef("Nada importante", "Muito importante")),
                    Question("P04", QuestionType.SCALE, "Prefiro que o psicólogo faça muitas perguntas e me ajude a refletir.", required = false, usage = "media", scale = ScaleDef("Discordo", "Concordo")),
                    Question("P05", QuestionType.SCALE, "Prefiro que o psicólogo seja direto e ofereça orientações práticas.", required = false, usage = "alta", scale = ScaleDef("Discordo", "Concordo")),
                ),
            ),
            Section(
                "logistica", "Rotina e formato", null,
                listOf(
                    Question(
                        "P20", QuestionType.SINGLE, "Qual modalidade você prefere?",
                        help = "Para o presencial, usamos o CEP do seu cadastro para calcular a distância até cada consultório.",
                        required = true, usage = "filtro",
                        options = listOf(Option("online", "Online"), Option("presencial", "Presencial"), Option("tanto_faz", "Tanto faz")),
                    ),
                    Question("P22", QuestionType.SCHEDULE, "Quando você costuma ter disponibilidade?", help = "Toque nos períodos em que você conseguiria fazer uma sessão.", required = true, usage = "filtro"),
                ),
            ),
            Section("seguranca", "Como você está agora", null, emptyList()),
        ),
    )

    private fun triageState(step: Int) = TriageState(
        loading = false,
        questionnaire = questionnaire,
        step = step,
        answers = mapOf(
            "P03" to intValue(5), "P04" to intValue(3),
            "P20" to stringValue("tanto_faz"),
            "P22" to listValue(listOf("ter-noite", "qui-noite", "qua-tarde", "sab-manha")),
        ),
        errors = if (step == 0) mapOf("P05" to false) else emptyMap(),
    )

    private fun at(daysFromNow: Long, hour: Int) =
        LocalDate.now().plusDays(daysFromNow).atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toString()

    private val appointments = listOf(
        Appointment(1, at(1, 14), AppointmentStatus.SCHEDULED, "Rafael Moura", "Ana Souza", "Terapia Cognitivo-Comportamental", "CRP 02/12345"),
        Appointment(2, at(1, 19), AppointmentStatus.SCHEDULED, "Larissa Cavalcanti", "Ana Souza", "Terapia Cognitivo-Comportamental", "CRP 02/18231"),
        Appointment(3, at(6, 18), AppointmentStatus.SCHEDULED, "Rafael Moura", "Ana Souza", "Terapia Cognitivo-Comportamental", "CRP 02/12345"),
    )

    private val profile = Profile(
        1, "Ana Souza", "paciente@hopemind.local", "81987654321", "1998-05-15", "Feminino", UserType.PATIENT, true, PatientInfo(1),
        cep = "52011-050", street = "Rua Amélia", addressNumber = "300", neighborhood = "Graças", city = "Recife", state = "PE",
    )

    private val safety = Safety(
        SafetyLevel.ELEVATED,
        listOf(
            SupportResource("CVV — Centro de Valorização da Vida", "Ligue 188, 24 horas, gratuito. Também por chat em cvv.org.br.", "tel:188"),
            SupportResource("SAMU", "Em emergência médica, ligue 192.", "tel:192"),
            SupportResource("Pronto-socorro / UPA", "Se estiver em perigo agora, procure o serviço de urgência mais próximo."),
        ),
    )
}
