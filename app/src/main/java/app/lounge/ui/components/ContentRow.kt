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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
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
import app.lounge.media.MediaEntry
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.glass.TitleArtwork
import app.lounge.ui.glass.glassFocus
import app.lounge.ui.glass.rememberLocalArtwork
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.loungeTween

/**
 * A labeled shelf. Focus is a glass rim and a slight scale, visible from the couch.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentRow(
    title: String,
    tiles: List<MediaEntry>,
    modifier: Modifier = Modifier,
    square: Boolean = false,
    initialFocusIndex: Int? = null,
    initialFocusRequester: FocusRequester? = null,
    onTileClick: (MediaEntry) -> Unit = {},
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
            contentPadding = PaddingValues(horizontal = metrics.screenPadding - metrics.focusPad),
            horizontalArrangement = Arrangement.spacedBy(metrics.tileGap),
        ) {
            itemsIndexed(tiles, key = { _, tile -> tile.id }) { index, tile ->
                val requester = if (index == initialFocusIndex) initialFocusRequester else null
                TileCard(
                    entry = tile,
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
    entry: MediaEntry,
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
    val scale by animateFloatAsState(
        targetValue = when {
            focused -> metrics.focusScale
            pressed -> 0.98f
            else -> 1f
        },
        animationSpec = loungeTween(),
        label = "tile-scale",
    )
    val emphasis by animateFloatAsState(
        if (focused) 1f else 0f,
        loungeTween(),
        label = "tile-glass",
    )
    val artwork = rememberLocalArtwork(
        uri = entry.uri,
        enabled = entry.local,
        sizePx = if (metrics.isTelevision) 720 else 480,
    )
    val description = entry.subtitle?.let { "${entry.title}, $it" } ?: entry.title

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
            .glassFocus(emphasis, metrics.tileCorner, metrics.focusStroke)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
    ) {
        LiquidGlass(
            modifier = Modifier
                .width(width)
                .height(height),
            corner = metrics.tileCorner,
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
                    .height(if (metrics.isTelevision) 48.dp else 36.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                        ),
                    ),
            )
            val progress = entry.progress
            if (progress != null) {
                GlassProgress(
                    progress = progress,
                    emphasized = focused,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(if (metrics.isTelevision) 12.dp else 8.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = metrics.tileTitleSize,
            fontWeight = FontWeight.Medium,
            color = if (focused) Ember.AmberBright else Ember.Cream,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (entry.subtitle != null) {
            Text(
                text = entry.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Ember.CreamDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun GlassProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Canvas(modifier.height(4.dp)) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)
        drawRoundRect(Color.White.copy(alpha = 0.28f), cornerRadius = radius)
        val width = size.width * progress.coerceIn(0f, 1f)
        if (width > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(Ember.Amber, if (emphasized) Color.White else Ember.AmberBright),
                ),
                size = Size(width, size.height),
                cornerRadius = radius,
            )
        }
    }
}
