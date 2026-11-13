package app.lounge.ui.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import app.lounge.media.MediaEntry
import app.lounge.playback.PlaybackStore
import app.lounge.ui.glass.CloseGlyph
import app.lounge.ui.glass.GlassIconButton
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.glass.PlayPauseGlyph
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield

/**
 * Full-screen player. Play, pause, seek, and a close control.
 * Position is written to [PlaybackStore] while watching and again on the way out.
 */
@Composable
fun PlayerScreen(
    entry: MediaEntry,
    playback: PlaybackStore,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val metrics = LocalLoungeMetrics.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val startMs = remember(entry.id) { playback.resumePosition(entry.id) }
    val gate = remember(entry.id) { PlayerGate(startMs, entry.durationMs) }
    val playFocus = remember { FocusRequester() }

    val player = remember(entry.id) {
        buildPlayer(context.applicationContext, entry, startMs).also { exo ->
            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                        gate.ready = true
                    }
                    if (state == Player.STATE_ENDED) gate.ended = true
                }

                override fun onPlayerError(error: PlaybackException) {
                    gate.failed = true
                    Log.w(TAG, "playback failed for ${entry.id}", error)
                }
            })
        }
    }

    var position by remember(entry.id) { mutableLongStateOf(startMs) }
    var duration by remember(entry.id) { mutableLongStateOf(entry.durationMs) }
    var playing by remember(entry.id) { mutableStateOf(false) }
    var buffering by remember(entry.id) { mutableStateOf(true) }
    var failed by remember(entry.id) { mutableStateOf(false) }
    var scrubbing by remember(entry.id) { mutableStateOf(false) }
    var scrub by remember(entry.id) { mutableFloatStateOf(0f) }

    fun persist(durable: Boolean) {
        val reported = player.duration
        val durationMs = when {
            reported > 0L && reported != C.TIME_UNSET -> reported
            gate.duration > 0L -> gate.duration
            else -> entry.durationMs
        }
        val positionMs = player.currentPosition.coerceAtLeast(0L)
        if (positionMs <= 0L && player.playbackState == Player.STATE_IDLE && !gate.ready) return
        gate.position = positionMs
        if (durationMs > 0L) gate.duration = durationMs
        playback.save(entry.id, positionMs, durationMs, durable)
    }

    fun close() {
        persist(durable = true)
        onClose()
    }

    fun seekToFraction(fraction: Float) {
        val length = player.duration
        if (length <= 0L || length == C.TIME_UNSET) return
        val target = (length * fraction.coerceIn(0f, 1f)).toLong()
        player.seekTo(target)
        gate.position = target
        position = target
        persist(durable = true)
    }

    fun seekBy(deltaMs: Long) {
        val length = player.duration
        if (length <= 0L || length == C.TIME_UNSET) return
        seekToFraction((player.currentPosition + deltaMs).toFloat() / length.toFloat())
    }

    fun toggle() {
        if (gate.failed || player.playerError != null) {
            gate.failed = false
            failed = false
            player.prepare()
            player.play()
            return
        }
        failed = false
        when {
            player.playbackState == Player.STATE_ENDED || gate.ended -> {
                gate.ended = false
                player.seekTo(0L)
                player.play()
            }
            player.isPlaying -> player.pause()
            else -> player.play()
        }
    }

    BackHandler(onBack = ::close)

    DisposableEffect(player, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                persist(durable = true)
                player.pause()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            persist(durable = true)
            player.release()
        }
    }

    LaunchedEffect(player) {
        var ticks = 0
        while (isActive) {
            delay(250)
            ticks++
            val state = player.playbackState
            playing = player.isPlaying
            buffering = state == Player.STATE_BUFFERING
            failed = gate.failed
            if (state != Player.STATE_IDLE) gate.ready = gate.ready || state == Player.STATE_READY
            val reported = player.duration
            if (reported > 0L && reported != C.TIME_UNSET) {
                duration = reported
                gate.duration = reported
            }
            if (!scrubbing) {
                position = player.currentPosition.coerceAtLeast(0L)
                gate.position = position
            }
            if (gate.ready && ticks % 8 == 0) persist(durable = false)
        }
    }

    LaunchedEffect(Unit) {
        yield()
        runCatching { playFocus.requestFocus() }
    }

    val fraction = when {
        scrubbing -> scrub
        duration > 0L -> (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        else -> 0f
    }
    val buttonSize = if (metrics.isTelevision) 84.dp else 64.dp

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.MediaPlayPause, Key.MediaPlay, Key.MediaPause, Key.Spacebar -> {
                        toggle()
                        true
                    }
                    Key.MediaFastForward -> {
                        seekBy(10_000L)
                        true
                    }
                    Key.MediaRewind -> {
                        seekBy(-10_000L)
                        true
                    }
                    else -> false
                }
            },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    setShutterBackgroundColor(android.graphics.Color.BLACK)
                    keepScreenOn = true
                    isFocusable = false
                    isClickable = false
                    this.player = player
                }
            },
            update = { it.player = player },
        )
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(player) {
                    detectTapGestures { toggle() }
                },
        )
        if (!playing || failed || buffering) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    failed -> Text(
                        text = "Couldn't play this. Check the connection, then tap to try again.",
                        color = Ember.Cream,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                    buffering && !playing -> CircularProgressIndicator(color = Ember.Cream)
                    !playing -> PlayPauseGlyph(
                        playing = false,
                        modifier = Modifier.size(if (metrics.isTelevision) 72.dp else 54.dp),
                    )
                }
            }
        }
        GlassIconButton(
            description = "Close",
            onClick = ::close,
            size = if (metrics.isTelevision) 72.dp else 52.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(if (metrics.isTelevision) 28.dp else 16.dp),
        ) {
            CloseGlyph(Modifier.size(22.dp))
        }
        LiquidGlass(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = if (metrics.isTelevision) 72.dp else 16.dp)
                .padding(bottom = if (metrics.isTelevision) 28.dp else 12.dp)
                .fillMaxWidth(),
            corner = 28.dp,
            refract = false,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = entry.title,
                    color = Ember.Cream,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = metrics.rowTitleSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!entry.credit.isNullOrBlank()) {
                    Text(
                        text = entry.credit,
                        color = Ember.CreamDim,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GlassIconButton(
                        description = if (playing) "Pause" else "Play",
                        onClick = ::toggle,
                        focusRequester = playFocus,
                        size = buttonSize,
                    ) {
                        PlayPauseGlyph(
                            playing = playing,
                            modifier = Modifier.size(buttonSize * 0.42f),
                        )
                    }
                    Text(text = formatClock(position), color = Ember.Cream, style = MaterialTheme.typography.bodyMedium)
                    GlassScrubber(
                        fraction = fraction,
                        onScrubStart = {
                            scrubbing = true
                            scrub = fraction
                        },
                        onScrub = { scrub = it },
                        onScrubEnd = { value ->
                            scrubbing = false
                            scrub = value
                            seekToFraction(value)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatClock(duration),
                        color = Ember.CreamMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassScrubber(
    fraction: Float,
    onScrubStart: () -> Unit,
    onScrub: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalLoungeMetrics.current
    var dragging by remember { mutableStateOf(false) }
    val shown = fraction.coerceIn(0f, 1f)
    val dragFraction = remember { floatArrayOf(shown) }
    dragFraction[0] = shown
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier
            .height(if (metrics.isTelevision) 56.dp else 44.dp)
            .focusable(interactionSource = interaction)
            .semantics { contentDescription = "Seek" }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> {
                        if (shown <= 0.001f) {
                            false
                        } else {
                            onScrubEnd((shown - 0.02f).coerceIn(0f, 1f))
                            true
                        }
                    }
                    Key.DirectionRight -> {
                        onScrubEnd((shown + 0.02f).coerceIn(0f, 1f))
                        true
                    }
                    else -> false
                }
            }
            .pointerInput(onScrubEnd) {
                detectTapGestures { offset ->
                    val next = (offset.x / size.width).coerceIn(0f, 1f)
                    onScrubEnd(next)
                }
            }
            .pointerInput(onScrubEnd) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        onScrubStart()
                        val next = (offset.x / size.width).coerceIn(0f, 1f)
                        dragFraction[0] = next
                        onScrub(next)
                    },
                    onHorizontalDrag = { change, _ ->
                        val next = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragFraction[0] = next
                        onScrub(next)
                    },
                    onDragEnd = {
                        dragging = false
                        onScrubEnd(dragFraction[0])
                    },
                    onDragCancel = {
                        dragging = false
                        onScrubEnd(dragFraction[0])
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxWidth().height(if (focused || dragging) 8.dp else 5.dp)) {
            val radius = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(Color.White.copy(alpha = 0.28f), cornerRadius = radius)
            val fill = size.width * shown
            if (fill > 0f) {
                drawRoundRect(
                    color = if (focused) Ember.AmberBright else Ember.Amber,
                    size = Size(fill, size.height),
                    cornerRadius = radius,
                )
            }
            val knob = if (focused || dragging) 14.dp.toPx() else 10.dp.toPx()
            drawCircle(
                color = Ember.Cream,
                radius = knob / 2f,
                center = Offset(fill.coerceIn(knob / 2f, size.width - knob / 2f), size.height / 2f),
            )
        }
    }
}

private fun buildPlayer(context: android.content.Context, entry: MediaEntry, startMs: Long): ExoPlayer {
    val http = DefaultHttpDataSource.Factory()
        .setUserAgent("Lounge/0.2 (Android)")
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(12_000)
        .setReadTimeoutMs(20_000)
    val dataSource = DefaultDataSource.Factory(context, http)
    val audio = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .build()
    return ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
        .setAudioAttributes(audio, /* handleAudioFocus = */ true)
        .setHandleAudioBecomingNoisy(true)
        .build()
        .apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(entry.id)
                    .setUri(entry.uri)
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(entry.title).build())
                    .build(),
            )
            if (startMs > 0L) seekTo(startMs)
            prepare()
            playWhenReady = true
        }
}

private class PlayerGate(startMs: Long, durationMs: Long) {
    var ready: Boolean = false
    var failed: Boolean = false
    var ended: Boolean = false
    var position: Long = startMs
    var duration: Long = durationMs
}

private fun formatClock(ms: Long): String {
    if (ms <= 0L || ms == C.TIME_UNSET) return "0:00"
    val total = ms / 1000L
    val seconds = total % 60L
    val minutes = (total / 60L) % 60L
    val hours = total / 3600L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

private const val TAG = "Lounge"
