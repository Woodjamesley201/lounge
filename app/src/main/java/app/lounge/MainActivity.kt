package app.lounge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.lounge.display.DisplayConnectionRepository

class MainActivity : ComponentActivity() {

    private val displays: DisplayConnectionRepository by lazy {
        DisplayConnectionRepository(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val connection by displays.connection.collectAsStateWithLifecycle()
            LoungeApp(
                connection = connection,
                onTitleLongPress = { displays.toggleDebugTelevision() },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        displays.start()
    }

    override fun onStop() {
        displays.stop()
        super.onStop()
    }
}
