package app.lounge.media

import app.lounge.playback.PlaybackRecord
import app.lounge.playback.WatchProgress

data class LibrarySnapshot(
    val online: Boolean,
    val videosAllowed: Boolean,
    val photosAllowed: Boolean,
    val askedForAccess: Boolean,
    val scanning: Boolean,
    val movies: List<MediaEntry>,
    val localVideos: List<MediaEntry>,
    val photos: List<MediaEntry>,
    val continueWatching: List<MediaEntry>,
    val hero: MediaEntry?,
) {
    val accessGranted: Boolean get() = videosAllowed || photosAllowed
}

/**
 * Joins the open-movie shelf, on-device media, and saved positions.
 * Pure: no Android types, so the resume rules can be unit tested.
 */
fun assembleLibrary(
    online: Boolean,
    videosAllowed: Boolean,
    photosAllowed: Boolean,
    askedForAccess: Boolean,
    scanning: Boolean,
    localVideos: List<MediaEntry>,
    photos: List<MediaEntry>,
    records: List<PlaybackRecord>,
): LibrarySnapshot {
    val recordById = records.associateBy { it.mediaId }
    val locals = if (videosAllowed) localVideos else emptyList()
    val stills = if (photosAllowed) photos else emptyList()

    fun decorate(entry: MediaEntry): MediaEntry {
        val record = recordById[entry.id] ?: return entry
        val duration = when {
            record.durationMs > 0L -> record.durationMs
            else -> entry.durationMs
        }
        if (!WatchProgress.isResumable(record.positionMs, duration)) {
            return entry.copy(durationMs = duration, positionMs = 0L, progress = null)
        }
        return entry.copy(
            durationMs = duration,
            positionMs = record.positionMs,
            progress = WatchProgress.fraction(record.positionMs, duration),
            subtitle = WatchProgress.remainingLabel(record.positionMs, duration),
        )
    }

    val movies = OpenMovies.all.map { decorate(it) }
    val decoratedLocals = locals.map { decorate(it) }
    val catalog = movies + decoratedLocals
    val continueWatching = records
        .sortedByDescending { it.updatedAt }
        .mapNotNull { record -> catalog.find { it.id == record.mediaId } }
        .filter { it.progress != null }
        .distinctBy { it.id }
        .take(12)

    val hero = continueWatching.firstOrNull { it.local || online }
        ?: continueWatching.firstOrNull()
        ?: decoratedLocals.firstOrNull()
        ?: movies.firstOrNull()?.takeIf { online }

    return LibrarySnapshot(
        online = online,
        videosAllowed = videosAllowed,
        photosAllowed = photosAllowed,
        askedForAccess = askedForAccess,
        scanning = scanning,
        movies = movies,
        localVideos = decoratedLocals,
        photos = stills,
        continueWatching = continueWatching,
        hero = hero,
    )
}
