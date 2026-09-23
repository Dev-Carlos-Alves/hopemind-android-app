package br.edu.novaroma.hopemind.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.presentation.components.BrandLockup
import br.edu.novaroma.hopemind.presentation.components.BrandWash
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.MascotTile
import br.edu.novaroma.hopemind.ui.theme.HopeTheme

/** Primeira tela (equivalente ao "PreHome" do modelo): marca, proposta de valor, idioma e entrada. */
@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val c = HopeTheme.colors
    BrandWash(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            BrandLockup(emblemSize = 36.dp)
            Spacer(Modifier.height(36.dp))

            MascotTile(92.dp, tilt = -6f)
            Spacer(Modifier.height(24.dp))

            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.welcome_headline_start))
                    withStyle(SpanStyle(brush = Brush.linearGradient(listOf(c.tint, Color(0xFFB98A2F))))) {
                        append(stringResource(R.string.welcome_headline_em))
                    }
                    append(stringResource(R.string.welcome_headline_end))
                },
                style = HopeTheme.type.largeTitle.copy(fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.035).em),
                color = c.label,
            )
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.welcome_lede), style = HopeTheme.type.headline.copy(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp), color = c.label2)

            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Feature(Icons.Rounded.AutoAwesome, R.string.feature_match_title, R.string.feature_match_text)
                Feature(Icons.Rounded.Shield, R.string.feature_privacy_title, R.string.feature_privacy_text)
                Feature(Icons.Rounded.CalendarMonth, R.string.feature_booking_title, R.string.feature_booking_text)
            }

            Spacer(Modifier.height(28.dp))
            LanguageSelector()
            Spacer(Modifier.height(32.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HopeButton(stringResource(R.string.welcome_login), onLogin, Modifier.fillMaxWidth(), size = ButtonSize.LG)
                HopeButton(stringResource(R.string.welcome_register), onRegister, Modifier.fillMaxWidth(), variant = ButtonVariant.GRAY, size = ButtonSize.LG)
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                Image(
                    painterResource(R.drawable.safe_mind_live_logo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(Color.White).padding(3.dp),
                )
                Text(stringResource(R.string.credit), style = HopeTheme.type.footnote, color = c.label2)
            }
        }
    }
}

@Composable
private fun Feature(icon: ImageVector, title: Int, text: Int) {
    val c = HopeTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(38.dp).shadow(1.dp, RoundedCornerShape(11.dp)).clip(RoundedCornerShape(11.dp)).background(c.bgElevated),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = c.tint, modifier = Modifier.size(20.dp)) }
        Column {
            Text(stringResource(title), style = HopeTheme.type.callout.copy(fontWeight = FontWeight.SemiBold), color = c.label)
            Text(stringResource(text), style = HopeTheme.type.subhead, color = c.label2)
        }
    }
}
