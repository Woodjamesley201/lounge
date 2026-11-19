package app.lounge

import android.app.Application
import app.lounge.diagnostics.CrashReport

class LoungeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReport.install(this)
    }
}
