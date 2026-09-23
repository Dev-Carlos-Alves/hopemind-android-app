package br.edu.novaroma.hopemind.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.edu.novaroma.hopemind.ui.theme.HopeTheme
import kotlinx.coroutines.delay

/** Avisos rápidos no topo da tela — o equivalente aos toasts do site (substituem alert()). */
@Stable
class ToastState {
    data class Message(val text: String, val error: Boolean, val id: Long = System.nanoTime())

    var current by mutableStateOf<Message?>(null)
        private set

    fun success(text: String) { current = Message(text, false) }
    fun error(text: String) { current = Message(text, true) }
    fun dismiss() { current = null }
}

val LocalToast = staticCompositionLocalOf { ToastState() }

@Composable
fun ToastHost(state: ToastState) {
    val message = state.current
    LaunchedEffect(message?.id) {
        if (message != null) {
            delay(3800)
            state.dismiss()
        }
    }
    Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(12.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            val c = HopeTheme.colors
            val shown = message ?: return@AnimatedVisibility
            Row(
                Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(c.bgElevated)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(if (shown.error) c.danger else c.tint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (shown.error) Icons.Rounded.PriorityHigh else Icons.Rounded.Check,
                        null,
                        tint = if (shown.error) Color.White else c.onTint,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(shown.text, style = HopeTheme.type.subhead.copy(fontWeight = FontWeight.Medium), color = c.label)
            }
        }
    }
}
