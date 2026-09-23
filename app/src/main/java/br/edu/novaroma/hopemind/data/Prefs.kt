package br.edu.novaroma.hopemind.data

import android.content.Context
import br.edu.novaroma.hopemind.BuildConfig
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Preferências locais do app: aparência e endereço do servidor. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("hopemind.prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(
        runCatching { ThemeMode.valueOf(sp.getString(KEY_THEME, ThemeMode.AUTO.name)!!) }.getOrDefault(ThemeMode.AUTO),
    )
    val theme: StateFlow<ThemeMode> = _theme

    fun setTheme(mode: ThemeMode) {
        sp.edit().putString(KEY_THEME, mode.name).apply()
        _theme.value = mode
    }

    var apiUrl: String
        get() = sp.getString(KEY_API, null) ?: BuildConfig.API_BASE_URL
        set(value) {
            val normalized = value.trim().let { if (it.endsWith("/")) it else "$it/" }
            sp.edit().putString(KEY_API, normalized).apply()
        }

    fun resetApiUrl() = sp.edit().remove(KEY_API).apply()

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_API = "api_url"
    }
}
