# MiShareNoLimit

一个 LSPosed 模块，用来去掉小米互传「所有人」模式的 10 分钟自动关闭限制。

## 原理

小米互传 App（`com.miui.mishare.connectivity`）里的 `T0.b` 类（日志名 `AutoClose`）会在开启「所有人」模式时启动一个 10 分钟倒计时，倒计时结束后自动关闭「所有人可见」。

本模块 hook：

- `T0.b.f()`：跳过倒计时启动逻辑；
- `T0.b.d()`：即使倒计时触发，也不执行自动关闭。

双层防护，让「所有人」模式保持开启，不再自动关闭。

## 模块格式

- 使用 LSPosed 现代 libxposed 模块格式（`META-INF/xposed/module.prop` + `java_init.list` + `scope.list`）
- 入口类 `MainModule` 继承 `io.github.libxposed.api.XposedModule`
- 使用 `io.github.libxposed:api:101.0.1`，`minApiVersion` / `targetApiVersion = 101`

## 使用

1. 安装 [MiShareNoLimit_v1.2.apk](apk/MiShareNoLimit_v1.2.apk)
2. 在 LSPosed 管理器中启用本模块
3. 作用域勾选 `com.miui.mishare.connectivity`
4. 重启手机
5. 打开小米互传「所有人」模式，等待超过 10 分钟验证不再自动关闭

> 已实测：开启「所有人」超过 10 分钟不自动关闭。

## 构建

```bash
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

## 目录

- `app/src/main/java/com/example/mishare_nolimit/MainModule.java` —— libxposed 入口与 hook
- `app/src/main/java/com/example/mishare_nolimit/MainActivity.java` —— 模块 App 主页
- `app/src/main/resources/META-INF/xposed/` —— 现代 LSPosed 模块声明
- `app/src/main/res/values/`、`values-zh-rCN/` —— 中英文资源

## 说明

这是针对当前 HyperOS 小米互传 APK 的反编译结果写的 hook，如果小米后续更新互传 App，类名/方法名可能变化，需要重新适配。
