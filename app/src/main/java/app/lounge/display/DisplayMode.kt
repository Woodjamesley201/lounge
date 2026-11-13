package app.lounge.display

/**
 * How Lounge is currently presenting itself.
 *
 * Owned by Scripting Mike. UI code should collect [DisplayConnectionRepository.connection]
 * and must not detect displays on its own.
 */
enum class DisplayMode {
    /** Touch-first UI on the tablet's own screen. */
    TABLET,

    /** 10-foot UI for a couch / big-screen experience. */
    TELEVISION,
}

/** How a bigger display got attached, if one is attached at all. */
enum class DisplaySource {
    NONE,
    /** USB-C / HDMI / DisplayPort Alt Mode. The Tab A9 (SM-X110) cannot do this. */
    HDMI,
    /** Wireless mirror such as Samsung Smart View / Miracast. Usually mirrors the same UI. */
    WIRELESS_MIRROR,
    /** Google Cast (or similar) where the TV can show a distinct receiver UI. */
    CAST,
    /** A true Android Presentation / secondary display. */
    PRESENTATION,
}

data class DisplayConnection(
    val mode: DisplayMode,
    val source: DisplaySource = DisplaySource.NONE,
    val displayName: String? = null,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
) {
    val isTelevision: Boolean get() = mode == DisplayMode.TELEVISION

    companion object {
        val TabletOnly = DisplayConnection(DisplayMode.TABLET)
    }
}
