package app.lounge.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/** Runtime read access for videos and photos already on the device. */
object MediaPermissions {
    fun requestSet(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        )
        Build.VERSION.SDK_INT >= 33 -> arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /** The permission we ask the system about when explaining a denial. */
    fun primary(): String = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun canReadVideos(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 34 && granted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
            return true
        }
        return if (Build.VERSION.SDK_INT >= 33) {
            granted(context, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            granted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun canReadPhotos(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 34 && granted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
            return true
        }
        return if (Build.VERSION.SDK_INT >= 33) {
            granted(context, Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            granted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun granted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
