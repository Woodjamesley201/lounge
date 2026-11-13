package app.lounge.playback

/** Where someone left a film. Survives process death via [PlaybackStore]. */
data class PlaybackRecord(
    val mediaId: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)
