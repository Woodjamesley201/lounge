package app.lounge.playback

import android.content.Context
import org.json.JSONObject

/**
 * Playback positions on disk. [save] updates memory immediately so the
 * shelves can refresh before the write finishes. [flush] uses commit()
 * so a process kill after leaving the app still keeps the last position.
 */
class PlaybackStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val lock = Any()
    private var cache: MutableMap<String, PlaybackRecord>? = null

    fun records(): List<PlaybackRecord> = synchronized(lock) {
        load().values.toList()
    }

    fun record(id: String): PlaybackRecord? = synchronized(lock) {
        load()[id]
    }

    fun resumePosition(id: String): Long {
        val saved = record(id) ?: return 0L
        return WatchProgress.resumeAt(saved.positionMs, saved.durationMs)
    }

    fun save(id: String, positionMs: Long, durationMs: Long, durable: Boolean) {
        synchronized(lock) {
            val map = load()
            val previous = map[id]
            val duration = when {
                durationMs > 0L -> durationMs
                else -> previous?.durationMs ?: 0L
            }
            map[id] = PlaybackRecord(
                mediaId = id,
                positionMs = positionMs.coerceAtLeast(0L),
                durationMs = duration,
                updatedAt = System.currentTimeMillis(),
            )
            if (map.size > MAX_RECORDS) {
                map.values
                    .sortedBy { it.updatedAt }
                    .take(map.size - MAX_RECORDS)
                    .forEach { map.remove(it.mediaId) }
            }
            write(map, durable)
        }
    }

    fun flush() {
        synchronized(lock) {
            cache?.let { write(it, durable = true) }
        }
    }

    private fun load(): MutableMap<String, PlaybackRecord> {
        cache?.let { return it }
        val parsed = mutableMapOf<String, PlaybackRecord>()
        val raw = prefs.getString(KEY, null)
        if (!raw.isNullOrBlank()) {
            runCatching {
                val root = JSONObject(raw)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val id = keys.next()
                    val item = root.getJSONObject(id)
                    parsed[id] = PlaybackRecord(
                        mediaId = id,
                        positionMs = item.optLong("positionMs"),
                        durationMs = item.optLong("durationMs"),
                        updatedAt = item.optLong("updatedAt"),
                    )
                }
            }
        }
        cache = parsed
        return parsed
    }

    private fun write(map: MutableMap<String, PlaybackRecord>, durable: Boolean) {
        cache = map
        val root = JSONObject()
        map.values.forEach { record ->
            root.put(
                record.mediaId,
                JSONObject()
                    .put("positionMs", record.positionMs)
                    .put("durationMs", record.durationMs)
                    .put("updatedAt", record.updatedAt),
            )
        }
        val editor = prefs.edit().putString(KEY, root.toString())
        if (durable) editor.commit() else editor.apply()
    }

    private companion object {
        const val PREFS = "lounge_playback"
        const val KEY = "records"
        const val MAX_RECORDS = 100
    }
}
