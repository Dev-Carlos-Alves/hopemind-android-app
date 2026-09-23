package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Safety
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.PageHeader
import br.edu.novaroma.hopemind.presentation.components.SafetyPanel

/** Mostrada depois do questionário quando há resposta de risco (antes das recomendações). */
@Composable
fun SafetyScreen(safety: Safety, padding: PaddingValues, onContinue: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 20.dp, bottom = padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        PageHeader(title = stringResource(R.string.safety_page_title), eyebrow = stringResource(R.string.safety_eyebrow))
        SafetyPanel(safety) {
            HopeButton(
                stringResource(R.string.safety_see_recommendations),
                onContinue,
                Modifier.fillMaxWidth(),
                size = ButtonSize.LG,
                trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
            )
        }
    }
}
