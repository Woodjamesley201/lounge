package app.lounge.media

import app.lounge.playback.WatchProgress

/**
 * Blender Foundation open movies, streamed over HTTPS.
 * Nothing is bundled in the APK.
 *
 * The films are Creative Commons Attribution. The Internet Archive hosts
 * stable public copies and accepts range requests, which ExoPlayer needs
 * in order to start playback without downloading the whole file.
 *
 * - Big Buck Bunny (2008), CC BY 3.0 — peach.blender.org
 * - Sintel (2010), CC BY 3.0 — durian.blender.org
 * - Tears of Steel (2012), CC BY 3.0 — mango.blender.org
 * - Elephants Dream (2006), CC BY 2.5 — orange.blender.org
 */
object OpenMovies {
    const val BIG_BUCK_BUNNY = "open:big-buck-bunny"
    const val SINTEL = "open:sintel"
    const val TEARS_OF_STEEL = "open:tears-of-steel"
    const val ELEPHANTS_DREAM = "open:elephants-dream"

    val all: List<MediaEntry> = listOf(
        film(
            id = BIG_BUCK_BUNNY,
            title = "Big Buck Bunny",
            uri = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
            durationMs = 596_000L,
            art = ArtMotif.MEADOW,
            credit = "Blender Foundation · CC BY 3.0",
        ),
        film(
            id = SINTEL,
            title = "Sintel",
            uri = "https://archive.org/download/Sintel/sintel-2048-stereo.mp4",
            durationMs = 888_000L,
            art = ArtMotif.FROST,
            credit = "Blender Foundation · CC BY 3.0",
        ),
        film(
            id = TEARS_OF_STEEL,
            title = "Tears of Steel",
            uri = "https://archive.org/download/Tears-of-Steel/tears_of_steel_1080p.mp4",
            durationMs = 734_000L,
            art = ArtMotif.FORGE,
            credit = "Blender Foundation · CC BY 3.0",
        ),
        film(
            id = ELEPHANTS_DREAM,
            title = "Elephants Dream",
            uri = "https://archive.org/download/ElephantsDream/ed_1024.mp4",
            durationMs = 654_000L,
            art = ArtMotif.DREAM,
            credit = "Blender Foundation · CC BY 2.5",
        ),
    )

    fun byId(id: String): MediaEntry? = all.find { it.id == id }

    private fun film(
        id: String,
        title: String,
        uri: String,
        durationMs: Long,
        art: ArtMotif,
        credit: String,
    ) = MediaEntry(
        id = id,
        title = title,
        subtitle = WatchProgress.runtimeLabel(durationMs),
        kind = MediaKind.FILM,
        uri = uri,
        local = false,
        durationMs = durationMs,
        art = art,
        credit = credit,
    )
}
