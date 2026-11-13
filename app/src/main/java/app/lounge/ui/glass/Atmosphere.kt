package app.lounge.ui.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import app.lounge.ui.theme.Ember

/**
 * True on Samsung and Android 16, where stacked radial shaders abort the
 * GPU during the first frame. Studio previews leave this false.
 */
val LocalConservativeGraphics = staticCompositionLocalOf { false }

/**
 * Dusk room behind the glass. On devices that can take it, the same function
 * is drawn full-screen and, clipped inside a panel, as the slice that panel
 * refracts. Samsung and Android 16 get a linear dusk only — the radial stack
 * was still killing the Galaxy Tab A9 after platform blur was removed.
 */
@Composable
fun Atmosphere(modifier: Modifier = Modifier) {
    val conservative = LocalConservativeGraphics.current
    Canvas(modifier.fillMaxSize()) {
        if (conservative) drawPlainRoom() else drawAtmosphere(Offset.Zero, size)
    }
}

private fun DrawScope.drawPlainRoom() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A2748),
                Color(0xFF10182E),
                Color(0xFF14110F),
            ),
        ),
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Ember.Amber.copy(alpha = 0.16f), Color.Transparent),
            startY = 0f,
            endY = size.height * 0.45f,
        ),
    )
}

fun DrawScope.drawAtmosphere(origin: Offset, root: Size) {
    if (root.width <= 0f || root.height <= 0f) return
    fun at(x: Float, y: Float) = Offset(root.width * x - origin.x, root.height * y - origin.y)
    val reach = minOf(root.width, root.height)

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1A2748),
                Color(0xFF10182E),
                Color(0xFF14110F),
            ),
            start = at(0f, 0f),
            end = at(0f, 1f),
        ),
    )
    blob(at(-0.02f, -0.02f), reach * 0.95f, Ember.Amber.copy(alpha = 0.55f))
    blob(at(0.96f, 0.06f), reach * 0.72f, Color(0xFF8EB6F2).copy(alpha = 0.42f))
    blob(at(0.12f, 0.82f), reach * 0.62f, Color(0xFF6FBF78).copy(alpha = 0.34f))
    blob(at(0.86f, 0.58f), reach * 0.58f, Color(0xFFE07A3C).copy(alpha = 0.40f))
    blob(at(0.46f, 0.98f), reach * 0.70f, Color(0xFF7A5CDA).copy(alpha = 0.36f))
    blob(at(0.62f, 0.28f), reach * 0.36f, Color(0xFFF0D7B0).copy(alpha = 0.16f))

    rotate(degrees = -16f, pivot = at(0.5f, 0.42f)) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f),
                    Ember.AmberBright.copy(alpha = 0.10f),
                    Color.Transparent,
                ),
                startX = at(0f, 0f).x,
                endX = at(1f, 0f).x,
            ),
            topLeft = at(-0.1f, 0.36f),
            size = Size(root.width * 1.2f, root.height * 0.07f),
        )
    }

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color(0xFF05070E).copy(alpha = 0.55f)),
            center = at(0.5f, 0.45f),
            radius = reach * 0.95f,
        ),
    )
}

private fun DrawScope.blob(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
