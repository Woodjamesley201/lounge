package app.lounge.ui.glass

/**
 * Whether a pane may call [android.graphics.RenderEffect.createBlurEffect].
 *
 * That call runs during the first composition, while the splash is still up.
 * On Samsung HWUI the native blur object comes back without a pointer
 * (`nativePtr is null`) or the RenderThread aborts outright. Android 16
 * (API 36) changed the blur shader path, and a native abort there kills the
 * process with no "has stopped" dialog — which is what the Galaxy Tab A9
 * (SM-X110) did on 0.2.0. A try/catch cannot stop a native abort, so the
 * effect is never created on those devices.
 *
 * The pane still paints a milk veil, the specular streak, and the rim.
 * Hardware blur stays available on other Android 12–15 devices, and only
 * once the layer is large enough to blur.
 *
 * 0.2.1 turned that blur off and the Tab A9 still died at the lamp splash.
 * The first frame was still a stack of large radial shaders: the dusk room,
 * the same room again inside every glass card, and the title studies.
 * A Mali driver abort on that stack is a native death with no dialog, and
 * a try/catch cannot see it. [useConservativeCanvas] keeps Samsung devices
 * and Android 16 on linear dusk only. The rich radial room stays on other
 * devices. It is not turned back on after the first frame — the next frame
 * would hit the same shader.
 */
object GlassBlurPolicy {
    const val MIN_LAYER_PX = 2f

    fun deviceAllowsHardwareBlur(
        sdkInt: Int,
        manufacturer: String?,
        brand: String?,
    ): Boolean {
        if (sdkInt < 31 || sdkInt >= 36) return false
        if (isSamsung(manufacturer, brand)) return false
        return true
    }

    fun useConservativeCanvas(
        sdkInt: Int,
        manufacturer: String?,
        brand: String?,
    ): Boolean {
        if (sdkInt >= 36) return true
        return isSamsung(manufacturer, brand)
    }

    fun layerIsLargeEnough(widthPx: Float, heightPx: Float): Boolean {
        return widthPx >= MIN_LAYER_PX && heightPx >= MIN_LAYER_PX
    }

    fun isSamsung(manufacturer: String?, brand: String?): Boolean {
        fun has(value: String?) = value?.contains("samsung", ignoreCase = true) == true
        return has(manufacturer) || has(brand)
    }
}
