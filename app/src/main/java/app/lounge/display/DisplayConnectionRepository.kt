package app.lounge.display

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Combines wired and wireless monitors into a single [DisplayConnection] flow
 * for the UI to collect.
 *
 * Owner: Display Manager Jayden (merge policy and debug override).
 * Wired source: [WiredDisplayMonitor] (Jayden).
 * Wireless source: [WirelessDisplayMonitor] (Scripting Mike).
 *
 * Merge policy: a debug override wins, then a wired/Presentation display,
 * then a wireless route, otherwise [DisplayConnection.TabletOnly].
 */
class DisplayConnectionRepository(context: Context) {

    private val _connection = MutableStateFlow(DisplayConnection.TabletOnly)
    val connection: StateFlow<DisplayConnection> = _connection.asStateFlow()

    private var started = false
    private var debugOverride: DisplayConnection? = null

    private val wired = WiredDisplayMonitor(context, onChange = { refresh() })
    private val wireless = WirelessDisplayMonitor(context, onChange = { refresh() })

    fun start() {
        if (started) return
        started = true
        wired.start()
        wireless.start()
        refresh()
    }

    fun stop() {
        if (!started) return
        started = false
        wired.stop()
        wireless.stop()
    }

    /** When set, [refresh] reports this connection instead of the real hardware. Null clears it. */
    fun setDebugOverride(connection: DisplayConnection?) {
        debugOverride = connection
        refresh()
    }

    fun toggleDebugTelevision() {
        setDebugOverride(
            if (debugOverride?.mode == DisplayMode.TELEVISION) null
            else DisplayConnection(
                mode = DisplayMode.TELEVISION,
                source = DisplaySource.WIRELESS_MIRROR,
                displayName = "Debug preview",
            ),
        )
    }

    private fun refresh() {
        _connection.value = debugOverride
            ?: wired.currentConnection()
            ?: wireless.currentConnection()
            ?: DisplayConnection.TabletOnly
    }
}
