package app.lounge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.lounge.display.DisplayConnection
import app.lounge.display.DisplayMode
import app.lounge.ui.tablet.TabletHomeScreen
import app.lounge.ui.theme.LoungeTheme
import app.lounge.ui.tv.TvHomeScreen

/**
 * Root composable. Switches layouts from [DisplayConnection.mode].
 * Theme and screens: UI Isaiah. The connection object: Scripting Mike.
 */
@Composable
fun LoungeApp(
    connection: DisplayConnection,
    modifier: Modifier = Modifier,
    onTitleLongPress: () -> Unit = {},
) {
    LoungeTheme(isTelevision = connection.isTelevision) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (connection.mode) {
                DisplayMode.TABLET -> TabletHomeScreen(connection, onTitleLongPress = onTitleLongPress)
                DisplayMode.TELEVISION -> TvHomeScreen(connection, onTitleLongPress = onTitleLongPress)
            }
        }
    }
}
