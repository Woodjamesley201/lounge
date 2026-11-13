package app.lounge.ui.tablet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.lounge.display.DisplayConnection
import app.lounge.media.LibrarySnapshot
import app.lounge.media.MediaEntry
import app.lounge.ui.home.LoungeHome

/**
 * Touch-first home for the tablet's own screen. Owner: UI Isaiah.
 * The connection is accepted so the activity can keep passing it through;
 * the eyebrow stays "On this tablet" until a bigger display is attached.
 */
@Composable
fun TabletHomeScreen(
    @Suppress("UNUSED_PARAMETER") connection: DisplayConnection,
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
    LoungeHome(
        snapshot = snapshot,
        eyebrow = "On this tablet",
        showClock = false,
        requestInitialFocus = false,
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
