package app.lounge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.lounge.media.MediaEntry
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.glass.TitleArtwork
import app.lounge.ui.glass.glassFocus
import app.lounge.ui.glass.rememberLocalArtwork
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.loungeTween

/**
 * Wide glass feature for the thing to play next. The whole pane is the control.
 */
@Composable
fun HeroCard(
    entry: MediaEntry,
    kicker: String,
    action: String?,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        when {
            focused -> if (metrics.isTelevision) 1.03f else 1.015f
            pressed -> 0.99f
            else -> 1f
        },
        animationSpec = loungeTween(),
        label = "hero-scale",
    )
    val emphasis by animateFloatAsState(
        if (focused) 1f else 0f,
        loungeTween(),
        label = "hero-glass",
    )
    val artwork = rememberLocalArtwork(
        uri = entry.uri,
        enabled = entry.local,
        sizePx = if (metrics.isTelevision) 1280 else 960,
    )
    val corner = metrics.tileCorner + 4.dp
    val description = buildString {
        append(kicker)
        append(", ")
        append(entry.title)
        entry.subtitle?.let {
            append(", ")
            append(it)
        }
        action?.let {
            append(", ")
            append(it)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(metrics.heroHeight)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .glassFocus(emphasis, corner, metrics.focusStroke)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
    ) {
        LiquidGlass(
            modifier = Modifier.fillMaxSize(),
            corner = corner,
            emphasis = emphasis,
        ) {
            TitleArtwork(
                motif = entry.art,
                seed = entry.id.hashCode(),
                image = artwork,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(metrics.heroHeight * 0.62f)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF070A14).copy(alpha = 0.72f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = if (metrics.isTelevision) 36.dp else 22.dp)
                    .padding(bottom = if (metrics.isTelevision) 28.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = kicker,
                    style = MaterialTheme.typography.labelLarge,
                    color = Ember.AmberBright,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.1.sp,
                )
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = Ember.Cream,
                    fontSize = if (metrics.isTelevision) 44.sp else 30.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!entry.subtitle.isNullOrBlank()) {
                        Text(
                            text = entry.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ember.CreamMuted,
                            fontSize = if (metrics.isTelevision) 18.sp else 14.sp,
                        )
                    }
                    if (action != null) {
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Ember.AmberBright,
                            fontSize = if (metrics.isTelevision) 18.sp else 14.sp,
                        )
                    }
                }
                if (!entry.credit.isNullOrBlank()) {
                    Text(
                        text = entry.credit,
                        style = MaterialTheme.typography.bodySmall,
                        color = Ember.CreamDim,
                        fontSize = if (metrics.isTelevision) 14.sp else 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            val progress = entry.progress
            if (progress != null) {
                GlassProgress(
                    progress = progress,
                    emphasized = focused,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
