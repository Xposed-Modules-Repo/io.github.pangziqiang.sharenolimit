# ShareNoLimit

[English](README_EN.md)

一个基于 LSPosed 传统 XposedBridge API 的模块，用来移除系统互传 App 的 10 分钟自动关闭/群组自动解散限制。

## 项目演进与验证状态

项目最初只适配小米互传「所有人」模式下的 10 分钟自动关闭限制，之后扩展到 OPPO/ColorOS、vivo/iQOO 和三星互传。由于目前只有小米和 OPPO 设备，小米与 OPPO/ColorOS 已完成真机验证；vivo/iQOO 和三星仍基于 APK 静态分析进行适配，尚未进行真机验证，相关功能可能因系统版本或互传 App 版本不同而失效。

遇到问题时，请附上设备型号、系统版本、互传 App 版本以及 LSPosed 日志。

> 1.9 起模块包名由 `io.github.pangziqiang.misharenolimit` 变更为 `io.github.pangziqiang.sharenolimit`。1.10 起改用独立正式证书签名，与更早版本不兼容覆盖安装。升级前请先在 LSPosed 管理器中停用并卸载旧模块，再安装新版并重新勾选作用域。

当前支持：

- 小米互传 `com.miui.mishare.connectivity`
- ColorOS / realme UI 互传 `com.coloros.oshare`，以及部分一加/OxygenOS 的 `com.oplus.oshare`
- vivo / iQOO 互传 `com.vivo.easyshare`
- 三星 S 互传 / Quick Share `com.samsung.android.app.sharelive`

## 原理

### 小米互传

小米互传里的 `T0.b` 类（日志名 `AutoClose`）会在开启「所有人」模式时启动 10 分钟倒计时。本模块 hook：

- `T0.b.f()`：跳过倒计时启动逻辑；
- `T0.b.d()`：即使倒计时触发，也不执行自动关闭。

### ColorOS / realme / 一加互传

ColorOS 互传 17.2.35 中，互传接收状态保存在 `oshare_state`：

- `0`：接收关闭
- `1`：所有人（10 分钟）
- `2`：仅限联系人

默认时长来自 `assets/apps_oshare_feature_config.xml` 的 `SwitchTimeOut=10`。开启「所有人」后，TimeoutManager 会设置到期闹钟，到期广播为 `oplus.action.oshare.switch.timeout`，由 `TimeoutReceiver` 切换到联系人模式或关闭接收。

本模块按版本 hook：

- 配置读取 `jb.b.l()`（17.x）和 `wa.b.l()`（16.x）：返回 `-1`，不设置到期闹钟，TimeoutReceiver 也会把到期广播当作「永不关闭」；
- TimeoutManager `v9.e.d()` / `v9.e.e()`（17.x）和 `y8.f.d()` / `y8.f.e()`（16.x）：不设置切换闹钟，也不写入新的到期时间；
- 上次开启时间 `com.oplus.oshare.utils.s1.g()`（17.x）和 `lb.s1.g()`（16.x）：返回 `0`，避免设置页和下拉面板的倒计时 UI 自行触发切换；
- `com.oplus.oshare.device.TimeoutReceiver.onReceive()`：忽略 `oplus.action.oshare.switch.timeout` 到期广播。

多层防护让「所有人」模式保持开启。该适配已在 OPPO/ColorOS 设备上完成真机验证。

### vivo / iQOO 互传

vivo 互传的机制与小米/OPPO 不同。它没有「所有人可见性 10 分钟后自动切到联系人」这条逻辑，而是「摇一摇群组空闲后自动解散」：

- 默认 `auto_disband_duration=600000`，即 10 分钟；
- 设置页可改为 30 分钟、1 小时或 3 小时；
- `ShakeDevicesConnectManager` 在群组建立后调用 `I(true)`，提前 1 分钟弹出提醒，并在到期时解散群组。

本模块 hook `ShakeDevicesConnectManager.I(boolean)`：

- 当参数为 `true` 时跳过两个延时解散任务；
- 当参数为 `false` 时仍执行原逻辑，保证退出群组或关闭功能时能正常清理计时器；
- `S()` 返回 `false`，避免状态栏/设置页继续显示即将解散的倒计时。

已适配的类名：vivo 互传 7.4.5.1 的 `ic.m`，以及 7.4.2.15 的 `hc.m`。该适配来自 APK 静态分析，尚未在 vivo/iQOO 真机验证。vivo 每次大版本更新都可能重新混淆类名；若日志提示 `ShakeDevicesConnectManager not found`，需要重新适配。

### 三星 S 互传 / Quick Share

