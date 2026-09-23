package br.edu.novaroma.hopemind.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/** "Hope" + "Mind" com a cor areia do logo. */
@Composable
fun Wordmark(style: TextStyle = HopeTheme.type.title3) {
    val c = HopeTheme.colors
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = c.label)) { append("Hope") }
            withStyle(SpanStyle(color = c.sandLabel)) { append("Mind") }
        },
        style = style.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
fun BrandLockup(emblemSize: Dp = 32.dp, style: TextStyle = HopeTheme.type.title3) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(painterResource(R.drawable.emblema), contentDescription = null, modifier = Modifier.size(emblemSize))
        Wordmark(style)
    }
}

/** Fundo com a "lavagem" de cores da marca (sálvia, areia, pêssego), igual às telas de login do site. */
@Composable
fun BrandWash(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c = HopeTheme.colors
    Box(modifier.background(c.bgBrand)) {
        Canvas(Modifier.fillMaxSize()) {
            fun blob(color: Color, x: Float, y: Float, r: Float) = drawRect(
                Brush.radialGradient(listOf(color, Color.Transparent), center = Offset(size.width * x, size.height * y), radius = size.maxDimension * r),
            )
            blob(c.washA, 0.1f, 0.12f, 0.55f)
            blob(c.washB, 0.95f, 0.05f, 0.45f)
            blob(c.washC, 0.75f, 0.95f, 0.6f)
        }
        content()
    }
}
