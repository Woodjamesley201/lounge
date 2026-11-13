package app.lounge.ui.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.lounge.display.DisplayConnection
import app.lounge.ui.catalog.SampleCatalog
import app.lounge.ui.components.ContentRow
import app.lounge.ui.components.HeroCard
import app.lounge.ui.components.LoungeHeader
import app.lounge.ui.theme.Ember
import app.lounge.ui.theme.LocalLoungeMetrics

/**
 * Touch-first home for the tablet's own 8.7" screen. Owner: UI Isaiah.
 *
 * Comfortable targets, a featured resume card, and the same warm language
 * as the TV layout — just tighter, for a hand holding the Tab A9.
 */
@Composable
fun TabletHomeScreen(
    connection: DisplayConnection,
    modifier: Modifier = Modifier,
    onTitleLongPress: () -> Unit = {},
) {
    val metrics = LocalLoungeMetrics.current
    val featured = SampleCatalog.continueWatching.first()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ember.Night)
            .drawBehind { drawRoomGlow() }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = metrics.screenPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.rowGap),
        ) {
            LoungeHeader(
                eyebrow = "On this tablet",
                onTitleLongPress = onTitleLongPress,
            )
            Spacer(Modifier.height(2.dp))
            HeroCard(
                tile = featured,
                modifier = Modifier.padding(horizontal = metrics.screenPadding),
            )
            ContentRow("Continue watching", SampleCatalog.continueWatching)
            ContentRow("Movies", SampleCatalog.movies)
            ContentRow("Shows", SampleCatalog.shows)
            ContentRow("Photos", SampleCatalog.photos, square = true)
        }
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoomGlow() {
    drawRect(
        Brush.radialGradient(
            colors = listOf(
                Ember.Amber.copy(alpha = 0.10f),
                Ember.AmberDeep.copy(alpha = 0.04f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.12f, size.height * 0.0f),
            radius = size.minDimension * 0.95f,
        ),
    )
    drawRect(
        Brush.verticalGradient(
            0f to Color.Transparent,
            1f to Ember.NightDeep.copy(alpha = 0.5f),
        ),
    )
}
