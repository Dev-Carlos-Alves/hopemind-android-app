package br.edu.novaroma.hopemind.presentation.screen.home

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.R
import br.edu.novaroma.hopemind.domain.model.User
import br.edu.novaroma.hopemind.presentation.common.firstName
import br.edu.novaroma.hopemind.presentation.components.BrandWash
import br.edu.novaroma.hopemind.presentation.components.ButtonSize
import br.edu.novaroma.hopemind.presentation.components.ButtonVariant
import br.edu.novaroma.hopemind.presentation.components.HopeButton
import br.edu.novaroma.hopemind.presentation.components.HopeCard
import br.edu.novaroma.hopemind.presentation.components.MascotTile
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import br.edu.novaroma.hopemind.ui.theme.Radius
import java.time.LocalTime

private data class Item(val icon: ImageVector, @StringRes val title: Int, @StringRes val text: Int)
private data class Factor(@StringRes val label: Int, val weight: Int, @StringRes val text: Int)

private val STEPS = listOf(
    Item(Icons.Rounded.Assignment, R.string.home_step1_title, R.string.home_step1_text),
    Item(Icons.Rounded.AutoAwesome, R.string.home_step2_title, R.string.home_step2_text),
    Item(Icons.Rounded.CalendarMonth, R.string.home_step3_title, R.string.home_step3_text),
)

// Mesmos pesos do motor de match (hm-match-1.1.0).
private val FACTORS = listOf(
    Factor(R.string.comp_demanda, 25, R.string.factor_demanda),
    Factor(R.string.comp_estilo, 18, R.string.factor_estilo),
    Factor(R.string.comp_modalidade, 15, R.string.factor_localizacao),
    Factor(R.string.comp_experiencia, 14, R.string.factor_experiencia),
    Factor(R.string.comp_relacao, 13, R.string.factor_relacao),
    Factor(R.string.comp_disponibilidade, 10, R.string.factor_disponibilidade),
    Factor(R.string.comp_preferencias, 5, R.string.factor_preferencias),
)

private val PRINCIPLES = listOf(
    Item(Icons.Rounded.Visibility, R.string.principle_transparent_title, R.string.principle_transparent_text),
    Item(Icons.Rounded.Shield, R.string.principle_safe_title, R.string.principle_safe_text),
    Item(Icons.Rounded.Lock, R.string.principle_private_title, R.string.principle_private_text),
    Item(Icons.Rounded.LocationOn, R.string.principle_near_title, R.string.principle_near_text),
)

private val APPROACHES = listOf(
    R.string.approach_tcc_name to R.string.approach_tcc_text,
    R.string.approach_psicanalise_name to R.string.approach_psicanalise_text,
    R.string.approach_humanista_name to R.string.approach_humanista_text,
    R.string.approach_sistemica_name to R.string.approach_sistemica_text,
    R.string.approach_act_name to R.string.approach_act_text,
    R.string.approach_emdr_name to R.string.approach_emdr_text,
)

private val HABITS = listOf(
    Item(Icons.Rounded.Bedtime, R.string.habit_sleep_title, R.string.habit_sleep_text),
    Item(Icons.Rounded.Spa, R.string.habit_pause_title, R.string.habit_pause_text),
    Item(Icons.Rounded.ChatBubbleOutline, R.string.habit_support_title, R.string.habit_support_text),
)

