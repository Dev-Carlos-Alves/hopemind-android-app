package br.edu.novaroma.hopemind.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.Safety
import br.edu.novaroma.hopemind.domain.model.SafetyLevel
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius

/** Painel de apoio mostrado quando o paciente indica risco (seção 1 do documento de requisitos). */
@Composable
fun SafetyPanel(safety: Safety, compact: Boolean = false, footer: @Composable (() -> Unit)? = null) {
    if (safety.level == SafetyLevel.NONE) return
    val c = HopeTheme.colors
    val context = LocalContext.current
    val urgent = safety.level == SafetyLevel.IMMEDIATE
    val (title, text) = when (safety.level) {
        SafetyLevel.IMMEDIATE -> R.string.safety_title_immediate to R.string.safety_text_immediate
        SafetyLevel.ELEVATED -> R.string.safety_title_elevated to R.string.safety_text_elevated
        else -> R.string.safety_title_wants_talk to R.string.safety_text_wants_talk
    }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xl)).background(if (urgent) c.dangerSoft else c.peachSoft).padding(if (compact) 18.dp else 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.bgElevated), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Favorite, null, tint = if (urgent) c.danger else c.peachLabel, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(title), style = if (compact) HopeTheme.type.headline else HopeTheme.type.title3, color = c.label)
                Text(stringResource(text), style = HopeTheme.type.callout, color = c.label2)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(c.bgElevated)) {
            safety.resources.forEachIndexed { i, r ->
                val phone = r.href?.removePrefix("tel:")
                ListRow(
                    label = r.name,
                    subtitle = r.detail,
                    icon = if (phone != null) Icons.Rounded.Call else Icons.Rounded.LocalHospital,
                    iconBackground = if (phone != null) c.tint else Color(0xFF8E8E93),
                    showDivider = i > 0,
                    chevron = phone != null,
                    onClick = phone?.let { number -> { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) } },
                )
            }
        }
        footer?.invoke()
    }
}
