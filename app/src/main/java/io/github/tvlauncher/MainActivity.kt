package io.github.tvlauncher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

// A home-screen button: hands one URL to one browser, then closes itself.
// The page runs in a real browser rather than an embedded WebView so you can pick
// the browser engine that performs best on your TV (see docs/lessons.md).
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = Uri.parse(BuildConfig.LAUNCH_URL)
        // The SESSION extra asks the browser for a Custom Tab: a throwaway window that never
        // joins the tab list and is gone when you press Back, so presses don't stack up tabs.
        val inBrowser = Intent(Intent.ACTION_VIEW, uri)
            .setPackage(BuildConfig.BROWSER_PACKAGE)
            .putExtras(Bundle().apply { putBinder("android.support.customtabs.extra.SESSION", null) })
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // If the chosen browser isn't installed, fall back to whatever handles web links.
        runCatching { startActivity(inBrowser) }
            .onFailure { startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        finish()
    }
}
