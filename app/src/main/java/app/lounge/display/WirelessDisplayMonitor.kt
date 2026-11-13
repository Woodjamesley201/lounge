package app.lounge.display

import android.content.Context
import android.content.IntentFilter
import android.view.Display
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo

/**
 * Watches wireless display routes: Samsung Smart View / Miracast and Google Cast.
 *
 * Owner: Scripting Mike.
 *
 * On the Galaxy Tab A9 (SM-X110) this is the path that actually reaches a TV.
 * Smart View usually mirrors the tablet (use [DisplaySource.WIRELESS_MIRROR] and
 * let the on-device UI switch to TV mode). Cast can show a distinct receiver
 * ([DisplaySource.CAST]) and turn the tablet into a remote.
 *
 * Return a [DisplayConnection] when a live-video or Cast route is selected,
 * or null when nothing wireless is connected. Do not inspect DisplayManager
 * here; that is [WiredDisplayMonitor].
 */
class WirelessDisplayMonitor(
    context: Context,
    private val onChange: () -> Unit,
) {
    private val appContext = context.applicationContext
    private val router: MediaRouter = MediaRouter.getInstance(appContext)

    private var started = false

    private val selector: MediaRouteSelector = MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

    private val callback = object : MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter,
            route: RouteInfo,
            reason: Int,
        ) = onChange()

        override fun onRouteUnselected(
            router: MediaRouter,
            route: RouteInfo,
            reason: Int,
        ) = onChange()

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) = onChange()

        override fun onRouteAdded(router: MediaRouter, route: RouteInfo) = onChange()

        override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) = onChange()

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter,
            route: RouteInfo,
        ) = onChange()
    }

    fun start() {
        if (started) return
        started = true
        router.addCallback(
            selector,
            callback,
            MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY,
        )
    }

    fun stop() {
        if (!started) return
        started = false
        router.removeCallback(callback)
    }

    /**
     * The selected MediaRouter route, if it is a live-video (Smart View / Miracast)
     * or Cast session pointed at a display. The default, fallback, and audio-only
     * routes (built-in speaker, Bluetooth, Cast speakers) return null.
     */
    fun currentConnection(): DisplayConnection? = connectionFor(router.selectedRoute)

    private fun connectionFor(route: RouteInfo): DisplayConnection? {
        if (!isActiveDisplayRoute(route)) return null
        val presentation = route.presentationDisplay
        return DisplayConnection(
            mode = DisplayMode.TELEVISION,
            source = sourceFor(route),
            displayName = route.name.toString().ifBlank { null },
            widthPx = presentation?.pixelWidth(),
            heightPx = presentation?.pixelHeight(),
        )
    }

    /**
     * A usable wireless *display*: selected, enabled, not the default/fallback
     * route, not an audio-only sink, and carrying live-video or a Cast session.
     */
    private fun isActiveDisplayRoute(route: RouteInfo): Boolean {
        if (!route.isEnabled) return false
        if (route.isDefault || route.isDeviceSpeaker || route.isBluetooth) return false
        if (isAudioOnlySink(route)) return false

        val liveVideo = route.supportsControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        val remotePlayback =
            route.supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        val cast = isCastRoute(route)
        if (!liveVideo && !cast && !remotePlayback) return false
        // The default/fallback system route can match a broad selector; already
        // rejected above. Still require the selector (or a Cast category, which
        // is not always listed in the androidx selector) so random local routes
        // do not count as a TV.
        return route.matchesSelector(selector) || cast || liveVideo
    }

    /**
     * Smart View / Miracast is a live-video mirror. A Cast session (GMS Cast
     * category or provider) is [DisplaySource.CAST], even if the receiver also
     * advertises live video. Other remote-playback routes on a display follow Cast.
     */
    private fun sourceFor(route: RouteInfo): DisplaySource {
        if (isCastRoute(route)) return DisplaySource.CAST
        if (route.supportsControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)) {
            return DisplaySource.WIRELESS_MIRROR
        }
        return DisplaySource.CAST
    }

    companion object {
        /** Matches Cast CATEGORY_CAST and categoryForCast(appId). */
        private const val CAST_CATEGORY_PREFIX = "com.google.android.gms.cast.CATEGORY_CAST"

        private const val GMS_PACKAGE = "com.google.android.gms"

        internal fun isCastRoute(route: RouteInfo): Boolean {
            val provider = route.provider.packageName ?: ""
            if (provider == GMS_PACKAGE || provider.startsWith("$GMS_PACKAGE.")) return true
            return route.controlFilters.any { it.hasCastCategory() }
        }

        /**
         * Speakers, headsets, and similar sinks should not flip Lounge into TV
         * mode. A live-video flag on the same route still counts as a display
         * (some AVRs advertise both).
         */
        internal fun isAudioOnlySink(route: RouteInfo): Boolean {
            if (route.supportsControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)) {
                return false
            }
            return when (route.deviceType) {
                RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER, // same value as deprecated DEVICE_TYPE_SPEAKER
                RouteInfo.DEVICE_TYPE_BLUETOOTH_A2DP,
                RouteInfo.DEVICE_TYPE_BUILTIN_SPEAKER,
                RouteInfo.DEVICE_TYPE_WIRED_HEADSET,
                RouteInfo.DEVICE_TYPE_WIRED_HEADPHONES,
                RouteInfo.DEVICE_TYPE_USB_HEADSET,
                RouteInfo.DEVICE_TYPE_HEARING_AID,
                RouteInfo.DEVICE_TYPE_BLE_HEADSET,
                RouteInfo.DEVICE_TYPE_HDMI_ARC,
                RouteInfo.DEVICE_TYPE_HDMI_EARC,
                RouteInfo.DEVICE_TYPE_SMARTWATCH,
                // Speaker groups (e.g. a Cast speaker pair) are audio-only.
                // A live-video group is already excluded above.
                RouteInfo.DEVICE_TYPE_GROUP,
                -> true
                else -> false
            }
        }

        private fun IntentFilter.hasCastCategory(): Boolean {
            for (i in 0 until countCategories()) {
                val category = getCategory(i) ?: continue
                if (category == CAST_CATEGORY_PREFIX ||
                    category.startsWith("$CAST_CATEGORY_PREFIX/")
                ) {
                    return true
                }
            }
            return false
        }

        private fun Display.pixelWidth(): Int = mode.physicalWidth

        private fun Display.pixelHeight(): Int = mode.physicalHeight
    }
}
