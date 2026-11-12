package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhonelinkLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/**
 * Pergunta feita logo depois do login: "Manter conectado neste aparelho?".
 * Sim → a sessão fica salva e o app entra direto no perfil nas próximas vezes.
 * Agora não → a sessão vale só enquanto o app estiver aberto.
 */
@Composable
fun KeepSignedInDialog(userName: String, onChoice: (Boolean) -> Unit) {
    val c = HopeTheme.colors
    AlertDialog(
        // Tocar fora ou voltar conta como "agora não": nada fica salvo sem um sim explícito.
        onDismissRequest = { onChoice(false) },
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = c.bgElevated,
        icon = { Icon(Icons.Rounded.PhonelinkLock, null, tint = c.tint, modifier = Modifier.size(28.dp)) },
        title = { Text(stringResource(R.string.keep_signed_in_title), style = HopeTheme.type.title3, color = c.label) },
        text = {
            Text(stringResource(R.string.keep_signed_in_text, userName), style = HopeTheme.type.callout, color = c.label2)
        },
        confirmButton = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                HopeButton(stringResource(R.string.keep_signed_in_yes), { onChoice(true) }, Modifier.fillMaxWidth(), size = ButtonSize.LG)
                TextButton(onClick = { onChoice(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.keep_signed_in_no), style = HopeTheme.type.body, color = c.label2)
                }
            }
        },
    )
}
