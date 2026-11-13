package app.lounge.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.lounge.ui.glass.LocalConservativeGraphics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Original Lounge mark: a floor lamp and a pool of warm light.
 * Not a TV, monitor, or any commercial-OS glyph.
 */
@Composable
fun EmberLamp(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    lit: Boolean = true,
) {
    val conservative = LocalConservativeGraphics.current
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        if (lit) {
            val glow = Size(w, h * 0.55f)
            val glowAt = Offset(cx - w * 0.5f, h * 0.38f)
            if (conservative) {
                drawOval(
                    color = Ember.Amber.copy(alpha = 0.22f),
                    topLeft = glowAt,
                    size = glow,
                )
            } else {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Ember.AmberBright.copy(alpha = 0.45f),
                            Ember.Amber.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                        center = Offset(cx, h * 0.58f),
                        radius = w * 0.55f,
                    ),
                    topLeft = glowAt,
                    size = glow,
                )
            }
        }
        // Shade (shallow cone)
        val shade = Path().apply {
            moveTo(cx - w * 0.18f, h * 0.22f)
            lineTo(cx + w * 0.18f, h * 0.22f)
            lineTo(cx + w * 0.34f, h * 0.42f)
            lineTo(cx - w * 0.34f, h * 0.42f)
            close()
        }
        drawPath(shade, Ember.Amber)
        // Inner lip of the shade
        drawLine(
            color = Ember.AmberBright.copy(alpha = 0.7f),
            start = Offset(cx - w * 0.30f, h * 0.42f),
            end = Offset(cx + w * 0.30f, h * 0.42f),
            strokeWidth = h * 0.035f,
            cap = StrokeCap.Round,
        )
        // Stem
        drawLine(
            color = Ember.CreamMuted,
            start = Offset(cx, h * 0.42f),
            end = Offset(cx, h * 0.78f),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round,
        )
        // Base
        drawRoundRect(
            color = Ember.CreamMuted,
            topLeft = Offset(cx - w * 0.16f, h * 0.78f),
            size = Size(w * 0.32f, h * 0.07f),
            cornerRadius = CornerRadius(h * 0.04f, h * 0.04f),
        )
        // Stem joint
        drawCircle(Ember.AmberDeep, radius = w * 0.035f, center = Offset(cx, h * 0.42f))
    }
}

@Composable
fun LoungeWordmark(
    modifier: Modifier = Modifier,
    titleSize: TextUnit,
    lampSize: Dp = 28.dp,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EmberLamp(size = lampSize)
        Text(
            text = "Lounge",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = titleSize,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = Ember.Cream,
        )
    }
}
