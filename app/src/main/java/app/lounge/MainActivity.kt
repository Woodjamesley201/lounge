package app.lounge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lounge.display.DisplayConnectionRepository
import app.lounge.media.LibraryRepository
import app.lounge.playback.PlaybackStore

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val connection by displays.connection.collectAsStateWithLifecycle()
            LoungeApp(
                connection = connection,
                library = library,
                playback = playback,
                onTitleLongPress = { displays.toggleDebugTelevision() },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        displays.start()
        library.start()
    }

    override fun onStop() {
        playback.flush()
        library.stop()
        displays.stop()
        super.onStop()
    }
}
