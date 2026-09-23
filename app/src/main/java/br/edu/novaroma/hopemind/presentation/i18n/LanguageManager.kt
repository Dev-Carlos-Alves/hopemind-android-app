package br.edu.novaroma.hopemind.presentation.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Troca de idioma do app (mesmo padrão do projeto-modelo da disciplina).
 * - "pt-BR" força Português (Brasil)
 * - "en" força Inglês
 * - lista vazia volta a seguir o idioma do dispositivo
 *
 * Observação: textos que vêm da API (perguntas do questionário, motivos do match)
 * são mantidos em português, como no site.
 */
object LanguageManager {
    fun usePortuguese() = AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pt-BR"))
    fun useEnglish() = AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
    fun useSystemDefault() = AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())

    /** Código atualmente forçado pelo app, ou null se segue o dispositivo. */
    fun current(): String? = AppCompatDelegate.getApplicationLocales().toLanguageTags().ifBlank { null }
}
