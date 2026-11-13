package app.lounge.ui.glass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassBlurPolicyTest {

    @Test
    fun hardwareBlurStaysOffUntilAndroid12() {
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(26, "Google", "google"))
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(30, "Google", "google"))
    }

    @Test
    fun pixelOnAndroid13MayBlur() {
        assertTrue(GlassBlurPolicy.deviceAllowsHardwareBlur(33, "Google", "google"))
    }

    @Test
    fun samsungNeverUsesPlatformBlur() {
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(33, "samsung", "samsung"))
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(34, "Samsung", "samsung"))
        assertFalse(
            GlassBlurPolicy.deviceAllowsHardwareBlur(36, "samsung", "samsung"),
        )
    }

    @Test
    fun android16NeverUsesPlatformBlur() {
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(36, "Google", "google"))
        assertFalse(GlassBlurPolicy.deviceAllowsHardwareBlur(36, "samsung", "samsung"))
    }

    @Test
    fun samsungAndAndroid16StayOnTheLinearCanvas() {
        assertTrue(GlassBlurPolicy.useConservativeCanvas(36, "samsung", "samsung"))
        assertTrue(GlassBlurPolicy.useConservativeCanvas(36, "Google", "google"))
        assertTrue(GlassBlurPolicy.useConservativeCanvas(34, "samsung", "samsung"))
        assertTrue(GlassBlurPolicy.useConservativeCanvas(26, "Samsung Electronics", "samsung"))
    }

    @Test
    fun otherDevicesKeepTheRadialRoom() {
        assertFalse(GlassBlurPolicy.useConservativeCanvas(34, "Google", "google"))
        assertFalse(GlassBlurPolicy.useConservativeCanvas(33, "Google", "google"))
        assertFalse(GlassBlurPolicy.useConservativeCanvas(26, "Google", "google"))
    }

    @Test
    fun zeroSizeLayerIsNotBlurred() {
        assertFalse(GlassBlurPolicy.layerIsLargeEnough(0f, 200f))
        assertFalse(GlassBlurPolicy.layerIsLargeEnough(200f, 1f))
        assertTrue(GlassBlurPolicy.layerIsLargeEnough(48f, 48f))
    }
}