# MiShareNoLimit

[English](README_EN.md)

一个基于 LSPosed 现代 libxposed API 的模块，用来移除小米互传「所有人」模式的 10 分钟自动关闭限制。

## 原理

小米互传 App（`com.miui.mishare.connectivity`）里的 `T0.b` 类（日志名 `AutoClose`）会在开启「所有人」模式时启动一个 10 分钟倒计时，倒计时结束后自动关闭「所有人可见」。

本模块 hook：

- `T0.b.f()`：跳过倒计时启动逻辑；
- `T0.b.d()`：即使倒计时触发，也不执行自动关闭。

双层防护，让「所有人」模式保持开启，不再自动关闭。

## 兼容性

| 项目 | 要求 |
| --- | --- |
| 目标应用 | 小米互传 5.1.12（`com.miui.mishare.connectivity`） |
| 框架 | 支持 libxposed Modern API 101+ 的官方 LSPosed |
| Android | Android 10（API 29）及以上 |
| 模块版本 | 1.2（versionCode 3） |

模块依赖互传 App 的内部类名和方法签名，不保证兼容其他版本。

## 模块格式

- LSPosed 现代 libxposed 模块格式（`META-INF/xposed/module.prop` + `java_init.list` + `scope.list`）
- 入口类 `MainModule` 继承 `io.github.libxposed.api.XposedModule`
- 依赖 `io.github.libxposed:api:101.0.1`，`minApiVersion` / `targetApiVersion = 101`
- App 名称与描述支持中英文，中文系统显示中文，其他语言显示英文

## 使用

1. 从 GitHub Releases 下载并安装 APK
2. 在 LSPosed 管理器中启用本模块
3. 作用域勾选 `com.miui.mishare.connectivity`
4. 重启手机
5. 打开小米互传「所有人」模式，等待超过 10 分钟验证不再自动关闭

> 已实测：开启「所有人」超过 10 分钟不自动关闭。

## 构建

需要 JDK 17+ 和 Android SDK 34+。

```bash
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

## 目录

- `app/src/main/java/com/example/mishare_nolimit/MainModule.java` —— libxposed 入口与 hook
- `app/src/main/java/com/example/mishare_nolimit/MainActivity.java` —— 模块 App 主页
- `app/src/main/resources/META-INF/xposed/` —— 现代 LSPosed 模块声明
- `app/src/main/res/values/`、`values-zh-rCN/` —— 中英文资源

## 许可证

[MIT License](LICENSE)

## 免责声明

本项目是社区模块，与小米公司、小米互传或 LSPosed 项目无隶属或认可关系，仅供学习、研究和个人设备使用。使用前请确认符合当地法律及相关服务条款。
