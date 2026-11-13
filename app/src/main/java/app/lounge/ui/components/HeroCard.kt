package app.lounge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.loungeTween

/**
 * Featured "pick back up" card for the tablet home — a wide, easy touch target
 * with remaining time and an ember progress tick.
 */
@Composable
fun HeroCard(
    tile: LoungeTile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        when {
            focused -> 1.02f
            pressed -> 0.99f
            else -> 1f
        },
        animationSpec = loungeTween(220),
        label = "hero-scale",
    )
    val glow by animateFloatAsState(if (focused) 1f else 0f, animationSpec = loungeTween(), label = "hero-glow")
    val shape = RoundedCornerShape(metrics.tileCorner + 2.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(metrics.heroHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawFocusGlow(glow, metrics.tileCorner + 2.dp, metrics.focusStroke)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Box(Modifier.fillMaxSize().clip(shape)) {
        TileArt(tile = tile, corner = metrics.tileCorner + 2.dp, focused = focused, modifier = Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.horizontalGradient(
                            0f to Ember.Night.copy(alpha = 0.55f),
                            0.55f to Ember.Night.copy(alpha = 0.15f),
                            1f to Color.Transparent,
                        ),
                    )
                },
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Pick back up",
                style = MaterialTheme.typography.labelLarge,
                color = Ember.Amber,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = tile.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = Ember.Cream,
                    fontSize = 28.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (tile.subtitle != null) {
                        Text(
                            text = tile.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ember.CreamMuted,
                        )
                    }
                    Text(
                        text = "Resume",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Ember.AmberBright,
                    )
                }
            }
        }
        }
    }
}
