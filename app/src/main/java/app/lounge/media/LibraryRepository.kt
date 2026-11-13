package app.lounge.media

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import app.lounge.playback.PlaybackStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Open movies, on-device videos and photos, and whether the network is up.
 * Playback positions come from [PlaybackStore]. Display detection stays in
 * [app.lounge.display.DisplayConnectionRepository].
 */
class LibraryRepository(
    context: Context,
    private val playback: PlaybackStore,
) {
    private val appContext = context.applicationContext
    private val connectivity = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val accessPrefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _snapshot = MutableStateFlow(publishValue(scanning = false))
    val snapshot: StateFlow<LibrarySnapshot> = _snapshot.asStateFlow()

    private var scope: CoroutineScope? = null
    private var started = false
    private var localVideos: List<MediaEntry> = emptyList()
    private var photos: List<MediaEntry> = emptyList()
    private var scanning = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = onNetworkChanged()
        override fun onLost(network: Network) = onNetworkChanged()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = onNetworkChanged()
    }

    fun start() {
        if (started) {
            refresh()
            return
        }
        started = true
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { connectivity.registerNetworkCallback(request, networkCallback) }
        refresh()
    }

    fun stop() {
        if (!started) return
        started = false
        runCatching { connectivity.unregisterNetworkCallback(networkCallback) }
        scope?.cancel()
        scope = null
    }

    fun refresh() {
        val current = scope ?: return
        current.launch { reload() }
    }

    /** Re-read saved positions after the player closes. */
    fun refreshProgress() {
        _snapshot.value = publishValue(scanning = scanning)
    }

    fun onPermissionResult(result: Map<String, Boolean>) {
        accessPrefs.edit().putBoolean(ASKED, true).apply()
        refresh()
    }

    private fun onNetworkChanged() {
        scope?.launch { _snapshot.value = publishValue(scanning = scanning) }
    }

    private suspend fun reload() {
        scanning = true
        _snapshot.value = publishValue(scanning = true)
        val videosAllowed = MediaPermissions.canReadVideos(appContext)
        val photosAllowed = MediaPermissions.canReadPhotos(appContext)
        val scannedVideos = withContext(Dispatchers.IO) {
            if (videosAllowed) LocalLibrary.videos(appContext) else emptyList()
        }
        val scannedPhotos = withContext(Dispatchers.IO) {
            if (photosAllowed) LocalLibrary.photos(appContext) else emptyList()
        }
        localVideos = scannedVideos
        photos = scannedPhotos
        scanning = false
        _snapshot.value = publishValue(scanning = false)
    }

    private fun publishValue(scanning: Boolean): LibrarySnapshot {
        return assembleLibrary(
            online = isOnline(),
            videosAllowed = MediaPermissions.canReadVideos(appContext),
            photosAllowed = MediaPermissions.canReadPhotos(appContext),
            askedForAccess = accessPrefs.getBoolean(ASKED, false),
            scanning = scanning,
            localVideos = localVideos,
            photos = photos,
            records = playback.records(),
        )
    }

    private fun isOnline(): Boolean {
        val network = connectivity.activeNetwork ?: return false
        val caps = connectivity.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        const val PREFS = "lounge_access"
        const val ASKED = "asked_for_media"
    }
}
