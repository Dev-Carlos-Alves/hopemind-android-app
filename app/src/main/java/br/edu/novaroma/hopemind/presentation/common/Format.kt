package br.edu.novaroma.hopemind.presentation.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

private val BRL: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

fun formatCurrency(value: String?): String = BRL.format(value?.toDoubleOrNull() ?: 0.0)

fun firstName(name: String): String =
    name.replace(Regex("\\(.*?\\)"), "").replace(Regex("^(dra?\\.?|dr\\.?)\\s+", RegexOption.IGNORE_CASE), "").trim()
        .split(Regex("\\s+")).firstOrNull() ?: name

/**
 * Máscara visual (o estado guarda só os dígitos; a tela mostra formatado).
 * Ex.: "###.###.###-##" para CPF.
 */
class MaskTransformation(private val maskFor: (Int) -> String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val mask = maskFor(digits.length)
        val out = StringBuilder()
        val originalToTransformed = IntArray(digits.length + 1)
        var d = 0
        for (ch in mask) {
            if (d >= digits.length) break
            if (ch == '#') {
                originalToTransformed[d] = out.length
                out.append(digits[d]); d++
            } else {
                out.append(ch)
            }
        }
        originalToTransformed[digits.length] = out.length
        val transformedToOriginal = IntArray(out.length + 1)
        // Offset original = quantos dígitos ficam antes da posição t no texto formatado.
        var o = 0
        for (t in 0..out.length) {
            while (o < digits.length && originalToTransformed[o] < t) o++
            transformedToOriginal[t] = o
        }
        return TransformedText(AnnotatedString(out.toString()), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = originalToTransformed[offset.coerceIn(0, digits.length)]
            override fun transformedToOriginal(offset: Int) = transformedToOriginal[offset.coerceIn(0, out.length)]
        })
    }

    companion object {
        val Cpf = MaskTransformation { "###.###.###-##" }
        val Phone = MaskTransformation { len -> if (len <= 10) "(##) ####-####" else "(##) #####-####" }
        val Cep = MaskTransformation { "#####-###" }
    }
}

fun formatPhone(digits: String?): String? {
    if (digits.isNullOrBlank()) return null
    val d = digits.filter { it.isDigit() }
    return MaskTransformation.Phone.filter(AnnotatedString(d)).text.text
}

/** "1,2 km" / "menos de 1 km" — mesma escrita do motor de match do site. */
fun formatKm(km: Double, lessThanOne: String): String =
    if (km < 1) lessThanOne else String.format(Locale("pt", "BR"), "%.1f km", km)

/** "Graças, Recife – PE" */
fun placeLabel(neighborhood: String?, city: String?, state: String? = null): String {
    val cityPart = listOfNotNull(city, state).filter { it.isNotBlank() }.joinToString(" – ")
    return listOfNotNull(neighborhood, cityPart).filter { it.isNotBlank() }.joinToString(", ")
}
