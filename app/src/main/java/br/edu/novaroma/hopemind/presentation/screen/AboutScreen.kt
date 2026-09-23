package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.BuildConfig
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.presentation.components.BackBar
import br.edu.novaroma.hopemind.presentation.components.BrandLockup
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.MascotTile
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val c = HopeTheme.colors
    Column(Modifier.fillMaxSize()) {
        BackBar(onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MascotTile(104.dp, tilt = -4f)
                BrandLockup(emblemSize = 30.dp, style = HopeTheme.type.title2)
                Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), style = HopeTheme.type.footnote, color = c.label2)
            }
            Section(R.string.about_what_title, R.string.about_what_text)
            Section(R.string.about_how_title, R.string.about_how_text)
            Section(R.string.about_safety_title, R.string.about_safety_text)
            Section(R.string.about_tech_title, R.string.about_tech_text)
            Text(stringResource(R.string.footer_credit), style = HopeTheme.type.footnote, color = c.label3, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp))
        }
    }
}

@Composable
private fun Section(title: Int, text: Int) {
    HopeCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(title), style = HopeTheme.type.headline, color = HopeTheme.colors.label)
            Text(stringResource(text), style = HopeTheme.type.callout, color = HopeTheme.colors.label2)
        }
    }
}
