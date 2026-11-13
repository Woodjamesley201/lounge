package app.lounge.ui.glass

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.lounge.media.ArtMotif
import app.lounge.media.Thumbnails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun rememberLocalArtwork(uri: String?, enabled: Boolean, sizePx: Int = 480): ImageBitmap? {
    val context = LocalContext.current
    var image by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri, enabled, sizePx) {
        if (!enabled || uri.isNullOrBlank()) {
            image = null
            return@LaunchedEffect
        }
        val bitmap = withContext(Dispatchers.IO) {
            Thumbnails.load(context, Uri.parse(uri), sizePx)
        }
        image = bitmap?.asImageBitmap()
    }
    return image
}

/**
 * Picture laminated inside a glass tile. Edges fall away so the blurred
 * room shows through the rim. Motifs are original color studies, not posters.
 */
@Composable
fun TitleArtwork(
    motif: ArtMotif,
    seed: Int,
    image: ImageBitmap?,
    modifier: Modifier = Modifier,
    softenEdges: Boolean = true,
) {
    Canvas(
        modifier.graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        },
    ) {
        if (image != null) {
            drawCropped(image)
        } else {
            when (motif) {
                ArtMotif.MEADOW -> drawMeadow()
                ArtMotif.FROST -> drawFrost()
                ArtMotif.FORGE -> drawForge()
                ArtMotif.DREAM -> drawDream()
                ArtMotif.REEL -> drawReel(seed)
                ArtMotif.STILL -> drawStill()
            }
        }
        if (softenEdges) {
            drawRect(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Black,
                        0.62f to Color.Black,
                        0.86f to Color.Black.copy(alpha = 0.72f),
                        1f to Color.Black.copy(alpha = 0.18f),
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.minDimension * 0.85f,
                ),
                blendMode = androidx.compose.ui.graphics.BlendMode.DstIn,
            )
        }
    }
}

private fun DrawScope.drawCropped(image: ImageBitmap) {
    val srcW = image.width.toFloat().coerceAtLeast(1f)
    val srcH = image.height.toFloat().coerceAtLeast(1f)
    val scale = max(size.width / srcW, size.height / srcH)
    val dw = srcW * scale
    val dh = srcH * scale
    drawImage(
        image = image,
        dstOffset = IntOffset(
            ((size.width - dw) / 2f).roundToInt(),
            ((size.height - dh) / 2f).roundToInt(),
        ),
        dstSize = IntSize(dw.roundToInt().coerceAtLeast(1), dh.roundToInt().coerceAtLeast(1)),
    )
}

private fun DrawScope.drawMeadow() {
    drawRect(
        Brush.verticalGradient(
            listOf(Color(0xFFF6D7A4), Color(0xFFE3924E), Color(0xFF7EAE62), Color(0xFF245C3A)),
        ),
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFF6D8), Color(0xFFFFF6D8).copy(alpha = 0.15f), Color.Transparent),
            center = Offset(size.width * 0.78f, size.height * 0.22f),
            radius = size.minDimension * 0.28f,
        ),
        radius = size.minDimension * 0.28f,
        center = Offset(size.width * 0.78f, size.height * 0.22f),
    )
    val far = Path().apply {
        moveTo(0f, size.height * 0.58f)
        quadraticTo(size.width * 0.3f, size.height * 0.42f, size.width * 0.62f, size.height * 0.6f)
        quadraticTo(size.width * 0.84f, size.height * 0.7f, size.width, size.height * 0.52f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(far, Color(0xFF3E8A52).copy(alpha = 0.85f))
    val near = Path().apply {
        moveTo(0f, size.height * 0.74f)
        quadraticTo(size.width * 0.4f, size.height * 0.6f, size.width, size.height * 0.78f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(near, Color(0xFF1E4A30))
    repeat(5) { index ->
        val x = size.width * (0.18f + index * 0.15f)
        drawCircle(
            color = Color(0xFFFFF1C9).copy(alpha = 0.7f),
            radius = size.minDimension * 0.012f,
            center = Offset(x, size.height * (0.4f + (index % 3) * 0.06f)),
        )
    }
}

private fun DrawScope.drawFrost() {
    drawRect(
        Brush.verticalGradient(
            listOf(Color(0xFF07111C), Color(0xFF163A5C), Color(0xFFD5E6F2)),
        ),
    )
    drawCircle(
        color = Color(0xFFE7F2FA).copy(alpha = 0.9f),
        radius = size.minDimension * 0.08f,
        center = Offset(size.width * 0.72f, size.height * 0.22f),
    )
    val ridge = Path().apply {
        moveTo(0f, size.height * 0.78f)
        lineTo(size.width * 0.18f, size.height * 0.48f)
        lineTo(size.width * 0.34f, size.height * 0.66f)
        lineTo(size.width * 0.52f, size.height * 0.36f)
        lineTo(size.width * 0.7f, size.height * 0.62f)
        lineTo(size.width * 0.86f, size.height * 0.44f)
        lineTo(size.width, size.height * 0.7f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(ridge, Color(0xFF0C2234).copy(alpha = 0.88f))
    drawRect(
        brush = Brush.linearGradient(
            listOf(Color.Transparent, Color(0xFFE7A06A).copy(alpha = 0.85f), Color.Transparent),
            start = Offset(size.width * 0.15f, size.height * 0.7f),
            end = Offset(size.width * 0.85f, size.height * 0.2f),
        ),
        topLeft = Offset(size.width * 0.42f, size.height * 0.18f),
        size = Size(size.width * 0.04f, size.height * 0.7f),
    )
}

private fun DrawScope.drawForge() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1A120F), Color(0xFF3A2A28), Color(0xFF120E10))))
    drawRect(
        color = Color(0xFF5C6A74).copy(alpha = 0.35f),
        topLeft = Offset(0f, size.height * 0.18f),
        size = Size(size.width, size.height * 0.16f),
    )
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF2A201C), Color(0xFFFFD7B0), Color(0xFFE25A28), Color(0xFF2A201C)),
        ),
        topLeft = Offset(0f, size.height * 0.58f),
        size = Size(size.width, size.height * 0.08f),
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFE1B8).copy(alpha = 0.8f), Color.Transparent),
            center = Offset(size.width * 0.3f, size.height * 0.62f),
            radius = size.minDimension * 0.35f,
        ),
        radius = size.minDimension * 0.35f,
        center = Offset(size.width * 0.3f, size.height * 0.62f),
    )
    repeat(7) { index ->
        drawCircle(
            color = Color(0xFFFFE6C2).copy(alpha = 0.75f),
            radius = size.minDimension * 0.01f,
            center = Offset(size.width * (0.2f + index * 0.1f), size.height * (0.3f + (index % 4) * 0.08f)),
        )
    }
}

