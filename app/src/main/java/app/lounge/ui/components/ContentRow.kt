package app.lounge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.loungeTween

enum class TileKind { FILM, SHOW, PHOTO }

data class LoungeTile(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val progress: Float? = null,
    val kind: TileKind = TileKind.FILM,
)

data class TilePalette(
    val deep: Color,
    val mid: Color,
    val light: Color,
)

fun paletteFor(id: String): TilePalette {
    val palettes = listOf(
        TilePalette(Color(0xFF3A2218), Color(0xFF8C4A2E), Color(0xFFE8A05C)),
        TilePalette(Color(0xFF241820), Color(0xFF6A3A52), Color(0xFFE0A8B0)),
        TilePalette(Color(0xFF1A2418), Color(0xFF3E5A38), Color(0xFFC8C070)),
        TilePalette(Color(0xFF1C1A16), Color(0xFF4A4034), Color(0xFFD4C4A8)),
        TilePalette(Color(0xFF2A1810), Color(0xFFA05630), Color(0xFFF0B070)),
        TilePalette(Color(0xFF2A1014), Color(0xFF7A3038), Color(0xFFE09080)),
        TilePalette(Color(0xFF2A2410), Color(0xFF8A6A28), Color(0xFFF0D078)),
        TilePalette(Color(0xFF10201E), Color(0xFF2A5854), Color(0xFFA8D0C4)),
    )
    val index = (id.hashCode().and(0x7FFF_FFFF)) % palettes.size
    return palettes[index]
}

