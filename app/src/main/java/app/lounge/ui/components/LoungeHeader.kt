package app.lounge.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.LoungeWordmark
import kotlinx.coroutines.delay
import java.util.Date

/**
 * Lamp wordmark and a quiet status line. The clock, on a television, sits in
 * its own glass chip. Long-press the wordmark to preview TV mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoungeHeader(
    eyebrow: String,
    onTitleLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    showClock: Boolean = false,
) {
    val metrics = LocalLoungeMetrics.current
    val context = LocalContext.current
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(showClock) {
        if (!showClock) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val time = if (showClock) {
        DateFormat.getTimeFormat(context).format(Date(now))
    } else {
        null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = metrics.screenPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LoungeWordmark(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                        onLongClick = onTitleLongPress,
                    )
                    .semantics { contentDescription = "Lounge" },
                titleSize = metrics.titleSize,
                lampSize = if (metrics.isTelevision) 40.dp else 30.dp,
            )
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.bodySmall,
                fontSize = metrics.eyebrowSize,
                color = Ember.CreamDim,
            )
        }
        if (time != null) {
            LiquidGlass(corner = 20.dp) {
                Text(
                    text = time,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = metrics.rowTitleSize,
                    color = Ember.Cream,
                )
            }
        }
    }
}
