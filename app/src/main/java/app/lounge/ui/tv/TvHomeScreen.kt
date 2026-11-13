package app.lounge.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.lounge.display.DisplayConnection
import app.lounge.display.DisplaySource
import app.lounge.media.LibrarySnapshot
import app.lounge.media.MediaEntry
import app.lounge.ui.home.LoungeHome

/**
 * 10-foot home. Owner: UI Isaiah.
 * Large type, a thick focus rim, and D-pad focus on the feature pane.
 */
@Composable
fun TvHomeScreen(
    connection: DisplayConnection,
    snapshot: LibrarySnapshot,
    needsSettings: Boolean,
    onPlay: (MediaEntry) -> Unit,
    onOpenPhoto: (MediaEntry) -> Unit,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onTitleLongPress: () -> Unit = {},
) {
    val sourceLabel = when (connection.source) {
        DisplaySource.HDMI -> "On the television"
        DisplaySource.WIRELESS_MIRROR -> "Mirrored to the television"
        DisplaySource.CAST -> "Casting"
        DisplaySource.PRESENTATION -> "On the external display"
        DisplaySource.NONE -> "Television preview"
    }
    val eyebrow = connection.displayName?.let { "$sourceLabel · $it" } ?: sourceLabel

    LoungeHome(
        snapshot = snapshot,
        eyebrow = eyebrow,
        showClock = true,
        requestInitialFocus = true,
        needsSettings = needsSettings,
        onPlay = onPlay,
        onOpenPhoto = onOpenPhoto,
        onRequestAccess = onRequestAccess,
        onOpenSettings = onOpenSettings,
        onRetry = onRetry,
        onTitleLongPress = onTitleLongPress,
        modifier = modifier,
    )
}
