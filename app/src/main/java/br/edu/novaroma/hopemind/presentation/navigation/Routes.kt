package br.edu.novaroma.hopemind.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import br.edu.novaroma.hopemind.R

/** Rotas do NavHost (mesma ideia de rotas em texto do projeto-modelo). */
object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val MATCHES = "matches"
    const val SESSIONS = "sessions"
    const val TRIAGE = "triage"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val SAFETY = "safety"
}

data class Tab(val route: String, @StringRes val label: Int, val icon: ImageVector)

// Mesmos módulos do site: Home · Minhas sessões · Matches · Ajustes.
val PatientTabs = listOf(
    Tab(Routes.HOME, R.string.tab_home, Icons.Rounded.Home),
    Tab(Routes.SESSIONS, R.string.tab_sessions, Icons.Rounded.CalendarMonth),
    Tab(Routes.MATCHES, R.string.tab_matches, Icons.Rounded.AutoAwesome),
    Tab(Routes.SETTINGS, R.string.tab_settings, Icons.Rounded.Settings),
)

val PsychologistTabs = listOf(
    Tab(Routes.HOME, R.string.tab_home, Icons.Rounded.Home),
    Tab(Routes.SESSIONS, R.string.tab_agenda, Icons.Rounded.CalendarMonth),
    Tab(Routes.TRIAGE, R.string.tab_profile, Icons.Rounded.Assignment),
    Tab(Routes.SETTINGS, R.string.tab_settings, Icons.Rounded.Settings),
)
