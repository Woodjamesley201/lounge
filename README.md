# Lounge

A living-room app for Android tablets. On the tablet it is a touch-friendly media home. When a bigger display is connected — wirelessly, or over HDMI on devices that support it — it switches to a 10-foot TV layout.

First device: Samsung Galaxy Tab A9 (SM-X110). That model cannot output video over USB-C HDMI; wireless (Smart View or Cast) is the path that works.

## Build

Needs JDK 17 and the Android SDK (`sdk.dir` in `local.properties`).

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Install on the tablet, allow unknown apps if asked. Long-press the "Lounge" title to preview TV mode without a second display.

## Project layout

See [COLLAB.md](COLLAB.md) for who owns what.

## Release

### v0.1.0

Debug APK for the Galaxy Tab A9 (SM-X110). Long-press the Lounge title to preview TV mode. Use Smart View for wireless auto-detect. The SM-X110 cannot output video over USB-C HDMI.
