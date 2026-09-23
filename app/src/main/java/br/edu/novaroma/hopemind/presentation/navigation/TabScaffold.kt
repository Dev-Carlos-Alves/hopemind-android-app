package br.edu.novaroma.hopemind.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.BrandLockup
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/**
 * Estrutura das telas logadas: barra superior com a marca e o avatar,
 * conteúdo e barra de abas inferior (paciente e psicólogo têm abas diferentes).
 */
@Composable
fun TabScaffold(
    user: User?,
    currentRoute: String,
    onTab: (String) -> Unit,
    onAvatar: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val c = HopeTheme.colors
    val tabs = if (user?.isPsychologist == true) PsychologistTabs else PatientTabs
    Scaffold(
        containerColor = c.bg,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column(Modifier.background(c.material).windowInsetsPadding(WindowInsets.statusBars)) {
                Row(
                    Modifier.fillMaxWidth().height(54.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BrandLockup(emblemSize = 28.dp, style = HopeTheme.type.headline)
                    if (user != null) {
                        Box(Modifier.clip(CircleShape).clickable(onClick = onAvatar)) { Avatar(user.name, 34.dp) }
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = c.separator)
            }
        },
        bottomBar = {
            Column(Modifier.background(c.material)) {
                HorizontalDivider(thickness = 0.5.dp, color = c.separator)
                Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).height(62.dp)) {
                    tabs.forEach { tab ->
                        val selected = tab.route == currentRoute
                        val color = if (selected) c.tint else c.label3
                        Column(
                            Modifier.weight(1f).fillMaxSize().clickable(role = Role.Tab) { if (!selected) onTab(tab.route) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(25.dp))
                            Text(
                                stringResource(tab.label),
                                color = color,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                    }
                }
            }
        },
        content = content,
    )
}
