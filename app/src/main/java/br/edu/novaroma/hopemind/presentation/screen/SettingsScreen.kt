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
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhonelinkLock
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Profile
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.common.formatCurrency
import br.edu.novaroma.hopemind.presentation.common.formatPhone
import br.edu.novaroma.hopemind.presentation.components.Avatar
import br.edu.novaroma.hopemind.presentation.components.GroupSection
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.ListRow
import br.edu.novaroma.hopemind.presentation.components.PageHeader
import br.edu.novaroma.hopemind.presentation.components.Pill
import br.edu.novaroma.hopemind.presentation.components.PillTone
import br.edu.novaroma.hopemind.presentation.components.SegmentedControl
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.ThemeMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(user: User, padding: PaddingValues, onQuestionnaire: () -> Unit, onAbout: () -> Unit, onLogout: () -> Unit) {
    val container = appContainer()
    var profile by remember { mutableStateOf<Profile?>(null) }
    val theme by container.prefs.theme.collectAsStateWithLifecycle()
    var showServer by remember { mutableStateOf(false) }
    var keepSignedIn by remember { mutableStateOf(container.auth.keepSignedIn) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { profile = container.auth.profile().getOrNull() }

    SettingsContent(
        user, profile, theme, container.prefs.apiUrl, padding, container.prefs::setTheme, onQuestionnaire, onAbout, { showServer = true }, onLogout,
        keepSignedIn = keepSignedIn,
        onKeepSignedIn = { keep -> keepSignedIn = keep; scope.launch { container.auth.setKeepSignedIn(keep) } },
    )
    if (showServer) ServerDialog(onDismiss = { showServer = false })
}

@Composable
fun SettingsContent(
    user: User,
    profile: Profile?,
    theme: ThemeMode,
    serverUrl: String,
    padding: PaddingValues,
    onTheme: (ThemeMode) -> Unit,
    onQuestionnaire: () -> Unit,
    onAbout: () -> Unit,
    onServer: () -> Unit,
    onLogout: () -> Unit,
    keepSignedIn: Boolean = false,
    onKeepSignedIn: (Boolean) -> Unit = {},
) {
    val c = HopeTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 20.dp, bottom = padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        PageHeader(title = stringResource(R.string.settings_title))

        HopeCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Avatar(user.name, 88.dp)
                Text(user.name, style = HopeTheme.type.title2, color = c.label, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                Text(user.email, style = HopeTheme.type.callout, color = c.label2)
                Pill(
                    stringResource(if (user.isPsychologist) R.string.role_psychologist else R.string.role_patient),
                    if (user.isPsychologist) PillTone.SAND else PillTone.TINT,
                )
            }
        }

        GroupSection(header = stringResource(R.string.personal_data)) {
            ListRow(stringResource(R.string.name), icon = Icons.Rounded.Person, iconBackground = Color(0xFF3A7733), value = profile?.name ?: "—")
            ListRow(stringResource(R.string.email), icon = Icons.Rounded.Email, iconBackground = Color(0xFF4A64B0), value = profile?.email ?: "—", showDivider = true)
            ListRow(stringResource(R.string.phone), icon = Icons.Rounded.Phone, iconBackground = Color(0xFF2F7A64), value = formatPhone(profile?.phone) ?: "—", showDivider = true)
            ListRow(
                stringResource(R.string.birth), icon = Icons.Rounded.CalendarMonth, iconBackground = Color(0xFFC0573A), showDivider = true,
                value = profile?.birthDate?.take(10)?.let {
                    runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault())) }.getOrNull()
                } ?: "—",
            )
        }

        // Só aparece depois de carregar o perfil, para não abrir o formulário vazio por engano.
        profile?.let { AddressCard(psychologist = user.isPsychologist, known = it.address, knownLoaded = true) }

        profile?.psychologist?.let { psy ->
            GroupSection(header = stringResource(R.string.professional_registration)) {
                ListRow(stringResource(R.string.crp), icon = Icons.Rounded.Badge, iconBackground = Color(0xFF9A7428), value = psy.crp)
                ListRow(stringResource(R.string.specialty_label), icon = Icons.Rounded.Psychology, iconBackground = Color(0xFF3A7733), value = psy.specialty, showDivider = true)
                ListRow(stringResource(R.string.session_fee_label), icon = Icons.Rounded.Payments, iconBackground = Color(0xFFC47A2C), value = formatCurrency(psy.sessionFee), showDivider = true)
            }
        }

        GroupSection(header = stringResource(R.string.appearance), footer = stringResource(R.string.appearance_footer)) {
            Column(Modifier.padding(12.dp)) {
                SegmentedControl(
                    options = listOf(stringResource(R.string.appearance_auto), stringResource(R.string.appearance_light), stringResource(R.string.appearance_dark)),
                    selected = theme.ordinal,
                    onSelect = { onTheme(ThemeMode.entries[it]) },
                )
            }
        }

        GroupSection(header = stringResource(R.string.language_title)) {
            Column(Modifier.padding(12.dp)) { LanguageSelector(showTitle = false) }
        }

        GroupSection(header = stringResource(if (user.isPsychologist) R.string.service else R.string.your_profile)) {
            ListRow(
                stringResource(if (user.isPsychologist) R.string.service_profile else R.string.redo_questionnaire),
                icon = Icons.Rounded.Assignment, chevron = true, onClick = onQuestionnaire,
            )
        }

        GroupSection(header = stringResource(R.string.app_section)) {
            ListRow(
                stringResource(R.string.keep_signed_in_setting), icon = Icons.Rounded.PhonelinkLock, iconBackground = Color(0xFF3A7733),
                subtitle = stringResource(R.string.keep_signed_in_setting_hint),
                onClick = { onKeepSignedIn(!keepSignedIn) },
                trailing = { Switch(checked = keepSignedIn, onCheckedChange = onKeepSignedIn) },
            )
            ListRow(stringResource(R.string.about), icon = Icons.Rounded.Info, iconBackground = Color(0xFF8E8E93), chevron = true, showDivider = true, onClick = onAbout)
            ListRow(
                stringResource(R.string.server_title), icon = Icons.Rounded.Dns, iconBackground = Color(0xFF636366),
                subtitle = serverUrl, chevron = true, showDivider = true, onClick = onServer,
            )
        }

        GroupSection {
            ListRow(stringResource(R.string.logout), destructive = true, onClick = onLogout)
        }

        Text(stringResource(R.string.footer_credit), style = HopeTheme.type.footnote, color = c.label3, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}
