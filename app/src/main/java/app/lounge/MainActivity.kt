package app.lounge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lounge.display.DisplayConnectionRepository
import app.lounge.media.LibraryRepository
import app.lounge.playback.PlaybackStore
import app.lounge.ui.theme.Ember

class MainActivity : ComponentActivity() {

    private val displays: DisplayConnectionRepository by lazy {
        DisplayConnectionRepository(applicationContext)
    }
    private val playback: PlaybackStore by lazy {
        PlaybackStore(applicationContext)
    }
    private val library: LibraryRepository by lazy {
        LibraryRepository(applicationContext, playback)
    }

    /** Display and library startup wait until a plain frame has been drawn. */
    private var servicesArmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var pastFirstFrame by remember { mutableStateOf(false) }
            if (!pastFirstFrame) {
                Box(Modifier.fillMaxSize().background(Ember.Night))
                LaunchedEffect(Unit) {
                    withFrameNanos { }
                    pastFirstFrame = true
                }
            } else {
                val connection by displays.connection.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { armServices() }
                LoungeApp(
                    connection = connection,
                    library = library,
                    playback = playback,
                    onTitleLongPress = { displays.toggleDebugTelevision() },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (servicesArmed) {
            displays.start()
            library.start()
        }
    }

    override fun onStop() {
        if (servicesArmed) {
            playback.flush()
            library.stop()
            displays.stop()
        }
        super.onStop()
    }

    private fun armServices() {
        servicesArmed = true
        displays.start()
        library.start()
    }
}
