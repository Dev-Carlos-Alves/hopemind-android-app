package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.BuildConfig
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.components.BackBar
import br.edu.novaroma.hopemind.presentation.components.Banner
import br.edu.novaroma.hopemind.presentation.components.BannerTone
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.PasswordField
import br.edu.novaroma.hopemind.presentation.components.Pill
import br.edu.novaroma.hopemind.presentation.components.PillTone
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onBack: () -> Unit, onRegister: () -> Unit, onLoggedIn: (User) -> Unit) {
    val container = appContainer()
    val scope = rememberCoroutineScope()
    val c = HopeTheme.colors

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showServer by remember { mutableStateOf(false) }

    fun submit() {
        error = null
        loading = true
        scope.launch {
            container.auth.login(email, password)
                .onSuccess(onLoggedIn)
                .onFailure { error = it.message }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        BackBar(onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.login_title), style = HopeTheme.type.title1, color = c.label)
                Text(stringResource(R.string.login_subtitle), style = HopeTheme.type.callout, color = c.label2)
            }

            error?.let { Banner(it, Icons.Rounded.ErrorOutline, BannerTone.DANGER) }

            HopeTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            PasswordField(password, { password = it }, stringResource(R.string.password))

            HopeButton(
                stringResource(R.string.login_button),
                ::submit,
                Modifier.fillMaxWidth(),
                size = ButtonSize.LG,
                enabled = email.isNotBlank() && password.isNotBlank(),
                loading = loading,
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.login_new_here) + " ", style = HopeTheme.type.subhead, color = c.label2)
                Text(
                    stringResource(R.string.login_create),
                    style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = c.tint,
                    modifier = Modifier.clickable(onClick = onRegister),
                )
            }

            HorizontalDivider(color = c.separator)

            if (BuildConfig.DEBUG) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.demo_accounts) + ":", style = HopeTheme.type.footnote, color = c.label2)
                    Row(Modifier.clickable { email = "paciente@hopemind.local"; password = "hopemind123" }) {
                        Pill(stringResource(R.string.role_patient), PillTone.TINT)
                    }
                    Row(Modifier.clickable { email = "rafael@hopemind.local"; password = "hopemind123" }) {
                        Pill(stringResource(R.string.role_psychologist), PillTone.SAND)
                    }
                }
            }

            TextButton(onClick = { showServer = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                androidx.compose.material3.Icon(Icons.Rounded.Dns, null, tint = c.label2, modifier = Modifier.padding(end = 6.dp))
                Text(stringResource(R.string.server_title), style = HopeTheme.type.footnote, color = c.label2)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showServer) ServerDialog(onDismiss = { showServer = false })
}

/** Permite apontar o app para a API do PC (útil em celular físico). */
@Composable
fun ServerDialog(onDismiss: () -> Unit) {
    val container = appContainer()
    val toast = LocalToast.current
    var url by remember { mutableStateOf(container.prefs.apiUrl) }
    val saved = stringResource(R.string.server_saved)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.server_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.server_hint), style = HopeTheme.type.subhead, color = HopeTheme.colors.label2)
                HopeTextField(url, { url = it }, "URL", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
            }
        },
        confirmButton = {
            TextButton(onClick = { container.changeServer(url); toast.success(saved); onDismiss() }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = { container.changeServer(null); toast.success(saved); onDismiss() }) { Text(stringResource(R.string.server_reset)) }
        },
    )
}