/**
 * A labeled horizontal row of tiles. Owner: UI Isaiah.
 *
 * Focus is visible without hover: a warm ember ring, a soft glow, and a
 * gentle scale. D-pad / remote focus brings the tile into view.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentRow(
    title: String,
    tiles: List<LoungeTile>,
    modifier: Modifier = Modifier,
    square: Boolean = false,
    initialFocusIndex: Int? = null,
    initialFocusRequester: FocusRequester? = null,
    onTileClick: (LoungeTile) -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val listState = rememberLazyListState()
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontSize = metrics.rowTitleSize,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            color = Ember.Cream,
            modifier = Modifier.padding(horizontal = metrics.screenPadding),
        )
        Spacer(Modifier.height(if (metrics.isTelevision) 16.dp else 10.dp))
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup(),
            contentPadding = PaddingValues(
                horizontal = metrics.screenPadding - metrics.focusPad,
            ),
            horizontalArrangement = Arrangement.spacedBy(metrics.tileGap),
        ) {
            itemsIndexed(tiles, key = { _, tile -> tile.id }) { index, tile ->
                val requester = if (index == initialFocusIndex) initialFocusRequester else null
                TileCard(
                    tile = tile,
                    square = square,
                    focusRequester = requester,
                    onClick = { onTileClick(tile) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileCard(
    tile: LoungeTile,
    modifier: Modifier = Modifier,
    square: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val width = if (square) metrics.photoSize else metrics.tileWidth
    val height = if (square) metrics.photoSize else metrics.tileHeight
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(focused) {
        if (focused) bringIntoView.bringIntoView()
    }
    val targetScale = when {
        focused -> metrics.focusScale
        pressed -> 0.98f
        else -> 1f
    }
    val scale by animateFloatAsState(targetScale, animationSpec = loungeTween(), label = "tile-scale")
    val glow by animateFloatAsState(if (focused) 1f else 0f, animationSpec = loungeTween(), label = "tile-glow")

    Column(
        modifier = modifier
            .width(width + metrics.focusPad * 2)
            .padding(metrics.focusPad)
            .bringIntoViewRequester(bringIntoView)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawFocusGlow(glow, metrics.tileCorner, metrics.focusStroke)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = tile.subtitle?.let { "${tile.title}, $it" } ?: tile.title },
    ) {
        TileArt(
            tile = tile,
            modifier = Modifier
                .width(width)
                .height(height),
            corner = metrics.tileCorner,
            focused = focused,
        )
        Spacer(Modifier.height(if (metrics.isTelevision) 12.dp else 8.dp))
        Text(
            text = tile.title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = metrics.tileTitleSize,
            fontWeight = FontWeight.Medium,
            color = if (focused) Ember.AmberBright else Ember.Cream,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (tile.subtitle != null) {
            Text(
                text = tile.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Ember.CreamDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun TileArt(
    tile: LoungeTile,
    modifier: Modifier = Modifier,
    corner: Dp = 14.dp,
    focused: Boolean = false,
) {
    val palette = remember(tile.id) { paletteFor(tile.id) }
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(palette.deep, palette.mid),
                    start = Offset.Zero,
                    end = Offset(400f, 800f),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            when (tile.kind) {
                TileKind.PHOTO -> {
                    // A window: warm panes, dark muntins — looking out of a room.
                    val inset = size.minDimension * 0.14f
                    val paneW = (size.width - inset * 3f) / 2f
                    val paneH = (size.height - inset * 3f) / 2f
                    val glow = palette.light.copy(alpha = 0.55f)
                    for (r in 0..1) for (c in 0..1) {
                        val x = inset + c * (paneW + inset)
                        val y = inset + r * (paneH + inset)
                        drawRoundRect(
                            color = glow,
                            topLeft = Offset(x, y),
                            size = Size(paneW, paneH),
                            cornerRadius = CornerRadius(8f, 8f),
                        )
                    }
                }
                TileKind.SHOW -> {
                    // Two stacked "episodes" as soft bars, like stacked stills.
                    drawRoundRect(
                        color = palette.light.copy(alpha = 0.22f),
                        topLeft = Offset(size.width * 0.12f, size.height * 0.14f),
                        size = Size(size.width * 0.76f, size.height * 0.36f),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                    drawRoundRect(
                        color = palette.light.copy(alpha = 0.38f),
                        topLeft = Offset(size.width * 0.18f, size.height * 0.36f),
                        size = Size(size.width * 0.76f, size.height * 0.42f),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                }
                TileKind.FILM -> {
                    // A wide cinematic light and a soft off-center lamp bloom.
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(palette.light.copy(alpha = 0.45f), Color.Transparent),
                            center = Offset(size.width * 0.78f, size.height * 0.22f),
                            radius = size.minDimension * 0.7f,
                        ),
                    )
                    val barH = size.height * 0.08f
                    drawRect(palette.deep.copy(alpha = 0.45f), topLeft = Offset(0f, 0f), size = Size(size.width, barH))
                    drawRect(palette.deep.copy(alpha = 0.45f), topLeft = Offset(0f, size.height - barH), size = Size(size.width, barH))
                }
            }
        }
        // Bottom fade so a future title-on-art would sit cleanly; also grounds the progress.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Ember.Night.copy(alpha = 0.55f)),
                    ),
                ),
        )
        val progress = tile.progress
        if (progress != null) {
            ProgressEmber(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                emphasized = focused,
            )
        }
    }
}

@Composable
private fun ProgressEmber(
    progress: Float,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Canvas(modifier.height(4.dp)) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(Ember.Cream.copy(alpha = 0.22f), cornerRadius = radius)
        val w = size.width * progress
        if (w > 0f) {
            drawRoundRect(
                color = if (emphasized) Ember.AmberBright else Ember.Amber,
                size = Size(w, size.height),
                cornerRadius = radius,
            )
        }
    }
}

/**
 * Warm ember ring and a soft outer glow. Drawn behind the card so focus is
 * obvious from the couch, and it does not depend on hover or a mouse.
 */
fun Modifier.drawFocusGlow(glow: Float, corner: Dp, stroke: Dp): Modifier = this.then(
    Modifier.drawBehind {
        if (glow <= 0.01f) return@drawBehind
        val cornerPx = corner.toPx()
        val strokePx = stroke.toPx()
        val inset = strokePx / 2f
        // Soft outer wash
        val wash = Ember.Amber.copy(alpha = 0.22f * glow)
        drawRoundRect(
            color = wash,
            topLeft = Offset(-strokePx * 1.6f, -strokePx * 1.6f),
            size = Size(size.width + strokePx * 3.2f, size.height + strokePx * 3.2f),
            cornerRadius = CornerRadius(cornerPx + strokePx * 1.6f, cornerPx + strokePx * 1.6f),
        )
        drawRoundRect(
            color = Ember.AmberBright.copy(alpha = 0.95f * glow),
            topLeft = Offset(inset, inset),
            size = Size(size.width - strokePx, size.height - strokePx),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
            style = Stroke(width = strokePx),
        )
    },
)