@Composable
fun greeting(name: String): String {
    val h = LocalTime.now().hour
    val res = when {
        h < 5 -> R.string.greeting_evening
        h < 12 -> R.string.greeting_morning
        h < 18 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    return stringResource(res, firstName(name))
}

/**
 * Home institucional (igual à do site): quem somos, como funciona, o que o match considera,
 * princípios, abordagens, cuidados do dia a dia e canais de apoio.
 */
@Composable
fun HomeScreen(user: User, padding: PaddingValues, onMatches: () -> Unit, onSessions: () -> Unit, onProfile: () -> Unit) {
    val c = HopeTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 28.dp),
        verticalArrangement = Arrangement.spacedBy(36.dp),
    ) {
        // Hero
        BrandWash(Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xxl))) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                MascotTile(76.dp, tilt = -5f)
                Text(greeting(user.name).uppercase(), style = HopeTheme.type.eyebrow, color = c.tint)
                Text(stringResource(R.string.home_hero_title), style = HopeTheme.type.largeTitle.copy(fontSize = HopeTheme.type.largeTitle.fontSize * 1.05f), color = c.label)
                Text(stringResource(R.string.home_hero_lede), style = HopeTheme.type.body, color = c.label2)
                if (user.isPsychologist) {
                    HopeButton(stringResource(R.string.home_cta_agenda), onSessions, Modifier.fillMaxWidth(), size = ButtonSize.LG, trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward)
                    HopeButton(stringResource(R.string.service_profile), onProfile, Modifier.fillMaxWidth(), variant = ButtonVariant.GRAY)
                } else {
                    HopeButton(
                        stringResource(if (user.hasTriage) R.string.home_cta_matches else R.string.home_cta_find), onMatches, Modifier.fillMaxWidth(),
                        size = ButtonSize.LG, trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                    )
                }
            }
        }

        Section(R.string.home_about_eyebrow, R.string.home_about_title) {
            Text(stringResource(R.string.home_about_p1), style = HopeTheme.type.callout, color = c.label2)
            Text(stringResource(R.string.home_about_p2), style = HopeTheme.type.callout, color = c.label2)
        }

        Section(R.string.home_how_eyebrow, R.string.home_how_title) {
            STEPS.forEachIndexed { i, s ->
                HopeCard {
                    Box(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconTile(s.icon)
                            Text(stringResource(s.title), style = HopeTheme.type.title3, color = c.label)
                            Text(stringResource(s.text), style = HopeTheme.type.callout, color = c.label2)
                        }
                        Text(
                            "${i + 1}",
                            style = HopeTheme.type.largeTitle.copy(fontSize = HopeTheme.type.largeTitle.fontSize * 1.3f),
                            color = c.fillStrong,
                            modifier = Modifier.align(Alignment.TopEnd),
                        )
                    }
                }
            }
        }

        Section(R.string.home_factors_eyebrow, R.string.home_factors_title, R.string.home_factors_text) {
            HopeCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FACTORS.forEach { f ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(stringResource(f.label), style = HopeTheme.type.headline, color = c.label, modifier = Modifier.weight(1f))
                                Text("${f.weight}%", style = HopeTheme.type.footnote.copy(fontWeight = FontWeight.SemiBold), color = c.tint)
                            }
                            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(c.fill)) {
                                Box(Modifier.fillMaxWidth(f.weight / 25f).height(8.dp).clip(RoundedCornerShape(50)).background(c.tint))
                            }
                            Text(stringResource(f.text), style = HopeTheme.type.footnote, color = c.label2)
                        }
                    }
                }
            }
        }

        Section(R.string.home_principles_eyebrow, R.string.home_principles_title) {
            PRINCIPLES.forEach { p -> SmallCard(p) }
        }

        Section(R.string.home_approaches_eyebrow, R.string.home_approaches_title, R.string.home_approaches_text) {
            APPROACHES.forEach { (name, text) ->
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(topEnd = Radius.md, bottomEnd = Radius.md)).background(c.bgElevated),
                ) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(c.sage))
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(stringResource(name), style = HopeTheme.type.headline, color = c.label)
                        Text(stringResource(text), style = HopeTheme.type.subhead, color = c.label2)
                    }
                }
            }
        }

        Section(R.string.home_habits_eyebrow, R.string.home_habits_title) {
            HABITS.forEach { h -> SmallCard(h, sand = true) }
        }

        HelpNow()

        Text(stringResource(R.string.home_footer), style = HopeTheme.type.footnote, color = c.label3, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Section(@StringRes eyebrow: Int, @StringRes title: Int, @StringRes text: Int? = null, content: @Composable () -> Unit) {
    val c = HopeTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(eyebrow).uppercase(), style = HopeTheme.type.eyebrow, color = c.tint)
            Text(stringResource(title), style = HopeTheme.type.title1, color = c.label)
            text?.let { Text(stringResource(it), style = HopeTheme.type.callout, color = c.label2, modifier = Modifier.padding(top = 4.dp)) }
        }
        content()
    }
}

@Composable
private fun IconTile(icon: ImageVector, sand: Boolean = false) {
    val c = HopeTheme.colors
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (sand) c.sandSoft else c.tintSoft),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = if (sand) c.sandLabel else c.tintSoftLabel, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun SmallCard(item: Item, sand: Boolean = false) {
    val c = HopeTheme.colors
    HopeCard(padding = PaddingValues(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconTile(item.icon, sand)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(item.title), style = HopeTheme.type.headline, color = c.label)
                Text(stringResource(item.text), style = HopeTheme.type.subhead, color = c.label2)
            }
        }
    }
}

@Composable
private fun HelpNow() {
    val c = HopeTheme.colors
    val context = LocalContext.current
    fun dial(number: String) = runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.xl)).background(c.peachSoft).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(c.peach), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Favorite, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
        Text(stringResource(R.string.home_help_title), style = HopeTheme.type.title2, color = c.label)
        Text(stringResource(R.string.home_help_text), style = HopeTheme.type.callout, color = c.label)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HopeButton(stringResource(R.string.call_cvv), { dial("188") }, Modifier.weight(1f), variant = ButtonVariant.FILLED)
            HopeButton(stringResource(R.string.call_samu), { dial("192") }, Modifier.weight(1f), variant = ButtonVariant.GRAY)
        }
    }
}
