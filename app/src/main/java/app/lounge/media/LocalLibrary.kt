package app.lounge.media

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import app.lounge.playback.WatchProgress
import kotlin.math.max

/** MediaStore scan for videos and photos the person already has on the device. */
object LocalLibrary {
    private const val VIDEO_LIMIT = 40
    private const val PHOTO_LIMIT = 48

    fun videos(context: Context): List<MediaEntry> = query(
        context = context,
        collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        },
        projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED,
        ),
        sortColumn = MediaStore.Video.Media.DATE_ADDED,
        limit = VIDEO_LIMIT,
    ) { cursor ->
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
        val title = displayTitle(
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)),
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)),
            fallback = "Video",
        )
        val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION))
        val uri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            id,
        )
        MediaEntry(
            id = "video:$id",
            title = title,
            subtitle = WatchProgress.runtimeLabel(duration).ifBlank { "On this tablet" },
            kind = MediaKind.LOCAL_VIDEO,
            uri = uri.toString(),
            local = true,
            durationMs = duration.coerceAtLeast(0L),
            art = ArtMotif.REEL,
        )
    }

    fun photos(context: Context): List<MediaEntry> = query(
        context = context,
        collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        },
        projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
        ),
        sortColumn = MediaStore.Images.Media.DATE_ADDED,
        limit = PHOTO_LIMIT,
    ) { cursor ->
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
        val title = displayTitle(
            title = null,
            displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
            fallback = "Photo",
        )
        val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id,
        )
        MediaEntry(
            id = "photo:$id",
            title = title,
            subtitle = null,
            kind = MediaKind.PHOTO,
            uri = uri.toString(),
            local = true,
            art = ArtMotif.STILL,
        )
    }

    private fun <T> query(
        context: Context,
        collection: Uri,
        projection: Array<String>,
        sortColumn: String,
        limit: Int,
        map: (android.database.Cursor) -> T,
    ): List<T> {
        val selection = buildString {
            append("${MediaStore.MediaColumns.SIZE} > 0")
            if (Build.VERSION.SDK_INT >= 29) {
                append(" AND ${MediaStore.MediaColumns.IS_PENDING} = 0")
            }
        }
        val args = Bundle().apply {
            putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(
                android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(sortColumn),
            )
            putInt(
                android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION,
                android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            )
            putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, limit)
        }
        return try {
            context.contentResolver.query(collection, projection, args, null)?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(map(cursor))
                    }
                }
            } ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
    }

    private fun displayTitle(title: String?, displayName: String?, fallback: String): String {
        val raw = when {
            !title.isNullOrBlank() && !title.equals("<unknown>", ignoreCase = true) -> title
            !displayName.isNullOrBlank() -> displayName
            else -> fallback
        }
        val base = raw.substringAfterLast('/').substringBeforeLast('.')
        return base.replace('_', ' ').replace('-', ' ').trim().ifBlank { fallback }
    }
}

/** Small decoded frames for shelves and the photo viewer. */
object Thumbnails {
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun load(context: Context, uri: Uri, sizePx: Int): Bitmap? {
        cache.get(uri.toString())?.let { return it }
        val bitmap = decode(context, uri, sizePx.coerceIn(96, 2048)) ?: return null
        cache.put(uri.toString(), bitmap)
        return bitmap
    }

    private fun decode(context: Context, uri: Uri, sizePx: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                return context.contentResolver.loadThumbnail(uri, Size(sizePx, sizePx), null)
            } catch (_: Exception) {
                // Fall through to a manual decode. Videos on older volumes sometimes refuse.
            }
        }
        return decodeSampled(context, uri, sizePx)
    }

    private fun decodeSampled(context: Context, uri: Uri, sizePx: Int): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, sizePx)
        }
        return resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun sampleSize(width: Int, height: Int, target: Int): Int {
        var size = 1
        while (width / size > target * 2 && height / size > target * 2) {
            size *= 2
        }
        return max(1, size)
    }
}
