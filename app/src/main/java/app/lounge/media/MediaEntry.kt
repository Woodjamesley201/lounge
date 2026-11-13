package app.lounge.media

/**
 * One playable or viewable thing on the shelf.
 * Streams and on-device files share this shape so the home screen
 * does not care where the bytes live.
 */
enum class MediaKind {
    FILM,
    LOCAL_VIDEO,
    PHOTO,
}

/** Original painted artwork for a tile. Not a poster and not a logo. */
enum class ArtMotif {
    MEADOW,
    FROST,
    FORGE,
    DREAM,
    REEL,
    STILL,
}

data class MediaEntry(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val kind: MediaKind,
    val uri: String,
    val local: Boolean,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val progress: Float? = null,
    val art: ArtMotif = ArtMotif.REEL,
    val credit: String? = null,
)
