package app.lounge.diagnostics

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Safety net for launch crashes. A Java/Kotlin crash is written to
 * filesDir/last-crash.txt before the default handler kills the process.
 * On the next launch [pending] returns that text (or, on Android 11+, a
 * native or otherwise unrecorded crash from [ApplicationExitInfo]) so the
 * app can show it instead of silently closing again.
 */
object CrashReport {
    private const val FILE = "last-crash.txt"
    private const val PREFS = "lounge_crash"
    private const val SEEN_EXIT = "seen_exit_timestamp"

    @Volatile
    private var installed = false

    fun install(context: Context) {
        if (installed) return
        installed = true
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                File(appContext.filesDir, FILE).writeText(
                    format(
                        header = header(appContext),
                        threadName = thread.name,
                        trace = stackTrace(error),
                    ),
                )
            }
            if (previous != null) {
                previous.uncaughtException(thread, error)
            } else {
                Process.killProcess(Process.myPid())
                System.exit(10)
            }
        }
    }

    /** Text of the previous run's crash, or null if it ended normally. */
    fun pending(context: Context): String? {
        val file = File(context.filesDir, FILE)
        val written = runCatching { if (file.exists()) file.readText() else null }.getOrNull()
        val exit = runCatching { lastExitCrash(context) }.getOrNull()
        return when {
            !written.isNullOrBlank() -> written
            exit != null -> exit
            else -> null
        }
    }

    /** Forget the shown report so it does not come back next launch. */
    fun clear(context: Context) {
        runCatching { File(context.filesDir, FILE).delete() }
        if (Build.VERSION.SDK_INT >= 30) {
            runCatching {
                val latest = latestExit(context)?.timestamp ?: return@runCatching
                prefs(context).edit().putLong(SEEN_EXIT, latest).commit()
            }
        }
    }

    fun format(header: String, threadName: String, trace: String): String = buildString {
        appendLine(header)
        appendLine("Thread: $threadName")
        appendLine()
        append(trace.trimEnd())
        appendLine()
    }

    private fun stackTrace(error: Throwable): String {
        val writer = StringWriter()
        PrintWriter(writer).use { error.printStackTrace(it) }
        return writer.toString().ifBlank { Log.getStackTraceString(error) }
    }

    private fun header(context: Context): String {
        val version = runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            "${info.versionName} (${if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()})"
        }.getOrDefault("?")
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date())
        return "Lounge $version · ${Build.MANUFACTURER} ${Build.MODEL} · " +
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) · $time"
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun latestExit(context: Context): ApplicationExitInfo? {
        if (Build.VERSION.SDK_INT < 30) return null
        val manager = context.getSystemService(ActivityManager::class.java) ?: return null
        return manager.getHistoricalProcessExitReasons(context.packageName, 0, 1).firstOrNull()
    }

    /**
     * A crash of the previous run that the handler could not record (native
     * aborts, for example). Ignores exits from before this version was
     * installed and ones already shown.
     */
    private fun lastExitCrash(context: Context): String? {
        if (Build.VERSION.SDK_INT < 30) return null
        val info = latestExit(context) ?: return null
        val crash = info.reason == ApplicationExitInfo.REASON_CRASH ||
            info.reason == ApplicationExitInfo.REASON_CRASH_NATIVE
        if (!crash) return null
        val installedAt = context.packageManager
            .getPackageInfo(context.packageName, 0).lastUpdateTime
        if (info.timestamp <= installedAt) return null
        if (info.timestamp <= prefs(context).getLong(SEEN_EXIT, 0L)) return null
        val kind = if (info.reason == ApplicationExitInfo.REASON_CRASH_NATIVE) {
            "Native crash"
        } else {
            "Crash"
        }
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(info.timestamp))
        return buildString {
            appendLine(header(context))
            appendLine()
            appendLine("$kind reported by Android at $time")
            appendLine("Status: ${info.status}  Importance: ${info.importance}")
            info.description?.let { appendLine("Description: $it") }
        }
    }
}
