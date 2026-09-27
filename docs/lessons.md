# What went wrong, in order

The goal: put the gcdatlas grand tour on an NVIDIA Shield TV Pro (2019, Android 11, 3 GB RAM) as one button. Every dead end below cost real time. Each one is kept here because the symptom is easy to misread.

## 1. Brave: the page loads, the 3D view stays black

Brave (Chromium engine) drew the page's text labels, but the 3D canvas stayed pure black. WebGL2 reported healthy: an NVIDIA Tegra renderer, the context not lost, and every extension the site needs. The cause was never found. **Lesson:** "WebGL works" in a capability check doesn't mean a given site renders. Look at a screenshot.

## 2. Old Firefox: it renders and runs smoothly, but washed out

Firefox drew everything and ran smoothly, but empty space came out slate grey (about RGB 28, 34, 46) where the site means near-black (RGB 4, 5, 10). Turning the site's glow off helped only a little. The installed Firefox turned out to be **version 94, sideloaded in 2021**. It had never updated, because the Play Store on Android TV doesn't list Firefox. Updating to the current release (156) gave a true black background with no other change.

**Lesson:** check the browser's version before you debug its rendering. That's a one-line check (`dumpsys package`), and it would have saved every step below.

## 3. Chromium browsers: stutter and crackling audio

TV Bro, which uses the system Android WebView (also Chromium), rendered the colours correctly. But the page's renderer process sat at 180-240% CPU, so the animation stuttered and the site's live-generated music crackled. With Firefox 94 and Brave still open on the same page in the background, two soundtracks played at once and RAM ran out, which made everything worse.

Two things did **not** help:

- **Switching off the live overlays** (`?flags=-live,-planes,-launches`). This cuts the 16,000+ tracked satellites, but CPU stayed at about 180-236%.
- **A custom WebView app.** This was the first plan for the button. It was dropped once the WebView turned out to use the same Chromium engine and the same CPU.

**Lesson:** on this hardware the engine matters more than any site setting. The button hands the URL to Firefox instead of embedding a WebView.

## 4. A viewport "fix" that would have made it worse

The page reported a 960 x 540 screen. That's because the Shield runs its 1080p UI at 2x scaling, and the size pushed the site into its reduced-quality phone mode. The first idea was to rewrite the page's viewport to 1920 wide. That would have **quadrupled** the pixels the GPU draws, because the browser still multiplies by the 2x scale factor.

**Lesson:** read `wm size` and `wm density` before touching the viewport. The canvas was already at full 1080p resolution.

## 5. Firefox remote debugging on Android TV: the connection works, code evaluation doesn't

Firefox's "Remote debugging via USB" switch exposes a debugger socket (`localabstract:org.mozilla.firefox/firefox-debugger-socket`) that `adb forward` can reach. Listing tabs and attaching to the console both worked on Firefox 94. But `evaluateJSAsync` answered with a result ID and never ran the code, not even on example.com. This was not retested on Firefox 156.

**Lesson:** for page-level inspection on the TV, Brave or Chrome over `chrome_devtools_remote` is the reliable path. Firefox debugging needs the desktop Firefox `about:debugging` page, not a hand-written client.

## 6. Launching opens a new tab every time

The first version sent the URL to Firefox as an ordinary link, and Firefox opened every press in a new tab. On a 3 GB box, old tabs piled up. The fix is one extra on the intent (`android.support.customtabs.extra.SESSION`), which asks for a **Custom Tab**: a throwaway window that never joins the tab list and closes completely when you press Back. It uses the same engine and runs just as smoothly. Custom Tabs still show a small bar with the page title at the bottom of the screen.

## 7. Custom Tabs still show a bar, and the music waited for a click

The Custom Tab left a title bar along the bottom of the screen. gcdatlas publishes no web-app manifest, so Firefox couldn't open it as a full-screen web app either. The site also starts its music only after a first click, and a fresh window hasn't had one.

The fix was to stop handing the URL to a browser at all. The app now embeds **GeckoView**, Firefox's engine as a library (`org.mozilla.geckoview:geckoview-arm64-v8a` from maven.mozilla.org). It draws the page in a full-screen window of its own, grants the page permission to autoplay sound, and taps the page's right edge four seconds after it loads, to count as the first click.

Two build problems, and what fixed them:

- **GeckoView 156 declares that it needs compileSdk 36**, which Android Gradle Plugin 8.5 doesn't support. The app only calls long-standing GeckoView APIs, so `app/build.gradle.kts` switches off that one metadata check (`CheckAarMetadataTask`) instead of upgrading the whole toolchain.
- **GeckoView ships kotlin-stdlib 2.4**, whose class metadata the Kotlin 1.9 compiler can't read. The activity is plain Java, which avoids the Kotlin compiler entirely.

The cost is size: the APK is about 200 MB. On the Shield, the page rendered full screen and its audio started with no input. One CPU reading, taken 30 seconds after launch while the page was still loading, showed about 270% across the app's processes, against about 120% for Firefox mid-tour; the two haven't been compared under the same conditions.
