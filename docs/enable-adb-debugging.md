# Turn on debugging on an Android TV and connect from a PC

`adb` (Android Debug Bridge) lets a PC install apps, take screenshots, send remote-control presses and read system state on the TV. Over your home network, it needs no cable.

## 1. Install adb on the PC

Install Google's **SDK Platform-Tools** (a standalone zip), or use the copy that comes with Android Studio under the SDK's `platform-tools` folder. Check it with `adb version`.

## 2. Turn on developer options on the TV

Menu names differ a little between devices.

- **NVIDIA Shield:** Settings > Device Preferences > About. Scroll to **Build** and press OK on it seven times, until it says you are a developer.
- **Google TV / other Android TV:** Settings > System > About. Press OK on **Android TV OS build** seven times.

A new **Developer options** menu appears (on the Shield under Device Preferences, elsewhere under System).

## 3. Turn on debugging

In Developer options:

- **Shield:** turn on **USB debugging**, then **Network debugging**. The Shield then accepts `adb` on port 5555.
- **Google TV (Android 11+):** turn on **USB debugging** and **Wireless debugging**. Wireless debugging shows its own address and port, and pairs with a code (`adb pair <ip>:<pair-port>`, then `adb connect <ip>:<port>`).

## 4. Find the TV's address

Settings > Network (or Network & Internet), then the active connection. Note the IP address. Setting a DHCP reservation for the TV in your router keeps that address from changing.

## 5. Connect

```bash
adb connect <tv-ip>:5555
```

The first time, the TV shows an **Allow USB debugging?** prompt with the PC's key fingerprint. Tick **Always allow from this computer** and accept. After that, the PC connects without a prompt.

Check the connection:

```bash
adb devices
```

The TV should be listed as `<tv-ip>:5555  device`. `unauthorized` means the prompt on the TV was not accepted yet.

## 6. Always name the device

Once a phone or a second TV is also connected, a bare `adb install` can land on the wrong one. Pin every command to the TV with `-s`:

```bash
adb -s <tv-ip>:5555 shell getprop ro.product.model
```

## Handy commands

| What | Command |
|---|---|
| Screenshot to the PC | `adb -s <tv-ip>:5555 exec-out screencap -p > tv.png` |
| Open a URL in a specific browser | `adb -s <tv-ip>:5555 shell "am start -a android.intent.action.VIEW -d '<url>' org.mozilla.firefox"` |
| Press a remote button (Back) | `adb -s <tv-ip>:5555 shell input keyevent 4` |
| See what is using the CPU | `adb -s <tv-ip>:5555 shell top -b -n1 -m 8` |
| Free memory | `adb -s <tv-ip>:5555 shell cat /proc/meminfo` |
| Installed version of an app | `adb -s <tv-ip>:5555 shell dumpsys package <package>` (look for `versionName`) |
| Screen size and scaling | `adb -s <tv-ip>:5555 shell wm size` and `wm density` |

Notes:

- **Taps use the TV's logical resolution, not the screenshot's.** A Shield on a 4K TV takes 3840 x 2160 screenshots but lays out its UI at 1920 x 1080, so a screenshot position has to be divided by 2 before `adb shell input tap x y`.
- **Check the screen before you tap.** Someone may be watching something. A tap meant for a browser menu lands on whatever app is in front.
- **Security:** network debugging lets any machine that has been allowed control the TV. Only accept the prompt for computers you own, and turn the setting off if the TV ever sits on a network you don't trust.
