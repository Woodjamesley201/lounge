package app.lounge.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lounge visual language. Owner: UI Isaiah.
 *
 * Warm, dark, and calm — a living room at dusk, not a consumer electronics
 * storefront. Ember and cream on a walnut-black field. This is original:
 * it does not follow Apple tvOS, Google TV, or any other commercial chrome.
 */
object Ember {
    val Night = Color(0xFF0E0C0A)
    val NightDeep = Color(0xFF090807)
    val Raised = Color(0xFF1A1714)
    val Recess = Color(0xFF241F1A)
    val Line = Color(0xFF3A342C)
    val Amber = Color(0xFFE8A05C)
    val AmberBright = Color(0xFFF0C48A)
    val AmberDeep = Color(0xFFC4783A)
    val Cream = Color(0xFFF3EDE4)
    val CreamMuted = Color(0xFFB5ADA3)
    val CreamDim = Color(0xFF8A837A)
    val Scrim = Color(0xCC0E0C0A)
}

data class LoungeMetrics(
    val isTelevision: Boolean,
    val screenPadding: Dp,
    val rowGap: Dp,
    val tileWidth: Dp,
    val tileHeight: Dp,
    val photoSize: Dp,
    val tileCorner: Dp,
    val tileGap: Dp,
    val focusPad: Dp,
    val focusStroke: Dp,
    val focusScale: Float,
    val titleSize: TextUnit,
    val rowTitleSize: TextUnit,
    val tileTitleSize: TextUnit,
    val eyebrowSize: TextUnit,
    val heroHeight: Dp,
)

val TabletMetrics = LoungeMetrics(
    isTelevision = false,
    screenPadding = 28.dp,
    rowGap = 26.dp,
    tileWidth = 196.dp,
    tileHeight = 110.dp,
    photoSize = 132.dp,
    tileCorner = 14.dp,
    tileGap = 14.dp,
    focusPad = 8.dp,
    focusStroke = 2.dp,
    focusScale = 1.04f,
    titleSize = 34.sp,
    rowTitleSize = 17.sp,
    tileTitleSize = 14.sp,
    eyebrowSize = 13.sp,
    heroHeight = 196.dp,
)

val TelevisionMetrics = LoungeMetrics(
    isTelevision = true,
    screenPadding = 72.dp,
    rowGap = 40.dp,
    tileWidth = 304.dp,
    tileHeight = 171.dp,
    photoSize = 188.dp,
    tileCorner = 16.dp,
    tileGap = 22.dp,
    focusPad = 22.dp,
    focusStroke = 4.dp,
    focusScale = 1.08f,
    titleSize = 50.sp,
    rowTitleSize = 22.sp,
    tileTitleSize = 18.sp,
    eyebrowSize = 16.sp,
    heroHeight = 0.dp, // TV is sparse rows, no hero band
)

val LocalLoungeMetrics = staticCompositionLocalOf { TabletMetrics }

fun <T> loungeTween(duration: Int = 280): TweenSpec<T> =
    tween(durationMillis = duration, easing = FastOutSlowInEasing)

private val Colors = darkColorScheme(
    primary = Ember.Amber,
    onPrimary = Ember.Night,
    secondary = Ember.AmberDeep,
    onSecondary = Ember.Cream,
    background = Ember.Night,
    onBackground = Ember.Cream,
    surface = Ember.Raised,
    onSurface = Ember.Cream,
    surfaceVariant = Ember.Recess,
    onSurfaceVariant = Ember.CreamMuted,
    outline = Ember.Line,
    outlineVariant = Ember.Line,
    scrim = Ember.Scrim,
    tertiary = Ember.AmberBright,
    onTertiary = Ember.Night,
)

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private val LoungeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 50.sp,
        letterSpacing = (-0.6).sp,
        color = Ember.Cream,
        lineHeight = 56.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        letterSpacing = (-0.4).sp,
        color = Ember.Cream,
    ),
    headlineMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        letterSpacing = (-0.3).sp,
        color = Ember.Cream,
    ),
    titleMedium = TextStyle(
        fontFamily = Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = 0.2.sp,
        color = Ember.Cream,
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = Ember.CreamMuted,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Ember.Cream,
        lineHeight = 18.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = Ember.CreamDim,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.8.sp,
        color = Ember.CreamMuted,
    ),
)

@Composable
fun LoungeTheme(
    isTelevision: Boolean,
    content: @Composable () -> Unit,
) {
    val metrics = if (isTelevision) TelevisionMetrics else TabletMetrics
    CompositionLocalProvider(LocalLoungeMetrics provides metrics) {
        MaterialTheme(
            colorScheme = Colors,
            typography = LoungeTypography,
            content = content,
        )
    }
}