三星中国版 S 互传在「所有人」模式下会通过 `AlarmManager` 安排约 10 分钟后的到期闹钟，广播为 `com.samsung.android.app.sharelive.UPDATE_DISCOVERY_ALARM`，由 `DiscoverySettingAlarmReceiver` 触发可见性刷新。重启后同一接收器还会处理 `LAZY_BOOT_COMPLETE`，用于恢复或立即执行到期检查。

本模块同时 hook：

- 13.6.11.10 的 `oc.o5.n(Long)`，以及 13.8.0.47 的 `b7.F2.o(Long)`：不写入新的到期闹钟；
- `com.samsung.android.app.sharelive.presentation.receiver.DiscoverySettingAlarmReceiver.onReceive()`：忽略到期广播和延迟启动检查。

该适配来自固件 APK 静态分析，尚未在三星真机验证。三星不同系统版本会重新混淆内部类名；若日志提示 `oc.o5.n(Long)` 和 `b7.F2.o(Long)` 均未找到，接收器 hook 仍可阻止到期重置，但建议重新适配调度器。

## 兼容性

| 项目 | 要求 |
| --- | --- |
| 目标应用 | 小米互传 5.1.12 / ColorOS 互传 16.10.70、17.2.35 / vivo 互传 7.4.2.15、7.4.5.1 / 三星 S 互传 13.6.11.10、13.8.0.47（静态适配） |
| 目标包名 | `com.miui.mishare.connectivity` / `com.coloros.oshare`、`com.oplus.oshare` / `com.vivo.easyshare` / `com.samsung.android.app.sharelive` |
| 框架 | LSPosed（传统 XposedBridge 模块格式） |
| Android | Android 10（API 29）及以上 |
| 模块版本 | 1.10（versionCode 11） |
| 真机验证 | 小米、OPPO/ColorOS 已验证；vivo/iQOO、三星尚未验证 |

模块依赖互传 App 的内部类名和方法签名，不保证兼容其他版本。

## 模块格式

- LSPosed 传统 XposedBridge 模块格式（`assets/xposed_init` + manifest metadata）
- 入口类 `HookMain` 实现 `IXposedHookLoadPackage`
- 依赖 `de.robv.android.xposed:api:82`
- App 名称与描述支持中英文

## 使用

1. 从 [GitHub Releases](https://github.com/pangziqiang/io.github.pangziqiang.sharenolimit/releases) 下载并安装 APK
2. 在 LSPosed 管理器中启用本模块
3. 按设备系统勾选作用域：
   - 小米/澎湃系统：`com.miui.mishare.connectivity`
   - ColorOS / realme UI：`com.coloros.oshare`
   - 一加/OxygenOS（如使用）：`com.oplus.oshare`
   - vivo/iQOO：`com.vivo.easyshare`
   - 三星：`com.samsung.android.app.sharelive`
4. 重启手机
5. 小米/OPPO/三星打开「所有人」模式，vivo 创建摇一摇群组，等待超过 10 分钟验证不会自动关闭/解散；其中 vivo 和三星仍需更多真机反馈

模块没有桌面图标，所有操作都在 LSPosed 管理器中完成。

## 构建

需要 JDK 17+ 和 Android SDK 34+。

```bash
./gradlew :app:assembleRelease
```

正式签名使用的 `release.keystore` 与 `keystore.properties` 均已被 `.gitignore` 忽略，不会提交到仓库。构建时需要在项目根目录提供签名信息，或通过 `SHARENOLIMIT_KEYSTORE`、`SHARENOLIMIT_STORE_PASSWORD`、`SHARENOLIMIT_KEY_ALIAS`、`SHARENOLIMIT_KEY_PASSWORD` 环境变量传入。`keystore.properties` 示例：

```properties
storeFile=release.keystore
storePassword=your-store-password
keyAlias=sharenolimit
keyPassword=your-key-password
```

产物：`app/build/outputs/apk/release/app-release.apk`

APK 不提交到源码仓库，仅通过 GitHub Releases 分发。

## 目录

- `app/src/main/java/io/github/pangziqiang/sharenolimit/HookMain.java` —— XposedBridge 入口与 hook
- `app/src/main/assets/xposed_init` —— LSPosed 传统模块入口声明
- `app/src/main/res/values/`、`values-zh-rCN/` —— 中英文资源

## 许可证

[MIT License](LICENSE)

## 免责声明

本项目是社区模块，与小米公司、小米互传、OPPO、ColorOS、vivo、iQOO、三星或 LSPosed 项目无隶属或认可关系，仅供学习、研究和个人设备使用。使用前请确认符合当地法律及相关服务条款。
