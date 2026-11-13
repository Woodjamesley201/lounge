package app.lounge.ui.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import app.lounge.display.DisplayConnection
import app.lounge.display.DisplaySource
import app.lounge.ui.catalog.SampleCatalog
import app.lounge.ui.components.ContentRow
import app.lounge.ui.components.LoungeHeader
import app.lounge.ui.tablet.drawRoomGlow
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics
import kotlinx.coroutines.yield

/**
 * 10-foot home for a TV or a mirrored big screen. Owner: UI Isaiah.
 *
 * Designed for the couch and a D-pad / Bluetooth remote: large type, sparse
 * rows, a thick ember focus ring, and no hover-only states. The first
 * "Continue watching" tile takes focus when this layout appears.
 */
@Composable
fun TvHomeScreen(
    connection: DisplayConnection,
    modifier: Modifier = Modifier,
    onTitleLongPress: () -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        yield()
        runCatching { firstFocus.requestFocus() }
    }

    val sourceLabel = when (connection.source) {
        DisplaySource.HDMI -> "On the television"
        DisplaySource.WIRELESS_MIRROR -> "Mirrored to the television"
        DisplaySource.CAST -> "Casting"
        DisplaySource.PRESENTATION -> "On the external display"
        DisplaySource.NONE -> "Television preview"
    }
    val eyebrow = connection.displayName?.let { "$sourceLabel · $it" } ?: sourceLabel

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ember.Night)
            .drawBehind { drawRoomGlow() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = metrics.screenPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.rowGap),
        ) {
            LoungeHeader(
                eyebrow = eyebrow,
                onTitleLongPress = onTitleLongPress,
                showClock = true,
            )
            Spacer(Modifier.height(8.dp))
            ContentRow(
                title = "Continue watching",
                tiles = SampleCatalog.continueWatching,
                initialFocusIndex = 0,
                initialFocusRequester = firstFocus,
            )
            ContentRow("Movies", SampleCatalog.movies)
            ContentRow("Shows", SampleCatalog.shows)
            ContentRow("Photos", SampleCatalog.photos, square = true)
        }
    }
}
