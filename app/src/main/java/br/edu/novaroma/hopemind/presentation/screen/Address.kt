package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.CepAddress
import br.edu.novaroma.hopemind.domain.model.ProfileAddress
import br.edu.novaroma.hopemind.presentation.common.LocalToast
import br.edu.novaroma.hopemind.presentation.common.MaskTransformation
import br.edu.novaroma.hopemind.presentation.common.appContainer
import br.edu.novaroma.hopemind.presentation.common.placeLabel
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.GroupSection
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeTextField
import br.edu.novaroma.hopemind.presentation.components.Skeleton
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.launch

/**
 * Campo de CEP: guarda só os dígitos e, ao completar 8, busca o endereço na API
 * (GET geo/cep — ViaCEP + OpenStreetMap, igual ao site).
 */
@Composable
fun CepField(
    cep: String,
    onCep: (String) -> Unit,
    label: String,
    hint: String? = null,
    onResolved: (CepAddress?) -> Unit = {},
) {
    val container = appContainer()
    val c = HopeTheme.colors
    var address by remember { mutableStateOf<CepAddress?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(cep) {
        error = null
        if (cep.length != 8) {
            address = null
            onResolved(null)
            return@LaunchedEffect
        }
        loading = true
        container.auth.lookupCep(cep)
            .onSuccess { address = it; onResolved(it) }
            .onFailure { address = null; onResolved(null); error = it.message }
        loading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HopeTextField(
            value = cep,
            onValueChange = { onCep(it.filter(Char::isDigit).take(8)) },
            label = label,
            hint = if (address == null) hint else null,
            error = error,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = MaskTransformation.Cep,
            trailing = if (loading) {
                { CircularProgressIndicator(Modifier.size(20.dp), color = c.tint, strokeWidth = 2.dp) }
            } else null,
        )
        address?.let { a ->
            Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.LocationOn, null, tint = c.peachLabel, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                Text(
                    buildAnnotatedString {
                        a.street?.let { withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$it · ") } }
                        append(placeLabel(a.neighborhood, a.city, a.state))
                    },
                    style = HopeTheme.type.footnote,
                    color = c.label2,
                )
            }
        }
    }
}

/**
 * Endereço do cadastro com opção de alterar pelo CEP. Para o psicólogo é o consultório
 * (o paciente vê só o bairro); para o paciente serve só para calcular distâncias.
 *
 * @param known endereço já carregado pela tela (evita outra chamada); se `null` e [knownLoaded] for falso, busca em auth/me.
 */
@Composable
fun AddressCard(
    psychologist: Boolean,
    known: ProfileAddress? = null,
    knownLoaded: Boolean = false,
    title: String = stringResource(if (psychologist) R.string.office_title else R.string.address_title),
    footer: String = stringResource(if (psychologist) R.string.address_footer_psy else R.string.address_footer_patient),
    startEditing: Boolean = false,
    onSaved: (ProfileAddress) -> Unit = {},
) {
    val container = appContainer()
    val toast = LocalToast.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var address by remember(known) { mutableStateOf(known) }
    var loaded by remember(knownLoaded) { mutableStateOf(knownLoaded) }
    var editing by remember { mutableStateOf(startEditing) }
    var cep by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var resolved by remember { mutableStateOf<CepAddress?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(knownLoaded) {
        if (!knownLoaded) {
            address = container.auth.profile().getOrNull()?.address
            loaded = true
        }
    }
    LaunchedEffect(address) {
        cep = address?.cep?.filter(Char::isDigit).orEmpty()
        number = address?.addressNumber.orEmpty()
    }
    // Sem endereço salvo não há o que mostrar: vai direto para o formulário (decidido já na composição,
    // e não num efeito, para nunca desenhar a linha do endereço com ele nulo).
    val saved = address?.takeIf { it.cep != null }

    fun save() {
        saving = true
        scope.launch {
            container.auth.updateAddress(cep, number)
                .onSuccess {
                    address = it
                    editing = false
                    toast.success(context.getString(R.string.address_saved))
                    onSaved(it)
                }
                .onFailure { toast.error(it.message ?: "") }
            saving = false
        }
    }

    GroupSection(header = title, footer = footer) {
        when {
            !loaded -> Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Skeleton(Modifier.size(30.dp), height = 30.dp)
                Skeleton(Modifier.fillMaxWidth(0.5f), height = 14.dp)
            }
            editing || saved == null -> Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CepField(
                    cep = cep,
                    onCep = { cep = it },
                    label = stringResource(if (psychologist) R.string.cep_office else R.string.cep),
                    onResolved = { resolved = it },
                )
                HopeTextField(number, { number = it.take(20) }, stringResource(R.string.address_number))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    if (saved != null) HopeButton(stringResource(R.string.cancel), { editing = false }, variant = ButtonVariant.GRAY)
                    HopeButton(stringResource(R.string.address_save), ::save, enabled = resolved != null, loading = saving)
                }
            }
            else -> {
                val a = saved
                val c = HopeTheme.colors
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFC0573A)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(placeLabel(a.neighborhood, a.city, a.state), style = HopeTheme.type.body, color = c.label)
                        Text(
                            listOfNotNull(listOfNotNull(a.street, a.addressNumber).joinToString(", ").ifBlank { null }, "CEP ${a.cep}").joinToString(" · "),
                            style = HopeTheme.type.footnote,
                            color = c.label2,
                        )
                    }
                    HopeButton(stringResource(R.string.address_change), { editing = true }, variant = ButtonVariant.PLAIN, size = ButtonSize.SM)
                }
            }
        }
    }
}
