package app.lounge.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.lounge.media.LibrarySnapshot
import app.lounge.media.MediaEntry
import app.lounge.media.OpenMovies
import app.lounge.media.assembleLibrary
import app.lounge.playback.PlaybackRecord
import app.lounge.ui.components.ContentRow
import app.lounge.ui.components.HeroCard
import app.lounge.ui.components.LoungeHeader
import app.lounge.ui.glass.Atmosphere
import app.lounge.ui.glass.GlassTextButton
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import app.lounge.ui.theme.LoungeTheme
import kotlinx.coroutines.yield

@Composable
fun LoungeHome(
    snapshot: LibrarySnapshot,
    eyebrow: String,
    showClock: Boolean,
    requestInitialFocus: Boolean,
    needsSettings: Boolean,
    onPlay: (MediaEntry) -> Unit,
    onOpenPhoto: (MediaEntry) -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onTitleLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalLoungeMetrics.current
    val primaryFocus = remember { FocusRequester() }
    val showOfflinePanel = !snapshot.online && snapshot.localVideos.isEmpty()
    val hero = snapshot.hero
    LaunchedEffect(requestInitialFocus, hero?.id, showOfflinePanel, snapshot.accessGranted) {
        if (!requestInitialFocus) return@LaunchedEffect
        yield()
        runCatching { primaryFocus.requestFocus() }
    }

    Box(modifier.fillMaxSize()) {
        Atmosphere(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(vertical = metrics.screenPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.rowGap),
        ) {
            LoungeHeader(
                eyebrow = eyebrow,
                onTitleLongPress = onTitleLongPress,
                showClock = showClock,
            )
            if (!snapshot.online && !showOfflinePanel) {
                Text(
                    text = "Open movies need a connection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ember.CreamMuted,
                    modifier = Modifier.padding(horizontal = metrics.screenPadding),
                )
            }
            if (hero != null) {
                HeroCard(
                    entry = hero.shelf(snapshot.online),
                    kicker = if (hero.progress != null) "Pick back up" else "Play",
                    action = heroAction(hero, snapshot.online),
                    modifier = Modifier.padding(horizontal = metrics.screenPadding),
                    focusRequester = if (requestInitialFocus) primaryFocus else null,
                    onClick = { onPlay(hero) },
                )
            }
            if (snapshot.continueWatching.isNotEmpty()) {
                ContentRow(
                    title = "Continue watching",
                    tiles = snapshot.continueWatching.map { it.shelf(snapshot.online) },
                    initialFocusIndex = if (requestInitialFocus && hero == null) 0 else null,
                    initialFocusRequester = if (requestInitialFocus && hero == null) primaryFocus else null,
                    onTileClick = onPlay,
                )
            }
            if (showOfflinePanel) {
                OfflinePanel(
                    hasAccess = snapshot.accessGranted,
                    needsSettings = needsSettings,
                    onRetry = onRetry,
                    onRequestAccess = onRequestAccess,
                    onOpenSettings = onOpenSettings,
                    focusRequester = if (requestInitialFocus && hero == null && snapshot.continueWatching.isEmpty()) {
                        primaryFocus
                    } else {
                        null
                    },
                    modifier = Modifier.padding(horizontal = metrics.screenPadding),
                )
            }
            if (snapshot.online || !showOfflinePanel) {
                ContentRow(
                    title = "Open movies",
                    tiles = snapshot.movies.map { it.shelf(snapshot.online) },
                    initialFocusIndex = if (
                        requestInitialFocus && hero == null && snapshot.continueWatching.isEmpty() && !showOfflinePanel
                    ) {
                        0
                    } else {
                        null
                    },
                    initialFocusRequester = if (
                        requestInitialFocus && hero == null && snapshot.continueWatching.isEmpty() && !showOfflinePanel
                    ) {
                        primaryFocus
                    } else {
                        null
                    },
                    onTileClick = onPlay,
                )
            }
            if (snapshot.scanning) {
                Note("Looking through this tablet…")
            }
            if (snapshot.localVideos.isNotEmpty()) {
                ContentRow(
                    title = "On this tablet",
                    tiles = snapshot.localVideos,
                    onTileClick = onPlay,
                )
            } else if (snapshot.videosAllowed && !snapshot.scanning && !showOfflinePanel) {
                Note("No videos saved on this tablet.")
            }
            if (snapshot.photos.isNotEmpty()) {
                ContentRow(
                    title = "Photos",
                    tiles = snapshot.photos,
                    square = true,
                    onTileClick = onOpenPhoto,
                )
            } else if (snapshot.photosAllowed && !snapshot.scanning && !showOfflinePanel) {
                Note("No photos on this tablet.")
            }
            if (!snapshot.accessGranted && !showOfflinePanel) {
                PermissionCard(
                    needsSettings = needsSettings,
                    onRequestAccess = onRequestAccess,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(horizontal = metrics.screenPadding),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun MediaEntry.shelf(online: Boolean): MediaEntry {
    if (local || online || progress != null) return this
    return copy(subtitle = "Needs a connection")
}

private fun heroAction(entry: MediaEntry, online: Boolean): String? {
    if (!entry.local && !online) return "Needs a connection"
    return if (entry.progress != null) "Resume" else null
}

@Composable
private fun Note(text: String) {
    val metrics = LocalLoungeMetrics.current
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Ember.CreamDim,
        modifier = Modifier.padding(horizontal = metrics.screenPadding),
    )
}

@Composable
private fun OfflinePanel(
    hasAccess: Boolean,
    needsSettings: Boolean,
    onRetry: () -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    focusRequester: FocusRequester?,
    modifier: Modifier = Modifier,
) {
    LiquidGlass(modifier = modifier.fillMaxWidth(), corner = 28.dp) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "You are offline",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = Ember.Cream,
            )
            Text(
                text = if (hasAccess) {
                    "No videos saved on this tablet. Big Buck Bunny, Sintel, Tears of Steel, and Elephants Dream play when this tablet is back online."
                } else {
                    "Big Buck Bunny, Sintel, Tears of Steel, and Elephants Dream are free films from the Blender Foundation. They play when this tablet is online. Videos and photos already saved here can play too, and they stay on the device."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Ember.CreamMuted,
            )
            GlassTextButton(
                text = "Check connection",
                onClick = onRetry,
                focusRequester = focusRequester,
            )
            if (!hasAccess) {
                AccessAction(needsSettings, onRequestAccess, onOpenSettings)
            }
        }
    }
}

@Composable
private fun PermissionCard(
    needsSettings: Boolean,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LiquidGlass(modifier = modifier.fillMaxWidth(), corner = 28.dp) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Videos and photos on this tablet",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = Ember.Cream,
            )
            Text(
                text = if (needsSettings) {
                    "Videos and photos are blocked for Lounge. You can allow them in system settings."
                } else {
                    "Lounge can show videos and photos already saved on this tablet, next to the open movies. They stay on the device."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Ember.CreamMuted,
            )
            AccessAction(needsSettings, onRequestAccess, onOpenSettings)
        }
    }
}

