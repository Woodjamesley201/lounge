package app.lounge.diagnostics

import org.junit.Assert.assertTrue
import org.junit.Test

class CrashReportFormatTest {
    @Test
    fun formatKeepsHeaderThreadAndTrace() {
        val text = CrashReport.format(
            header = "Lounge 0.2.3",
            threadName = "main",
            trace = "java.lang.IllegalStateException: boom\n\tat a.b.C.d(C.kt:1)\n\n",
        )
        assertTrue(text.startsWith("Lounge 0.2.3\nThread: main\n\n"))
        assertTrue(text.contains("IllegalStateException: boom"))
        assertTrue(text.endsWith("(C.kt:1)\n"))
    }
}
