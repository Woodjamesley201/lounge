package app.lounge.ui.glass

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.lounge.ui.theme.Ember

/**
 * A pane of Lounge glass.
 *
 * On API 31+ the pane blurs a slice of [drawAtmosphere] with [RenderEffect],
 * the same dusk painted behind the UI, so the material has something to
 * refract. Older APIs keep the tint, the specular streak, and the rim,
 * without the blur. Previews skip the platform effect.
 *
 * [refract] is false over a video or photo, where the surface should stay
 * translucent and let the picture show through instead of painting the room.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    emphasis: Float = 0f,
    refract: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(corner)
    val density = LocalDensity.current.density
    val inspection = LocalInspectionMode.current
    val blur = remember(inspection, density) { frostEffect(inspection, density) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var root by remember { mutableStateOf(IntSize.Zero) }
    var placed by remember { mutableStateOf(false) }

    Box(
        modifier
            .onGloballyPositioned { coords ->
                origin = coords.positionInRoot()
                root = coords.findRootCoordinates().size
                placed = true
            }
            .graphicsLayer {
                shadowElevation = (8.dp + 10.dp * emphasis).toPx()
                this.shape = shape
                clip = false
                ambientShadowColor = Color.Black.copy(alpha = 0.42f)
                spotShadowColor = Color.Black.copy(alpha = 0.55f)
            },
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.shape = shape
                    clip = true
                    if (refract && blur != null) {
                        renderEffect = blur
                    }
                },
        ) {
            if (refract && placed && root.width > 0 && root.height > 0) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    drawAtmosphere(origin, Size(root.width.toFloat(), root.height.toFloat()))
                }
            }
            val veil = when {
                !refract -> Color.Black.copy(alpha = 0.40f)
                blur != null -> Color.White.copy(alpha = 0.08f)
                else -> Color(0xFFD7E3F4).copy(alpha = 0.22f)
            }
            Box(Modifier.fillMaxSize().background(veil))
        }
        Box(Modifier.clip(shape)) {
            content()
            GlassSheen(corner = corner, emphasis = emphasis)
        }
    }
}

private fun frostEffect(
    inspection: Boolean,
    density: Float,
): androidx.compose.ui.graphics.RenderEffect? {
    if (inspection || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    return runCatching {
        val radius = (16f * density).coerceIn(12f, 34f)
        RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP).asComposeRenderEffect()
    }.getOrNull()
}

@Composable
private fun BoxScope.GlassSheen(corner: Dp, emphasis: Float) {
    androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
        val stroke = (1.15.dp.toPx() + emphasis * 0.8.dp.toPx())
        val inset = stroke / 2f
        val cornerPx = corner.toPx().coerceAtLeast(stroke)
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.34f + 0.18f * emphasis),
                0.16f to Color.White.copy(alpha = 0.08f),
                0.48f to Color.Transparent,
                1f to Color.White.copy(alpha = 0.05f),
            ),
        )
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.38f to Color.White.copy(alpha = 0.02f),
                    0.5f to Color.White.copy(alpha = 0.16f + 0.08f * emphasis),
                    0.62f to Color.Transparent,
                    1f to Color.Transparent,
                ),
                start = Offset(0f, size.height * 0.05f),
                end = Offset(size.width, size.height * 0.55f),
            ),
        )
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.82f),
                    Color.White.copy(alpha = 0.18f),
                    Ember.AmberBright.copy(alpha = 0.28f + 0.45f * emphasis),
                    Color.White.copy(alpha = 0.34f),
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(
                (cornerPx - inset).coerceAtLeast(0f),
                (cornerPx - inset).coerceAtLeast(0f),
            ),
            style = Stroke(width = stroke),
        )
    }
}

/** Outer focus ring for the couch. Drawn in the tile's padding, not on the art. */
fun Modifier.glassFocus(emphasis: Float, corner: Dp, stroke: Dp): Modifier = this.then(
    Modifier.drawBehind {
        if (emphasis <= 0.02f) return@drawBehind
        val cornerPx = corner.toPx()
        val strokePx = stroke.toPx()
        drawRoundRect(
            color = Color.White.copy(alpha = 0.20f * emphasis),
            topLeft = Offset(-strokePx * 2.1f, -strokePx * 2.1f),
            size = Size(size.width + strokePx * 4.2f, size.height + strokePx * 4.2f),
            cornerRadius = CornerRadius(cornerPx + strokePx * 2f, cornerPx + strokePx * 2f),
        )
        drawRoundRect(
            color = Ember.AmberBright.copy(alpha = 0.95f * emphasis),
            topLeft = Offset(-strokePx * 0.15f, -strokePx * 0.15f),
            size = Size(size.width + strokePx * 0.3f, size.height + strokePx * 0.3f),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
            style = Stroke(width = strokePx),
        )
    },
)