private fun DrawScope.drawDream() {
    drawRect(Brush.radialGradient(listOf(Color(0xFF3A2468), Color(0xFF140C22))))
    val center = Offset(size.width * 0.56f, size.height * 0.48f)
    repeat(4) { index ->
        drawCircle(
            color = Color(0xFFD7B56A).copy(alpha = 0.35f - index * 0.05f),
            radius = size.minDimension * (0.16f + index * 0.12f),
            center = center,
            style = Stroke(width = size.minDimension * 0.015f),
        )
    }
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFF4E2FF), Color(0xFFB48CFF).copy(alpha = 0.2f), Color.Transparent),
            center = center,
            radius = size.minDimension * 0.22f,
        ),
        radius = size.minDimension * 0.22f,
        center = center,
    )
    drawArcLike(center)
}

private fun DrawScope.drawArcLike(center: Offset) {
    drawArc(
        color = Color(0xFFE6C27A).copy(alpha = 0.8f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - size.minDimension * 0.34f, center.y - size.minDimension * 0.2f),
        size = Size(size.minDimension * 0.7f, size.minDimension * 0.45f),
        style = Stroke(width = size.minDimension * 0.02f),
    )
}

private fun DrawScope.drawReel(seed: Int) {
    val accents = listOf(
        Color(0xFFE6B15E),
        Color(0xFF7EB6E6),
        Color(0xFFE07A8A),
        Color(0xFF8FCB8A),
        Color(0xFFC9A6E0),
    )
    val accent = accents[(seed and 0x7FFF_FFFF) % accents.size]
    drawRect(Brush.linearGradient(listOf(Color(0xFF10141C), Color(0xFF1C2432), accent.copy(alpha = 0.45f))))
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.42f,
        ),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
    )
    drawCircle(
        color = accent.copy(alpha = 0.9f),
        radius = size.minDimension * 0.28f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        style = Stroke(width = size.minDimension * 0.02f),
    )
}

private fun DrawScope.drawStill() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF1A2438), Color(0xFF0E1524))))
    val inset = size.minDimension * 0.12f
    val paneW = (size.width - inset * 3f) / 2f
    val paneH = (size.height - inset * 3f) / 2f
    val lights = listOf(
        Color(0xFFF0C48A),
        Color(0xFF9EBFDF),
        Color(0xFFE7A0B4),
        Color(0xFFB7D0A4),
    )
    var index = 0
    for (row in 0..1) {
        for (column in 0..1) {
            drawRoundRect(
                color = lights[index].copy(alpha = 0.8f),
                topLeft = Offset(inset + column * (paneW + inset), inset + row * (paneH + inset)),
                size = Size(paneW, paneH),
                cornerRadius = CornerRadius(10f, 10f),
            )
            index++
        }
    }
}
