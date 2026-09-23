package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.presentation.components.SegmentedControl
import br.edu.novaroma.hopemind.presentation.i18n.LanguageManager
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/** Seletor de idioma reutilizável (mesma ideia do LanguageSelector do projeto-modelo). */
@Composable
fun LanguageSelector(showTitle: Boolean = true) {
    var selected by remember {
        mutableIntStateOf(
            when (LanguageManager.current()?.lowercase()) {
                "pt-br", "pt" -> 1
                "en" -> 2
                else -> 0
            },
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showTitle) Text(stringResource(R.string.language_title), style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.SemiBold), color = HopeTheme.colors.label2)
        SegmentedControl(
            options = listOf(stringResource(R.string.appearance_auto), "PT-BR", "EN"),
            selected = selected,
            onSelect = {
                selected = it
                when (it) {
                    1 -> LanguageManager.usePortuguese()
                    2 -> LanguageManager.useEnglish()
                    else -> LanguageManager.useSystemDefault()
                }
            },
        )
    }
}
