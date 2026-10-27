# ShareNoLimit

[简体中文](README.md)

An LSPosed module built with the traditional XposedBridge API that removes the 10-minute auto-close/disband limits in system sharing apps.

## Project history and verification status

The project originally targeted only the 10-minute auto-close limit in Mi Share "Everyone" mode, then expanded to OPPO/ColorOS, vivo/iQOO and Samsung Share. Because only Xiaomi and OPPO devices are available, Mi Share and OPPO/ColorOS have been verified on real devices. The vivo/iQOO and Samsung adapters are based on static APK analysis and have not yet been verified on hardware, so they may stop working depending on the system or sharing-app version.

When reporting an issue, include the device model, system version, sharing-app version and LSPosed log.

> Starting with 1.9, the module package changed from `io.github.pangziqiang.misharenolimit` to `io.github.pangziqiang.sharenolimit`, so it cannot update the old package in place. Disable and uninstall the old module in LSPosed Manager, install the new version, and select its scopes again.

Supported targets:

- Mi Share `com.miui.mishare.connectivity`
- ColorOS / realme UI Share `com.coloros.oshare`, plus `com.oplus.oshare` on some OnePlus/OxygenOS builds
- vivo / iQOO Share `com.vivo.easyshare`
- Samsung S Share / Quick Share `com.samsung.android.app.sharelive`

## How it works

### Mi Share

The Mi Share app uses the `T0.b` class (log tag `AutoClose`) to start a 10-minute countdown when "Everyone" mode is enabled. This module hooks:

- `T0.b.f()` — skips starting the countdown.
- `T0.b.d()` — prevents the auto-close action even if the countdown fires.

### ColorOS / realme / OnePlus Share

In ColorOS Share 16.10.70 and 17.2.35, the receiving state is stored in `oshare_state`:

- `0` — receiving off
- `1` — Everyone (for 10 minutes)
- `2` — Contacts only

The default duration comes from `assets/apps_oshare_feature_config.xml` (`SwitchTimeOut=10`). After "Everyone" is enabled, TimeoutManager schedules an alarm and `TimeoutReceiver` handles the `oplus.action.oshare.switch.timeout` broadcast.

This module hooks:

- `jb.b.l()` (17.x) and `wa.b.l()` (16.x) — returns `-1`, so no alarm is scheduled and TimeoutReceiver treats the switch as never expiring.
- TimeoutManager `v9.e.d()` / `v9.e.e()` (17.x) and `y8.f.d()` / `y8.f.e()` (16.x) — skips scheduling the switch alarm and updating a new expiry time.
- `com.oplus.oshare.utils.s1.g()` (17.x) and `lb.s1.g()` (16.x) — returns `0` so the settings page and Quick Settings panel cannot expire the mode through their countdown UI.
- `com.oplus.oshare.device.TimeoutReceiver.onReceive()` — ignores the switch timeout broadcast.

These layers keep "Everyone" mode enabled and have been verified on an OPPO/ColorOS device.

### vivo / iQOO Share

vivo uses a different mechanism: there is no "Everyone visibility expires after 10 minutes" state. Instead, an idle shake-to-share group is automatically dissolved:

- The default `auto_disband_duration` is 600000 ms (10 minutes).
- The settings screen offers 30 minutes, 1 hour and 3 hours.
- `ShakeDevicesConnectManager.I(true)` posts a 1-minute warning and a dissolve task.

This module hooks `ShakeDevicesConnectManager.I(boolean)`:

- `true` skips both delayed dissolve tasks;
- `false` still runs the original cleanup path, so leaving a group or turning the feature off cancels timers correctly;
- `S()` returns `false` so the UI does not show a countdown that is about to dissolve the group.

Adapted class names: `ic.m` in vivo Share 7.4.5.1 and `hc.m` in 7.4.2.15. This adapter comes from static APK analysis and has not been verified on a vivo/iQOO device. Obfuscated class names can change between vivo releases; if the log says `ShakeDevicesConnectManager not found`, the module needs a new version adapter.

### Samsung S Share / Quick Share

