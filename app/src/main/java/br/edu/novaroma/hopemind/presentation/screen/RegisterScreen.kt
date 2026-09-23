package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.RegisterRequest
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.MaskTransformation
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.components.BackBar
import br.edu.novaroma.hopemind.presentation.components.Banner
import br.edu.novaroma.hopemind.presentation.components.BannerTone
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.presentation.components.PasswordField
import br.edu.novaroma.hopemind.presentation.components.SegmentedControl
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val GENDERS = listOf(
    R.string.gender_undisclosed to "Prefiro não informar",
    R.string.gender_female to "Feminino",
    R.string.gender_male to "Masculino",
    R.string.gender_nonbinary to "Não binário",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBack: () -> Unit, onLogin: () -> Unit, onRegistered: (User) -> Unit) {
    val container = appContainer()
    val toast = LocalToast.current
    val scope = rememberCoroutineScope()
    val c = HopeTheme.colors

    var type by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var birth by rememberSaveable { mutableStateOf("") } // yyyy-MM-dd
    var gender by rememberSaveable { mutableIntStateOf(0) }
    var crp by rememberSaveable { mutableStateOf("") }
    var fee by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var cep by rememberSaveable { mutableStateOf("") }
    var addressNumber by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    var genderOpen by remember { mutableStateOf(false) }
    val isPsy = type == 1
    val successText = stringResource(R.string.register_success)

    fun submit() {
        error = null
        loading = true
        scope.launch {
            val request = RegisterRequest(
                userType = if (isPsy) "PSYCHOLOGIST" else "PATIENT",
                name = name.trim(), email = email.trim(), password = password,
                cpf = cpf, phone = phone, birthDate = birth, gender = GENDERS[gender].second,
                crp = crp.takeIf { isPsy }, specialty = specialty.takeIf { isPsy }, sessionFee = fee.takeIf { isPsy && it.isNotBlank() },
                cep = cep.takeIf { it.length == 8 }, addressNumber = addressNumber.takeIf { cep.length == 8 && it.isNotBlank() },
            )
            container.auth.register(request)
                .onSuccess {
                    container.auth.login(email, password)
                        .onSuccess { user -> toast.success(successText); onRegistered(user) }
                        .onFailure { error = it.message }
                }
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.register_title), style = HopeTheme.type.title1, color = c.label)
                Text(stringResource(R.string.register_subtitle), style = HopeTheme.type.callout, color = c.label2)
            }

            SegmentedControl(
                options = listOf(stringResource(R.string.account_patient), stringResource(R.string.account_psychologist)),
                selected = type,
                onSelect = { type = it },
                large = true,
            )

            error?.let { Banner(it, Icons.Rounded.ErrorOutline, BannerTone.DANGER) }

            HopeTextField(name, { name = it }, stringResource(R.string.full_name))
            HopeTextField(email, { email = it }, stringResource(R.string.email), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            HopeTextField(
                cpf, { cpf = it.filter(Char::isDigit).take(11) }, stringResource(R.string.cpf),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = MaskTransformation.Cpf,
            )
            HopeTextField(
                phone, { phone = it.filter(Char::isDigit).take(11) }, stringResource(R.string.phone),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                visualTransformation = MaskTransformation.Phone,
            )

            // Campo de data: abre o DatePicker do Material 3 ao tocar.
            Box {
                HopeTextField(
                    value = birth.takeIf { it.isNotBlank() }?.let { it.substring(8, 10) + "/" + it.substring(5, 7) + "/" + it.substring(0, 4) }.orEmpty(),
                    onValueChange = {},
                    label = stringResource(R.string.birth_date),
                    readOnly = true,
                )
                Box(Modifier.matchParentSize().clickable { pickDate = true })
            }

            ExposedDropdownMenuBox(expanded = genderOpen, onExpandedChange = { genderOpen = it }) {
                HopeTextField(
                    value = stringResource(GENDERS[gender].first),
                    onValueChange = {},
                    label = stringResource(R.string.gender),
                    readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailing = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderOpen) },
                )
                ExposedDropdownMenu(expanded = genderOpen, onDismissRequest = { genderOpen = false }) {
                    GENDERS.forEachIndexed { i, (label, _) ->
                        DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = { gender = i; genderOpen = false })
                    }
                }
            }

            CepField(
                cep = cep,
                onCep = { cep = it },
                label = stringResource(if (isPsy) R.string.cep_office else R.string.cep),
                hint = stringResource(if (isPsy) R.string.cep_hint_psy else R.string.cep_hint_patient),
            )
            HopeTextField(addressNumber, { addressNumber = it.take(20) }, stringResource(R.string.address_number))

            AnimatedVisibility(isPsy) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        stringResource(R.string.professional_registration).uppercase(),
                        style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.Medium),
                        color = c.label2,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                    )
                    HopeTextField(crp, { crp = it }, stringResource(R.string.crp), hint = stringResource(R.string.crp_hint))
                    HopeTextField(specialty, { specialty = it }, stringResource(R.string.specialty), hint = stringResource(R.string.specialty_hint))
                    HopeTextField(fee, { fee = it.filter { ch -> ch.isDigit() || ch == '.' } }, stringResource(R.string.session_fee), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            }

            PasswordField(password, { password = it }, stringResource(R.string.create_password), hint = stringResource(R.string.password_hint))

            HopeButton(stringResource(R.string.register_button), ::submit, Modifier.fillMaxWidth(), size = ButtonSize.LG, loading = loading)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.have_account) + " ", style = HopeTheme.type.subhead, color = c.label2)
                Text(
                    stringResource(R.string.sign_in),
                    style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = c.tint,
                    modifier = Modifier.clickable(onClick = onLogin),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (pickDate) {
        val state = rememberDatePickerState(initialDisplayMode = androidx.compose.material3.DisplayMode.Input)
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        birth = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    pickDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = state) }
    }
}
