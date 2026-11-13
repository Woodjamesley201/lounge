# Lounge

A living-room app for Android tablets. On the tablet it is a touch-friendly media home. When a bigger display is connected — wirelessly, or over HDMI on devices that support it — it switches to a 10-foot TV layout.

The home is translucent glass over a dusk room: backdrop blur on Android 12–15 when the GPU can do it, and a frosted veil, specular streak, and rim everywhere else. Samsung devices and Android 16 do not call platform blur, and they do not paint the stacked radial dusk shaders either. On the Galaxy Tab A9 those shaders were still aborting the GPU at the lamp splash after 0.2.1. Those devices get a linear dusk, a milk veil, and a solid lamp glow. The first frame is a flat night color, and display and library startup wait until that frame has been drawn. The lamp wordmark is still Lounge.

First device: Samsung Galaxy Tab A9 (SM-X110). That model cannot output video over USB-C HDMI; wireless (Smart View or Cast) is the path that works.

This is a **debug build**, not a Play Store release. Do not ship the debug APK as a store listing. Version name `0.2.2` is the in-app version; release tags are cut separately.

## Build

Needs JDK 17 (or 21) and the Android SDK (`sdk.dir` in `local.properties`). `local.properties` is gitignored.

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Install on the tablet, allow unknown apps if asked. Long-press the "Lounge" title to preview TV mode without a second display.

## Playback

Tapping a film, the feature pane, Resume, or a video on the tablet opens an in-app player (AndroidX Media3 / ExoPlayer). It can play, pause, and seek, and the system Back key or the close control leaves the player. A D-pad or Bluetooth remote can move between close, play/pause, and the seek bar; left and right on the bar seek, and media play/pause keys toggle playback.

Where you stopped is stored on the device (SharedPreferences) and read back after the process is killed. "Pick back up" and "Continue watching" use that position. A film that has played out starts from the beginning the next time.

## Content

Open movies stream over HTTPS. They are not packed into the APK. All four are Blender Foundation films under Creative Commons Attribution, served from stable Internet Archive copies that allow range requests:

| Film | License | Stream |
| --- | --- | --- |
| Big Buck Bunny (2008) | CC BY 3.0 | `archive.org` item `BigBuckBunny_124` |
| Sintel (2010) | CC BY 3.0 | `archive.org` item `Sintel` |
| Tears of Steel (2012) | CC BY 3.0 | `archive.org` item `Tears-of-Steel` |
| Elephants Dream (2006) | CC BY 2.5 | `archive.org` item `ElephantsDream` |

Canonical project pages: [peach.blender.org](https://peach.blender.org/), [durian.blender.org](https://durian.blender.org/), [mango.blender.org](https://mango.blender.org/), [orange.blender.org](https://orange.blender.org/).

Videos and photos already on the tablet show up beside those films. Lounge does not ask for storage access at launch. A card on the home screen explains why, and the button starts the system permission dialog (`READ_MEDIA_VIDEO` and `READ_MEDIA_IMAGES` on Android 13+, `READ_EXTERNAL_STORAGE` on older versions). If access is turned off for good, the card links to system settings. Photos open in a viewer. If there is no network and nothing saved on the tablet, the home says so — a tile is never a dead tap.

## Display

Wired and wireless detection are unchanged. Long-press the Lounge title to force the television layout. Use Smart View or Cast for a real external screen. The SM-X110 cannot output video over USB-C HDMI; HDMI detection stays in the app for devices that can.

## Project layout

See [COLLAB.md](COLLAB.md) for who owns what. Playback, the catalog, and permissions live under `app.lounge.media`, `app.lounge.playback`, and `app.lounge.ui`.

Compose previews of the tablet and television homes are on `LoungeHome`.