@Composable
private fun AccessAction(
    needsSettings: Boolean,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (needsSettings) {
        GlassTextButton(text = "Open settings", onClick = onOpenSettings)
    } else {
        GlassTextButton(text = "Show what is on this tablet", onClick = onRequestAccess)
    }
}

@Preview(name = "Tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun TabletGlassPreview() {
    LoungeTheme(isTelevision = false) {
        LoungeHome(
            snapshot = previewSnapshot(),
            eyebrow = "On this tablet",
            showClock = false,
            requestInitialFocus = false,
            needsSettings = false,
            onPlay = {},
            onOpenPhoto = {},
            onRequestAccess = {},
            onOpenSettings = {},
            onRetry = {},
            onTitleLongPress = {},
        )
    }
}

@Preview(name = "Television", widthDp = 1920, heightDp = 1080)
@Composable
private fun TelevisionGlassPreview() {
    LoungeTheme(isTelevision = true) {
        LoungeHome(
            snapshot = previewSnapshot(),
            eyebrow = "Mirrored to the television · Debug preview",
            showClock = true,
            requestInitialFocus = false,
            needsSettings = false,
            onPlay = {},
            onOpenPhoto = {},
            onRequestAccess = {},
            onOpenSettings = {},
            onRetry = {},
            onTitleLongPress = {},
        )
    }
}

private fun previewSnapshot(): LibrarySnapshot {
    return assembleLibrary(
        online = true,
        videosAllowed = false,
        photosAllowed = false,
        askedForAccess = false,
        scanning = false,
        localVideos = emptyList(),
        photos = emptyList(),
        records = listOf(
            PlaybackRecord(OpenMovies.BIG_BUCK_BUNNY, positionMs = 170_000L, durationMs = 596_000L, updatedAt = 2L),
            PlaybackRecord(OpenMovies.SINTEL, positionMs = 420_000L, durationMs = 888_000L, updatedAt = 1L),
        ),
    )
}
