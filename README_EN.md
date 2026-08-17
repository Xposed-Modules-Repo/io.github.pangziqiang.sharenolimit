# MiShareNoLimit

[简体中文](README.md)

An LSPosed module built with the traditional XposedBridge API that removes the 10-minute auto-close limit for Mi Share "Everyone" mode.

## How it works

The Mi Share app (`com.miui.mishare.connectivity`) uses the `T0.b` class (log tag `AutoClose`) to start a 10-minute countdown when "Everyone" mode is enabled, then closes "Everyone" mode when the countdown finishes.

This module hooks:

- `T0.b.f()` — skips starting the countdown.
- `T0.b.d()` — prevents the auto-close action even if the countdown fires.

This double protection keeps "Everyone" mode enabled indefinitely.

## Compatibility

| Component | Requirement |
| --- | --- |
| Target app | Mi Share 5.1.12 (`com.miui.mishare.connectivity`) |
| Framework | LSPosed (traditional XposedBridge module format) |
| Android | Android 10 (API 29) or later |
| Module version | 1.6 (versionCode 7) |

The module relies on internal class/method names from the Mi Share APK. Compatibility with other versions is not guaranteed.

## Installation

1. Download and install the APK from the GitHub Releases page.
2. Enable the module in LSPosed Manager.
3. Select `com.miui.mishare.connectivity` as the scope.
4. Reboot the device.
5. Open Mi Share "Everyone" mode and verify it stays on for more than 10 minutes.

Verified: "Everyone" mode remained enabled after more than 10 minutes.

The module has no launcher icon. All management is done in LSPosed Manager.

## Building

The project requires JDK 17+ and Android SDK 34+.

```bash
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## License

[MIT License](LICENSE)

## Disclaimer

This project is a community module and is not affiliated with or endorsed by Xiaomi Inc., Mi Share, or the LSPosed project. It is intended for learning, research, and personal-device use. Verify that your use complies with applicable laws and relevant terms of service.
