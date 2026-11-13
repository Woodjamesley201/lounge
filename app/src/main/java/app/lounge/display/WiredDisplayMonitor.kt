package app.lounge.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Display

/**
 * Watches Android [DisplayManager] for a wired or Presentation secondary display
 * (HDMI, DisplayPort Alt Mode, a true external screen).
 *
 * Owner: Display Manager Jayden.
 *
 * On the first device (Galaxy Tab A9 SM-X110) a USB-C-to-HDMI cable will not
 * produce a display — the port is USB 2.0 with no DP Alt Mode. Still implement
 * this fully so the app works on tablets that can do HDMI.
 *
 * Return a [DisplayConnection] when a non-default, usable display is present,
 * or null when the tablet is on its own screen. Do not classify wireless
 * routes here; that is [WirelessDisplayMonitor].
 */
class WiredDisplayMonitor(
    context: Context,
    private val onChange: () -> Unit,
) {
    private val displayManager =
        context.applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    private var started = false

    private val listener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = onChange()
        override fun onDisplayRemoved(displayId: Int) = onChange()
        override fun onDisplayChanged(displayId: Int) = onChange()
    }

    fun start() {
        if (started) return
        started = true
        displayManager.registerDisplayListener(
            listener,
            Handler(Looper.getMainLooper()),
        )
    }

    fun stop() {
        if (!started) return
        started = false
        displayManager.unregisterDisplayListener(listener)
    }

    /**
     * A usable non-default wired or Presentation display, or null if none.
     * Prefers [DisplaySource.HDMI] when both kinds are present. Wireless and
     * virtual displays are left to [WirelessDisplayMonitor].
     */
    fun currentConnection(): DisplayConnection? {
        val display = selectWiredDisplay() ?: return null
        val size = display.physicalSize()
        return DisplayConnection(
            mode = DisplayMode.TELEVISION,
            source = classify(display),
            displayName = display.name,
            widthPx = size?.first,
            heightPx = size?.second,
        )
    }

    private fun selectWiredDisplay(): Display? {
        val candidates = displayManager.displays.filter { isUsableWiredDisplay(it) }
        return candidates.firstOrNull { classify(it) == DisplaySource.HDMI }
            ?: candidates.firstOrNull()
    }

    private fun isUsableWiredDisplay(display: Display): Boolean {
        if (display.displayId == Display.DEFAULT_DISPLAY) return false
        if (!display.isValid) return false
        if (display.state == Display.STATE_OFF) return false
        if (display.flags and Display.FLAG_PRIVATE != 0) return false

        return when (display.systemType()) {
            // Built-in screen (even if it is not DEFAULT_DISPLAY) and wireless /
            // virtual routes belong elsewhere.
            TYPE_INTERNAL, TYPE_WIFI, TYPE_VIRTUAL -> false
            TYPE_EXTERNAL -> true
            // Developer Options "Simulate secondary displays" uses TYPE_OVERLAY.
            TYPE_OVERLAY -> display.isPresentation
            // Hidden API unavailable, or TYPE_UNKNOWN: accept presentation
            // displays, but skip names that look like wireless routes.
            else -> display.isPresentation && !looksWireless(display.name)
        }
    }

    private fun classify(display: Display): DisplaySource {
        if (display.systemType() == TYPE_EXTERNAL) return DisplaySource.HDMI
        if (looksLikeHdmi(display.name)) return DisplaySource.HDMI
        return DisplaySource.PRESENTATION
    }

    private val Display.isPresentation: Boolean
        get() = flags and Display.FLAG_PRESENTATION != 0

    private fun Display.physicalSize(): Pair<Int, Int>? {
        val current = try {
            mode
        } catch (_: Throwable) {
            null
        } ?: return null
        return current.physicalWidth to current.physicalHeight
    }

    companion object {
        // android.view.Display type constants. They are @hide in the SDK but
        // the values have been stable, and getType() is public on the device.
        private const val TYPE_INTERNAL = 1
        private const val TYPE_EXTERNAL = 2
        private const val TYPE_WIFI = 3
        private const val TYPE_OVERLAY = 4
        private const val TYPE_VIRTUAL = 5

        private val getTypeMethod: java.lang.reflect.Method? by lazy {
            try {
                Display::class.java.getMethod("getType")
            } catch (_: Throwable) {
                null
            }
        }

        private fun Display.systemType(): Int? = try {
            getTypeMethod?.invoke(this) as? Int
        } catch (_: Throwable) {
            null
        }

        private fun looksLikeHdmi(name: String?): Boolean {
            val n = name?.lowercase() ?: return false
            return "hdmi" in n || "displayport" in n || "display port" in n
        }

        private fun looksWireless(name: String?): Boolean {
            val n = name?.lowercase() ?: return false
            return listOf(
                "wifi",
                "wi-fi",
                "wireless",
                "miracast",
                "smart view",
                "smartview",
                "chromecast",
            ).any { it in n }
        }
    }
}
