package br.edu.novaroma.hopemind

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.novaroma.hopemind.presentation.SessionViewModel
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.ToastHost
import br.edu.novaroma.hopemind.presentation.common.ToastState
import br.edu.novaroma.hopemind.presentation.common.hopeViewModel
import br.edu.novaroma.hopemind.presentation.navigation.AppNavHost
import br.edu.novaroma.hopemind.ui.theme.HopeMindTheme
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/**
 * AppCompatActivity (e não ComponentActivity) porque a troca de idioma usa
 * AppCompatDelegate.setApplicationLocales(), como no projeto-modelo da disciplina.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val prefs = (application as HopeMindApplication).container.prefs

        setContent {
            val themeMode by prefs.theme.collectAsStateWithLifecycle()
            HopeMindTheme(themeMode) {
                val dark = HopeTheme.colors.isDark
                val view = LocalView.current
                SideEffect {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                val toast = remember { ToastState() }
                val session = hopeViewModel { SessionViewModel(it) }
                CompositionLocalProvider(LocalToast provides toast) {
                    Box(Modifier.fillMaxSize().background(HopeTheme.colors.bg)) {
                        AppNavHost(session)
                        ToastHost(toast)
                    }
                }
            }
        }
    }
}
