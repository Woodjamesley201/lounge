package app.lounge.playback

import app.lounge.media.ArtMotif
import app.lounge.media.MediaEntry
import app.lounge.media.MediaKind
import app.lounge.media.OpenMovies
import app.lounge.media.assembleLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchLibraryTest {

    @Test
    fun barelyStartedAndFinishedAreNotResumable() {
        assertFalse(WatchProgress.isResumable(4_000L, 600_000L))
        assertFalse(WatchProgress.isResumable(560_000L, 600_000L))
        assertTrue(WatchProgress.isResumable(30_000L, 600_000L))
        assertEquals(0L, WatchProgress.resumeAt(560_000L, 600_000L))
        assertEquals(30_000L, WatchProgress.resumeAt(30_000L, 600_000L))
    }

    @Test
    fun remainingAndRuntimeLabels() {
        assertEquals("23 min left", WatchProgress.remainingLabel(positionMs = 120_000L, durationMs = 1_500_000L))
        assertEquals("Less than a minute left", WatchProgress.remainingLabel(590_000L, 600_000L))
        assertEquals("10 min", WatchProgress.runtimeLabel(596_000L))
    }

    @Test
    fun newestResumeIsTheHeroAndFinishedFilmsDropOff() {
        val snapshot = assembleLibrary(
            online = true,
            videosAllowed = true,
            photosAllowed = false,
            askedForAccess = true,
            scanning = false,
            localVideos = listOf(localVideo("video:9", "Porch reel")),
            photos = emptyList(),
            records = listOf(
                PlaybackRecord(OpenMovies.SINTEL, positionMs = 100_000L, durationMs = 888_000L, updatedAt = 10L),
                PlaybackRecord(OpenMovies.BIG_BUCK_BUNNY, positionMs = 580_000L, durationMs = 596_000L, updatedAt = 50L),
                PlaybackRecord("video:9", positionMs = 20_000L, durationMs = 80_000L, updatedAt = 40L),
            ),
        )

        assertEquals("video:9", snapshot.hero?.id)
        assertEquals("1 min left", snapshot.hero?.subtitle)
        assertEquals(listOf("video:9", OpenMovies.SINTEL), snapshot.continueWatching.map { it.id })
        assertEquals(
            "13 min left",
            snapshot.continueWatching.first { it.id == OpenMovies.SINTEL }.subtitle,
        )
        assertTrue(snapshot.movies.first { it.id == OpenMovies.BIG_BUCK_BUNNY }.progress == null)
        val local = snapshot.continueWatching.first { it.id == "video:9" }
        assertEquals(20_000L, local.positionMs)
        assertTrue(local.progress != null && local.progress > 0f)
    }

    @Test
    fun offlineWithNothingOnDeviceHasNoHero() {
        val snapshot = assembleLibrary(
            online = false,
            videosAllowed = false,
            photosAllowed = false,
            askedForAccess = false,
            scanning = false,
            localVideos = emptyList(),
            photos = emptyList(),
            records = emptyList(),
        )
        assertNull(snapshot.hero)
        assertEquals(4, snapshot.movies.size)
        assertTrue(snapshot.continueWatching.isEmpty())
    }

    @Test
    fun offlineLocalVideoIsPlayableHero() {
        val snapshot = assembleLibrary(
            online = false,
            videosAllowed = true,
            photosAllowed = true,
            askedForAccess = true,
            scanning = false,
            localVideos = listOf(localVideo("video:3", "Kitchen")),
            photos = emptyList(),
            records = emptyList(),
        )
        assertEquals("video:3", snapshot.hero?.id)
        assertTrue(snapshot.hero?.local == true)
    }

    private fun localVideo(id: String, title: String) = MediaEntry(
        id = id,
        title = title,
        kind = MediaKind.LOCAL_VIDEO,
        uri = "content://media/external/video/media/3",
        local = true,
        durationMs = 80_000L,
        art = ArtMotif.REEL,
    )
}
