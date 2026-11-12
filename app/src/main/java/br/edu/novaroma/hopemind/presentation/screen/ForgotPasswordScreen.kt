package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.MarkEmailRead
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.components.BackBar
import br.edu.novaroma.hopemind.presentation.components.Banner
import br.edu.novaroma.hopemind.presentation.components.BannerTone
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.presentation.components.PasswordField
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.launch

/** Regras locais do passo 2 (a API valida de novo). Retorna o recurso do erro ou null. */
fun resetFormError(code: String, password: String, confirm: String): Int? = when {
    code.filter(Char::isDigit).length != 6 -> R.string.forgot_error_code
    password.length < 8 -> R.string.forgot_error_password
    password != confirm -> R.string.forgot_error_confirm
    else -> null
}

/**
 * Esqueci minha senha:
 *  1. a pessoa informa o e-mail ou CPF e a API manda um código de 6 dígitos para o e-mail da conta;
 *  2. ela digita o código e a nova senha aqui mesmo no app (sem link para abrir).
 */
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onDone: () -> Unit, initialLogin: String = "") {
    val container = appContainer()
    val toast = LocalToast.current
    val scope = rememberCoroutineScope()
    val c = HopeTheme.colors
    val doneText = stringResource(R.string.forgot_done)

    var login by rememberSaveable { mutableStateOf(initialLogin) }
    var codeSent by rememberSaveable { mutableStateOf(false) }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var code by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val localError = resetFormError(code, password, confirm)?.let { stringResource(it) }

    fun requestCode() {
        error = null
        loading = true
        scope.launch {
            container.auth.forgotPassword(login)
                .onSuccess { info = it; codeSent = true }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun reset() {
        if (localError != null) { error = localError; return }
        error = null
        loading = true
        scope.launch {
            container.auth.resetPassword(login, code, password)
                .onSuccess { toast.success(doneText); onDone() }
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
                Text(stringResource(R.string.forgot_title), style = HopeTheme.type.title1, color = c.label)
                Text(
                    stringResource(if (codeSent) R.string.forgot_step2_subtitle else R.string.forgot_step1_subtitle),
                    style = HopeTheme.type.callout, color = c.label2,
                )
            }

            error?.let { Banner(it, Icons.Rounded.ErrorOutline, BannerTone.DANGER) }

            if (!codeSent) {
                HopeTextField(login, { login = it }, stringResource(R.string.email_or_cpf))
                HopeButton(
                    stringResource(R.string.forgot_send_code), ::requestCode, Modifier.fillMaxWidth(),
                    size = ButtonSize.LG, enabled = login.isNotBlank(), loading = loading,
                )
            } else {
                info?.let { Banner(it, Icons.Rounded.MarkEmailRead) }
                HopeTextField(
                    code, { v -> code = v.filter(Char::isDigit).take(6) }, stringResource(R.string.forgot_code),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                PasswordField(password, { password = it }, stringResource(R.string.forgot_new_password), hint = stringResource(R.string.forgot_password_hint))
                PasswordField(confirm, { confirm = it }, stringResource(R.string.forgot_confirm_password))
                HopeButton(
                    stringResource(R.string.forgot_reset), ::reset, Modifier.fillMaxWidth(),
                    size = ButtonSize.LG, enabled = code.length == 6 && password.isNotEmpty() && confirm.isNotEmpty(), loading = loading,
                )
                TextButton(onClick = ::requestCode, enabled = !loading, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.forgot_resend), style = HopeTheme.type.subhead, color = c.tint)
                }
            }
        }
    }
}
