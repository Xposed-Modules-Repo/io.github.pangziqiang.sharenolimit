package com.example.mishare_nolimit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.miui.mishare.connectivity";
    private static final String AUTO_CLOSE_CLASS = "T0.b";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }

        try {
            Class<?> clazz = XposedHelpers.findClass(AUTO_CLOSE_CLASS, lpparam.classLoader);

            XposedHelpers.findAndHookMethod(clazz, "f", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // f() starts the 10-minute auto-close timer when "everyone" mode is active.
                    // Skip it so the timer never runs; everyone mode stays on indefinitely.
                    XposedBridge.log("[MiShareNoLimit] skip AutoClose.f()");
                    param.setResult(null);
                }
            });
            XposedBridge.log("[MiShareNoLimit] hooked AutoClose.f()");

            XposedHelpers.findAndHookMethod(clazz, "d", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // d() is the timer-expired callback that stops "everyone" mode.
                    // No-op it as a second layer: even if the timer somehow starts, it won't close.
                    XposedBridge.log("[MiShareNoLimit] skip AutoClose.d()");
                    param.setResult(null);
                }
            });
            XposedBridge.log("[MiShareNoLimit] hooked AutoClose.d()");
        } catch (Throwable t) {
            XposedBridge.log("[MiShareNoLimit] hook failed: " + t);
        }
    }
}
