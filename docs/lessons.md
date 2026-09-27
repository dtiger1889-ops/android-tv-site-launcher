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

Each press of the button sends the URL to Firefox again, and Firefox opens it in a new tab. On a 3 GB box, old tabs pile up. Close them now and then from Firefox's tab list.
