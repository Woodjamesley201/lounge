package app.lounge.ui.catalog

import app.lounge.ui.components.LoungeTile
import app.lounge.ui.components.TileKind

/**
 * Placeholder library so the home screens feel finished.
 * Titles are invented; none are licensed artwork or real posters.
 */
object SampleCatalog {
    val continueWatching = listOf(
        LoungeTile("cw1", "North Shore", "23 min left", progress = 0.71f, kind = TileKind.FILM),
        LoungeTile("cw2", "Kitchen Night", "Episode 4", progress = 0.44f, kind = TileKind.SHOW),
        LoungeTile("cw3", "City Lights", "12 min left", progress = 0.86f, kind = TileKind.FILM),
        LoungeTile("cw4", "River House", "Episode 2", progress = 0.28f, kind = TileKind.SHOW),
    )
    val movies = listOf(
        LoungeTile("m1", "Harbor", kind = TileKind.FILM),
        LoungeTile("m2", "Afterglow", kind = TileKind.FILM),
        LoungeTile("m3", "The Ridge", kind = TileKind.FILM),
        LoungeTile("m4", "Sunday Train", kind = TileKind.FILM),
        LoungeTile("m5", "Low Tide", kind = TileKind.FILM),
        LoungeTile("m6", "Ember Hour", kind = TileKind.FILM),
    )
    val shows = listOf(
        LoungeTile("s1", "Workshop", "Season 2", kind = TileKind.SHOW),
        LoungeTile("s2", "River House", "Season 1", kind = TileKind.SHOW),
        LoungeTile("s3", "Night Shift", "Season 3", kind = TileKind.SHOW),
        LoungeTile("s4", "Long Table", "Season 1", kind = TileKind.SHOW),
        LoungeTile("s5", "Open Window", "Season 2", kind = TileKind.SHOW),
    )
    val photos = listOf(
        LoungeTile("p1", "Summer", kind = TileKind.PHOTO),
        LoungeTile("p2", "Garden", kind = TileKind.PHOTO),
        LoungeTile("p3", "Trip", kind = TileKind.PHOTO),
        LoungeTile("p4", "Porch", kind = TileKind.PHOTO),
        LoungeTile("p5", "Evening", kind = TileKind.PHOTO),
        LoungeTile("p6", "Kitchen", kind = TileKind.PHOTO),
    )
}
