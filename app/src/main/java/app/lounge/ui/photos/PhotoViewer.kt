package app.lounge.ui.photos

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.lounge.media.MediaEntry
import app.lounge.media.Thumbnails
import app.lounge.ui.glass.Atmosphere
import app.lounge.ui.glass.ChevronGlyph
import app.lounge.ui.glass.CloseGlyph
import app.lounge.ui.glass.GlassIconButton
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

@Composable
fun PhotoViewer(
    photos: List<MediaEntry>,
    initialIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (photos.isEmpty()) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    val metrics = LocalLoungeMetrics.current
    val context = LocalContext.current
    var index by remember { mutableIntStateOf(initialIndex.coerceIn(0, photos.lastIndex)) }
    val entry = photos[index]
    var image by remember(entry.id) { mutableStateOf<ImageBitmap?>(null) }
    val closeFocus = remember { FocusRequester() }
    var drag by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(entry.uri) {
        image = null
        val bitmap = withContext(Dispatchers.IO) {
            Thumbnails.load(context, Uri.parse(entry.uri), 1800)
        }
        image = bitmap?.asImageBitmap()
    }
    LaunchedEffect(Unit) {
        yield()
        runCatching { closeFocus.requestFocus() }
    }
    BackHandler(onBack = onClose)

    fun step(delta: Int) {
        index = (index + delta).coerceIn(0, photos.lastIndex)
    }

    Box(
        modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft, Key.MediaRewind -> {
                        step(-1)
                        true
                    }
                    Key.DirectionRight, Key.MediaFastForward -> {
                        step(1)
                        true
                    }
                    else -> false
                }
            }
            .pointerInput(photos.size) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> drag += amount },
                    onDragEnd = {
                        when {
                            drag > 72f -> step(-1)
                            drag < -72f -> step(1)
                        }
                        drag = 0f
                    },
                    onDragCancel = { drag = 0f },
                )
            },
    ) {
        Atmosphere(Modifier.fillMaxSize())
        image?.let {
            Image(
                bitmap = it,
                contentDescription = entry.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
        GlassIconButton(
            description = "Close",
            onClick = onClose,
            focusRequester = closeFocus,
            size = if (metrics.isTelevision) 72.dp else 52.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(if (metrics.isTelevision) 28.dp else 16.dp),
        ) {
            CloseGlyph(Modifier.size(22.dp))
        }
        if (photos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = if (metrics.isTelevision) 36.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlassIconButton(
                    description = "Previous photo",
                    onClick = { step(-1) },
                    size = if (metrics.isTelevision) 72.dp else 48.dp,
                ) {
                    ChevronGlyph(left = true, modifier = Modifier.size(22.dp))
                }
                GlassIconButton(
                    description = "Next photo",
                    onClick = { step(1) },
                    size = if (metrics.isTelevision) 72.dp else 48.dp,
                ) {
                    ChevronGlyph(left = false, modifier = Modifier.size(22.dp))
                }
            }
        }
        LiquidGlass(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = if (metrics.isTelevision) 72.dp else 16.dp)
                .padding(bottom = if (metrics.isTelevision) 28.dp else 12.dp)
                .fillMaxWidth(),
            corner = 24.dp,
            refract = false,
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    text = entry.title,
                    color = Ember.Cream,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${index + 1} of ${photos.size}",
                    color = Ember.CreamDim,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
