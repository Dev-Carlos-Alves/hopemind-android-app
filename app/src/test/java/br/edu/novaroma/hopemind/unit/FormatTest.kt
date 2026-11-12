package br.edu.novaroma.hopemind.unit

import androidx.compose.ui.text.AnnotatedString
import br.edu.novaroma.hopemind.presentation.common.MaskTransformation
import br.edu.novaroma.hopemind.presentation.common.firstName
import br.edu.novaroma.hopemind.presentation.common.formatCurrency
import br.edu.novaroma.hopemind.presentation.common.formatKm
import br.edu.novaroma.hopemind.presentation.common.formatPhone
import br.edu.novaroma.hopemind.presentation.common.placeLabel
import br.edu.novaroma.hopemind.presentation.components.initials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Formatação exibida no app — mesmas regras do site (frontend/src/services/format.ts). */
class FormatTest {

    private fun mask(m: MaskTransformation, digits: String) = m.filter(AnnotatedString(digits)).text.text

    @Test
    fun `mascaras de CPF, telefone e CEP`() {
        assertEquals("529.982.247-25", mask(MaskTransformation.Cpf, "52998224725"))
        assertEquals("529.982.2", mask(MaskTransformation.Cpf, "5299822"))
        assertEquals("(81) 3333-4444", mask(MaskTransformation.Phone, "8133334444"))
        assertEquals("(81) 99999-0000", mask(MaskTransformation.Phone, "81999990000"))
        assertEquals("52050-000", mask(MaskTransformation.Cep, "52050000"))
    }

    @Test
    fun `cursor acompanha os digitos na mascara`() {
        val t = MaskTransformation.Cpf.filter(AnnotatedString("52998224725"))
        // 4º dígito fica depois do primeiro ponto ("529." tem 4 caracteres).
        assertEquals(4, t.offsetMapping.originalToTransformed(3))
        assertEquals(3, t.offsetMapping.transformedToOriginal(4))
        assertEquals(14, t.offsetMapping.originalToTransformed(11))
    }

    @Test
    fun `telefone formatado a partir do que veio da API`() {
        assertEquals("(81) 99999-0000", formatPhone("81999990000"))
        assertNull(formatPhone(""))
        assertNull(formatPhone(null))
    }

    @Test
    fun `valor da sessao em reais`() {
        assertEquals("R\$ 150,00", formatCurrency("150.00").replace(' ', ' '))
        assertEquals("R\$ 0,00", formatCurrency(null).replace(' ', ' '))
    }

    @Test
    fun `primeiro nome e iniciais sem titulos`() {
        assertEquals("Ana", firstName("Dra. Ana Souza"))
        assertEquals("Bruno", firstName("Dr Bruno Lima (CRP 02/1234)"))
        assertEquals("AS", initials("Dra. Ana Souza"))
        assertEquals("B", initials("Bruno"))
    }

    @Test
    fun `distancia e local como no site`() {
        assertEquals("menos de 1 km", formatKm(0.4, "menos de 1 km"))
        assertEquals("8,7 km", formatKm(8.66, "menos de 1 km"))
        assertEquals("Graças, Recife – PE", placeLabel("Graças", "Recife", "PE"))
        assertEquals("Recife", placeLabel(null, "Recife"))
    }
}
