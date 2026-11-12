package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.ProfessionalProfileRequest
import br.edu.novaroma.hopemind.domain.model.PsychologistInfo
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.components.Banner
import br.edu.novaroma.hopemind.presentation.components.BannerTone
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.launch

private const val BIO_MAX = 2000

/** "1500.00" → "1500"; "150.5" → "150,50" — the fee as the person types it. */
fun feeForInput(fee: String?): String {
    val value = fee?.toDoubleOrNull() ?: return ""
    return if (value % 1.0 == 0.0) value.toLong().toString() else String.format(java.util.Locale("pt", "BR"), "%.2f", value)
}

/** The request sent to PATCH auth/me/profile (comma decimals become dots; blanks clear bio/link). */
fun professionalRequest(specialty: String, approach: String, biography: String, fee: String, link: String) = ProfessionalProfileRequest(
    specialty = specialty.trim(),
    therapeuticApproach = approach.trim(),
    biography = biography.trim(),
    // "1.500,50" (pt-BR) → "1500.50"; "150.50" stays as typed.
    sessionFee = fee.trim().let { if (',' in it) it.replace(".", "").replace(',', '.') else it }.ifBlank { "0" },
    contactLink = link.trim(),
)

/** US-11: o psicólogo edita o que os pacientes veem nas recomendações. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileSheet(profile: PsychologistInfo, onDismiss: () -> Unit, onSaved: (PsychologistInfo) -> Unit) {
    val container = appContainer()
    val toast = LocalToast.current
    val c = HopeTheme.colors
    val scope = rememberCoroutineScope()
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val savedText = stringResource(R.string.professional_saved)

    var specialty by remember { mutableStateOf(profile.specialty) }
    var approach by remember { mutableStateOf(profile.therapeuticApproach.orEmpty()) }
    var biography by remember { mutableStateOf(profile.biography.orEmpty()) }
    var fee by remember { mutableStateOf(feeForInput(profile.sessionFee)) }
    var link by remember { mutableStateOf(profile.contactLink.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun save() {
        saving = true
        error = null
        scope.launch {
            container.auth.updateProfessionalProfile(professionalRequest(specialty, approach, biography, fee, link))
                .onSuccess { toast.success(savedText); onSaved(it); onDismiss() }
                .onFailure { error = it.message }
            saving = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = c.bgElevated) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.professional_edit_title), style = HopeTheme.type.title3, color = c.label)
            Text(stringResource(R.string.professional_edit_subtitle), style = HopeTheme.type.subhead, color = c.label2)
            error?.let { Banner(it, Icons.Rounded.ErrorOutline, BannerTone.DANGER) }

            HopeTextField(specialty, { specialty = it }, stringResource(R.string.specialty_label))
            HopeTextField(approach, { approach = it }, stringResource(R.string.approach_label))
            HopeTextField(
                biography, { if (it.length <= BIO_MAX) biography = it }, stringResource(R.string.biography_label),
                singleLine = false, minLines = 4,
                hint = stringResource(R.string.biography_hint, biography.length, BIO_MAX),
            )
            HopeTextField(
                fee, { v -> fee = v.filter { it.isDigit() || it == ',' || it == '.' } }, stringResource(R.string.session_fee_input),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            HopeTextField(
                link, { link = it }, stringResource(R.string.contact_link_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                hint = stringResource(R.string.contact_link_hint),
            )

            HopeButton(
                stringResource(R.string.save_changes), ::save, Modifier.fillMaxWidth(), size = ButtonSize.LG,
                enabled = specialty.isNotBlank() && approach.isNotBlank(), loading = saving,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
