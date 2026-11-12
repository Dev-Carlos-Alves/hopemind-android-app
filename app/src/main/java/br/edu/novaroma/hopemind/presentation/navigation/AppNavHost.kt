package br.edu.novaroma.hopemind.presentation.navigation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import br.edu.novaroma.hopemind.presentation.screen.ForgotPasswordScreen
import br.edu.novaroma.hopemind.presentation.screen.KeepSignedInDialog
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Safety
import br.edu.novaroma.hopemind.domain.model.SafetyLevel
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.SessionViewModel
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.screen.AboutScreen
import br.edu.novaroma.hopemind.presentation.screen.LoginScreen
import br.edu.novaroma.hopemind.presentation.screen.RegisterScreen
import br.edu.novaroma.hopemind.presentation.screen.SafetyScreen
import br.edu.novaroma.hopemind.presentation.screen.SettingsRoute
import br.edu.novaroma.hopemind.presentation.screen.WelcomeScreen
import br.edu.novaroma.hopemind.presentation.screen.home.HomeScreen
import br.edu.novaroma.hopemind.presentation.screen.matches.MatchesRoute
import br.edu.novaroma.hopemind.presentation.screen.sessions.AppointmentsRoute
import br.edu.novaroma.hopemind.presentation.screen.triage.TriageRoute

/**
 * NavHost concentra todas as rotas do app (mesmo padrão do projeto-modelo).
 *
 * Fluxo:
 *   Splash → Boas-vindas → Login / Cadastro
 *   Logado (paciente):   Home · Sessões · Matches (com o questionário expansível) · Ajustes
 *   Logado (psicólogo):  Home · Agenda · Perfil de atendimento · Ajustes
 *   Questionário com risco → Apoio → Matches
 */
@Composable
fun AppNavHost(session: SessionViewModel) {
    val nav = rememberNavController()
    val user by session.user.collectAsStateWithLifecycle()
    val ready by session.ready.collectAsStateWithLifecycle()
    val container = appContainer()

    fun homeFor(u: User) = when {
        u.hasTriage -> Routes.HOME
        u.isPsychologist -> Routes.TRIAGE
        else -> Routes.MATCHES
    }

    /** Abre Matches já com "Meu questionário" expandido (vindo de Ajustes, por exemplo). */
    fun openQuestionnaire() {
        container.openQuestionnaire = true
        nav.goTab(Routes.MATCHES)
    }

    fun resetTo(route: String) = nav.navigate(route) { popUpTo(nav.graph.id) { inclusive = true } }

    // Depois do login (ou do cadastro), pergunta se a sessão deve ficar salva no aparelho.
    val scope = rememberCoroutineScope()
    var justSignedIn by remember { mutableStateOf<User?>(null) }
    justSignedIn?.let { u ->
        KeepSignedInDialog(u.name) { keep ->
            justSignedIn = null
            scope.launch { container.auth.setKeepSignedIn(keep) }
            resetTo(homeFor(u))
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
    ) {
        composable(Routes.SPLASH) {
            SplashScreen()
            LaunchedEffect(ready) {
                if (ready) resetTo(user?.let(::homeFor) ?: Routes.WELCOME)
            }
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(onLogin = { nav.navigate(Routes.LOGIN) }, onRegister = { nav.navigate(Routes.REGISTER) })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onRegister = { nav.navigate(Routes.REGISTER) { popUpTo(Routes.WELCOME) } },
                onLoggedIn = { justSignedIn = it },
                onForgot = { typed -> nav.navigate(Routes.FORGOT + "?login=" + android.net.Uri.encode(typed)) },
            )
        }

        composable(Routes.FORGOT + "?login={login}") { entry ->
            ForgotPasswordScreen(
                onBack = { nav.popBackStack() },
                onDone = { nav.popBackStack() },
                initialLogin = entry.arguments?.getString("login").orEmpty(),
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onLogin = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.WELCOME) } },
                onRegistered = { justSignedIn = it },
            )
        }

        tab(Routes.HOME, nav, { user }, onLoggedOut = { resetTo(Routes.WELCOME) }) { u, padding ->
            HomeScreen(
                u,
                padding,
                onMatches = { nav.goTab(Routes.MATCHES) },
                onSessions = { nav.goTab(Routes.SESSIONS) },
                onProfile = { nav.goTab(Routes.TRIAGE) },
            )
        }
        tab(Routes.SESSIONS, nav, { user }, onLoggedOut = { resetTo(Routes.WELCOME) }) { u, padding ->
            AppointmentsRoute(u, padding, onSeeRecommendations = { nav.goTab(Routes.MATCHES) })
        }
        tab(Routes.MATCHES, nav, { user }, onLoggedOut = { resetTo(Routes.WELCOME) }) { u, padding ->
            val open = remember { container.openQuestionnaire.also { container.openQuestionnaire = false } }
            MatchesRoute(u, padding, openQuestionnaire = open, onNeedsSupport = { nav.navigate(Routes.SAFETY) })
        }
        tab(Routes.TRIAGE, nav, { user }, onLoggedOut = { resetTo(Routes.WELCOME) }) { u, padding ->
            // Pacientes respondem o questionário dentro de Matches.
            if (!u.isPsychologist) {
                LaunchedEffect(Unit) { openQuestionnaire() }
                return@tab
            }
            TriageRoute(
                u,
                padding,
                onSaved = { nav.goTab(Routes.SESSIONS) },
                onNeedsSupport = { nav.navigate(Routes.SAFETY) },
            )
        }
        tab(Routes.SETTINGS, nav, { user }, onLoggedOut = { resetTo(Routes.WELCOME) }) { u, padding ->
            SettingsRoute(
                u,
                padding,
                onQuestionnaire = { if (u.isPsychologist) nav.goTab(Routes.TRIAGE) else openQuestionnaire() },
                onAbout = { nav.navigate(Routes.ABOUT) },
                onLogout = { session.logout { resetTo(Routes.WELCOME) } },
            )
        }
        tab(Routes.SAFETY, nav, { user }, highlight = Routes.MATCHES, onLoggedOut = { resetTo(Routes.WELCOME) }) { _, padding ->
            SafetyScreen(container.lastSafety ?: Safety(SafetyLevel.NONE), padding, onContinue = { nav.goTab(Routes.MATCHES) })
        }

        composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
    }
}

/** Troca de aba sem empilhar telas (padrão recomendado para barras de navegação inferiores). */
private fun NavHostController.goTab(route: String) = navigate(route) {
    popUpTo(graph.id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun androidx.navigation.NavGraphBuilder.tab(
    route: String,
    nav: NavHostController,
    user: () -> User?,
    highlight: String = route,
    onLoggedOut: () -> Unit,
    content: @Composable (User, androidx.compose.foundation.layout.PaddingValues) -> Unit,
) = composable(route) {
    val u = user()
    if (u == null) {
        LaunchedEffect(Unit) { onLoggedOut() }
        return@composable
    }
    TabScaffold(
        user = u,
        currentRoute = highlight,
        onTab = { nav.goTab(it) },
        onAvatar = { nav.goTab(Routes.SETTINGS) },
    ) { padding -> content(u, padding) }
}

@Composable
private fun SplashScreen() {
    val scale by rememberInfiniteTransition(label = "breathe").animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale",
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.emblema), contentDescription = null, modifier = Modifier.size(84.dp).scale(scale))
    }
}
