# Pick the browser that runs your site, and keep it current

Android TV boxes have weak processors and little RAM (the Shield TV Pro has 3 GB). Heavy WebGL sites can run fine in one browser engine and badly in another, so test before you point the button at a browser.

## Test the site in each browser

Open the page in each browser you have, from the PC, one at a time:

```bash
adb -s <tv-ip>:5555 shell "am start -a android.intent.action.VIEW -d '<url>' <browser-package>"
```

Then take a screenshot and read the CPU use while it runs:

```bash
adb -s <tv-ip>:5555 exec-out screencap -p > test.png
adb -s <tv-ip>:5555 shell top -b -n1 -m 5
```

**Force-stop each browser before you test the next one** (`adb -s <tv-ip>:5555 shell am force-stop <package>`). Two browsers left running on the same page means two soundtracks and two renderers fighting over RAM, and every result gets worse.

Common packages: Firefox `org.mozilla.firefox`, Brave `com.brave.browser`, TV Bro `com.phlox.tvwebbrowser`. TV Bro renders with the system's Android WebView, which uses the same engine (Chromium) as Brave and Chrome.

## Check the version

```bash
adb -s <tv-ip>:5555 shell dumpsys package org.mozilla.firefox
```

Look for `versionName` and `lastUpdateTime`. Browsers sideloaded years ago never update themselves, because the Play Store on Android TV doesn't list them. An old browser can render a site wrong: see [lessons.md](lessons.md).

## Update Firefox from Mozilla directly

1. Find the current release number. The `version` field of this file is it:

   ```bash
   curl -s https://product-details.mozilla.org/1.0/mobile_versions.json
   ```

2. Download the arm64 build, with `<v>` replaced by that number:

   ```bash
   curl -o fenix.apk "https://archive.mozilla.org/pub/fenix/releases/<v>/android/fenix-<v>-android-arm64-v8a/fenix-<v>.multi.android-arm64-v8a.apk"
   ```

3. Check that Mozilla signed it (`apksigner` ships with the Android SDK build-tools):

   ```bash
   apksigner verify --print-certs fenix.apk
   ```

   Expect `O=Mozilla Corporation` and the certificate SHA-256 digest `a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04`.

4. Install over the old version. `-r` keeps your bookmarks and tabs. Android also refuses the update if the signature doesn't match the installed copy, which is a second check.

   ```bash
   adb -s <tv-ip>:5555 install -r fenix.apk
   ```

5. The first launch after a big version jump shows a new Terms of Use prompt. Accept it with the remote, or the page stays hidden behind it.
