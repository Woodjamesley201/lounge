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
 * Dusk, glass, and a warm lamp. Panels are translucent and refractive.
 * The wordmark stays a serif "Lounge" with the floor lamp — this is not
 * a tvOS or iOS home screen, and it does not use a system font.
 */
object Ember {
    val Night = Color(0xFF070A14)
    val NightDeep = Color(0xFF05070F)
    val Raised = Color(0xFF161E30)
    val Recess = Color(0xFF1C2638)
    val Line = Color(0xFF3A4760)
    val Amber = Color(0xFFE6B15E)
    val AmberBright = Color(0xFFF6D7A2)
    val AmberDeep = Color(0xFFC4843A)
    val Cream = Color(0xFFF4F7FB)
    val CreamMuted = Color(0xFFC5D0E0)
    val CreamDim = Color(0xFF8E9BB0)
    val Scrim = Color(0xCC070A14)
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
    tileCorner = 22.dp,
    tileGap = 14.dp,
    focusPad = 12.dp,
    focusStroke = 2.dp,
    focusScale = 1.045f,
    titleSize = 34.sp,
    rowTitleSize = 17.sp,
    tileTitleSize = 14.sp,
    eyebrowSize = 13.sp,
    heroHeight = 220.dp,
)

val TelevisionMetrics = LoungeMetrics(
    isTelevision = true,
    screenPadding = 72.dp,
    rowGap = 40.dp,
    tileWidth = 304.dp,
    tileHeight = 171.dp,
    photoSize = 188.dp,
    tileCorner = 26.dp,
    tileGap = 22.dp,
    focusPad = 22.dp,
    focusStroke = 4.dp,
    focusScale = 1.06f,
    titleSize = 50.sp,
    rowTitleSize = 22.sp,
    tileTitleSize = 18.sp,
    eyebrowSize = 16.sp,
    heroHeight = 300.dp,
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
