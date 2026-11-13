package app.lounge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lounge.display.DisplayConnection
import app.lounge.display.DisplayMode
import app.lounge.media.LibraryRepository
import app.lounge.media.MediaEntry
import app.lounge.media.MediaPermissions
import app.lounge.playback.PlaybackStore
import app.lounge.ui.glass.LiquidGlass
import app.lounge.ui.photos.PhotoViewer
import app.lounge.ui.player.PlayerScreen
import app.lounge.ui.tablet.TabletHomeScreen
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LoungeTheme
import app.lounge.ui.tv.TvHomeScreen
import kotlinx.coroutines.delay

private sealed interface Destination {
    data object Home : Destination
    data class Player(val entry: MediaEntry) : Destination
    data class Photos(val entries: List<MediaEntry>, val index: Int) : Destination
}

/**
 * Root composable. Switches layouts from [DisplayConnection.mode].
 * Theme and screens: UI Isaiah. The connection object: Scripting Mike.
 */
@Composable
fun LoungeApp(
    connection: DisplayConnection,
    library: LibraryRepository,
    playback: PlaybackStore,
    modifier: Modifier = Modifier,
    onTitleLongPress: () -> Unit = {},
) {
    val snapshot by library.snapshot.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var destination by remember { mutableStateOf<Destination>(Destination.Home) }
    var notice by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        library.onPermissionResult(result)
    }
    val needsSettings = remember(
        snapshot.askedForAccess,
        snapshot.videosAllowed,
        snapshot.photosAllowed,
    ) {
        val activity = context as? Activity ?: return@remember false
        if (!snapshot.askedForAccess || snapshot.accessGranted) return@remember false
        !activity.shouldShowRequestPermissionRationale(MediaPermissions.primary())
    }

    fun play(entry: MediaEntry) {
        if (!entry.local && !snapshot.online) {
            notice = "No connection. ${entry.title} needs the network."
            return
        }
        destination = Destination.Player(entry)
    }

    fun openPhoto(entry: MediaEntry) {
        val photos = snapshot.photos
        val index = photos.indexOfFirst { it.id == entry.id }.coerceAtLeast(0)
        if (photos.isEmpty()) {
            notice = "That photo is not on this tablet anymore."
            return
        }
        destination = Destination.Photos(photos, index)
    }

    fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        )
        context.startActivity(intent)
    }

    LaunchedEffect(notice) {
        if (notice != null) {
            delay(4_200)
            notice = null
        }
    }

    LoungeTheme(isTelevision = connection.isTelevision) {
        Box(modifier.fillMaxSize()) {
            when (val dest = destination) {
                Destination.Home -> when (connection.mode) {
                    DisplayMode.TABLET -> TabletHomeScreen(
                        connection = connection,
                        snapshot = snapshot,
                        needsSettings = needsSettings,
                        onPlay = ::play,
                        onOpenPhoto = ::openPhoto,
                        onRequestAccess = { permissionLauncher.launch(MediaPermissions.requestSet()) },
                        onOpenSettings = ::openSettings,
                        onRetry = { library.refresh() },
                        onTitleLongPress = onTitleLongPress,
                    )
                    DisplayMode.TELEVISION -> TvHomeScreen(
                        connection = connection,
                        snapshot = snapshot,
                        needsSettings = needsSettings,
                        onPlay = ::play,
                        onOpenPhoto = ::openPhoto,
                        onRequestAccess = { permissionLauncher.launch(MediaPermissions.requestSet()) },
                        onOpenSettings = ::openSettings,
                        onRetry = { library.refresh() },
                        onTitleLongPress = onTitleLongPress,
                    )
                }
                is Destination.Player -> PlayerScreen(
                    entry = dest.entry,
                    playback = playback,
                    onClose = {
                        destination = Destination.Home
                        library.refreshProgress()
                    },
                )
                is Destination.Photos -> PhotoViewer(
                    photos = dest.entries,
                    initialIndex = dest.index,
                    onClose = { destination = Destination.Home },
                )
            }
            val message = notice
            if (message != null && destination == Destination.Home) {
                LiquidGlass(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    corner = 22.dp,
                    refract = true,
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        color = Ember.Cream,
                    )
                }
            }
        }
    }
}
