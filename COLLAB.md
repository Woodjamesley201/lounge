# Lounge — collaboration

A TV-mode Android app for Rahim's Galaxy Tab A9 (SM-X110, 8.7"). It should feel like a living-room home screen on the tablet, and automatically switch to a 10-foot TV layout when a bigger display is connected — wirelessly today, and over HDMI on a future device that supports it.

This is a new project. It is not OneOS or Aqualux; leave those alone.

## Ownership

| Person | Owns | Do not rewrite |
| --- | --- | --- |
| **Display Manager Jayden** | Wired / system displays: `WiredDisplayMonitor.kt`, DisplayManager listener, HDMI and Presentation detection, combining sources in `DisplayConnectionRepository.kt` | UI composables; wireless/Cast code |
| **Scripting Mike** | Wireless displays: `WirelessDisplayMonitor.kt`, MediaRouter, Smart View / Miracast, Google Cast, feeding a wireless `DisplayConnection?` into the repository | UI composables; DisplayManager / HDMI code |
| **UI Isaiah** | `app/src/main/java/app/lounge/ui/` and visual resources — theme, tablet home, TV home, tiles, focus, motion, icon | Display detection logic |
| **Goji** | Project scaffold, `MainActivity.kt`, `LoungeApp.kt`, `DisplayMode.kt` (the shared API), Gradle, README, integration | — |

Shared files (`MainActivity.kt`, `LoungeApp.kt`, `AndroidManifest.xml`, Gradle, `DisplayMode.kt`): propose a change rather than silently restyling the other person's work.

The public API for the UI is [`DisplayConnection`](app/src/main/java/app/lounge/display/DisplayMode.kt) and `DisplayConnectionRepository.connection`. Jayden's monitor and Mike's monitor each report an optional connection; the repository prefers a real Presentation/HDMI display, then a wireless route, otherwise tablet-only.

## Hardware constraints (SM-X110)

- USB-C is USB 2.0. **No DisplayPort Alt Mode, no USB-C-to-HDMI video.** A cable will not create a second display on this tablet. Still listen for HDMI so the same app works later on a tablet that can do it.
- Wireless works: Samsung Smart View (Miracast) and Google Cast.
- Smart View usually **mirrors** the tablet. Switching the on-device UI to TV mode is what the TV will show. Cast can use a receiver on the TV and turn the tablet into a remote.

## First version

1. Auto-detect a bigger display and flip `DisplayMode` between `TABLET` and `TELEVISION`.
2. Two original layouts (do **not** copy Apple tvOS or any commercial OS: no assets, logos, or look-alike chrome).
3. Placeholder rows: Continue watching, Movies, Shows, Photos.
4. D-pad / Bluetooth remote focus in TV mode.
5. A debug way to force TV mode on the tablet alone (`toggleDebugTelevision()`), already wired to a long-press on the "Lounge" title. Jayden owns this override on the repository.

## How to preview TV mode without a TV

Long-press the "Lounge" title, which calls `DisplayConnectionRepository.toggleDebugTelevision()`.
