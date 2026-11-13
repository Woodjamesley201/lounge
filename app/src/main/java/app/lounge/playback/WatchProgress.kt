package app.lounge.playback

/**
 * Rules for "pick back up" and "continue watching".
 * A few seconds in does not count, and a film that has played out starts over.
 */
object WatchProgress {
    const val START_MS = 5_000L
    const val DONE_FRACTION = 0.92f

    fun isResumable(positionMs: Long, durationMs: Long): Boolean {
        if (positionMs < START_MS) return false
        if (durationMs <= 0L) return true
        return positionMs < (durationMs * DONE_FRACTION).toLong()
    }

    fun fraction(positionMs: Long, durationMs: Long): Float {
        if (durationMs <= 0L) return 0f
        return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun resumeAt(positionMs: Long, durationMs: Long): Long {
        return if (isResumable(positionMs, durationMs)) positionMs else 0L
    }

    fun remainingLabel(positionMs: Long, durationMs: Long): String {
        if (durationMs <= 0L) return "Resume"
        val leftMs = (durationMs - positionMs).coerceAtLeast(0L)
        val minutes = (leftMs / 60_000L).toInt()
        return if (minutes <= 0) "Less than a minute left" else "$minutes min left"
    }

    fun runtimeLabel(durationMs: Long): String {
        if (durationMs <= 0L) return ""
        val minutes = ((durationMs + 30_000L) / 60_000L).toInt().coerceAtLeast(1)
        return "$minutes min"
    }
}