The Chinese Samsung share app schedules an `AlarmManager` expiry about 10 minutes after "Everyone" is enabled. It sends `com.samsung.android.app.sharelive.UPDATE_DISCOVERY_ALARM`, which is handled by `DiscoverySettingAlarmReceiver`. After a reboot, the same receiver also handles `LAZY_BOOT_COMPLETE` to restore or immediately run the expiry check.

This module hooks:

- `oc.o5.n(Long)` in 13.6.11.10 and `b7.F2.o(Long)` in 13.8.0.47 — no new expiry alarm is scheduled;
- `com.samsung.android.app.sharelive.presentation.receiver.DiscoverySettingAlarmReceiver.onReceive()` — the expiry broadcast and lazy-boot check are ignored.

This adapter comes from static APK analysis and has not been verified on a Samsung device. Samsung re-obfuscates internal class names between releases. If both scheduler hooks fail, the receiver hook still blocks the expiry reset, but the version should be re-adapted.

### Other vendors investigated

The following vendors are intentionally not hooked because no matching 10-minute "Everyone visibility" mechanism was found:

- Lenovo ZUI 15.0.0811 `com.zui.wifip2p` (Lenovo One) contains no visibility-expiry timer. Its 20/30/60/90-second constants cover connection negotiation and transfer tasks. The "about 5 minutes" note in the ZUI2Share project refers to the receiving device (for example, a ColorOS phone), not to ZUI.
- Moto tank/fogos/lamu firmware dumps contain no standalone vendor sharing APK. `MotoSpaces` only embeds client declarations for Google Nearby Sharing; the actual Quick Share implementation is provided by GMS and is outside a vendor Share adapter.
- Recent Honor devices generally lack a practical Bootloader unlock and LSPosed setup, so support is not recommended.
- Huawei now uses HarmonyOS native applications and is outside the Android/LSPosed ecosystem.

## Compatibility

| Component | Requirement |
| --- | --- |
| Target apps | Mi Share 5.1.12 / ColorOS Share 16.10.70, 17.2.35 / vivo Share 7.4.2.15, 7.4.5.1 / Samsung S Share 13.6.11.10, 13.8.0.47 (static adapter) |
| Target packages | `com.miui.mishare.connectivity` / `com.coloros.oshare`, `com.oplus.oshare` / `com.vivo.easyshare` / `com.samsung.android.app.sharelive` |
| Framework | LSPosed (traditional XposedBridge module format) |
| Android | Android 10 (API 29) or later |
| Module version | 1.9 (versionCode 10) |
| Device verification | Xiaomi and OPPO/ColorOS verified; vivo/iQOO and Samsung not yet verified |

The module relies on internal class and method names from the target sharing apps. Compatibility with other versions is not guaranteed.

## Installation

1. Install the APK.
2. Enable the module in LSPosed Manager.
3. Select the matching scope:
   - MIUI/HyperOS: `com.miui.mishare.connectivity`
   - ColorOS / realme UI: `com.coloros.oshare`
   - OnePlus/OxygenOS (if present): `com.oplus.oshare`
   - vivo/iQOO: `com.vivo.easyshare`
   - Samsung: `com.samsung.android.app.sharelive`
4. Reboot the device.
5. Enable the "Everyone" mode and verify it stays on for more than 10 minutes. vivo and Samsung still need real-device feedback.

The module has no launcher icon. All management is done in LSPosed Manager.

## Building

The project requires JDK 17+ and Android SDK 34+.

```bash
./gradlew :app:assembleRelease
```

Release builds require `keystore.properties` in the project root, or the `SHARENOLIMIT_KEYSTORE`, `SHARENOLIMIT_STORE_PASSWORD`, `SHARENOLIMIT_KEY_ALIAS` and `SHARENOLIMIT_KEY_PASSWORD` environment variables. Example `keystore.properties`:

```properties
storeFile=release.keystore
storePassword=your-store-password
keyAlias=sharenolimit
keyPassword=your-key-password
```

The APK is written to `app/build/outputs/apk/release/app-release.apk`.

## License

[MIT License](LICENSE)

## Disclaimer

This project is a community module and is not affiliated with or endorsed by Xiaomi Inc., Mi Share, OPPO, ColorOS, vivo, iQOO, Samsung, or the LSPosed project. It is intended for learning, research, and personal-device use. Verify that your use complies with applicable laws and relevant terms of service.
