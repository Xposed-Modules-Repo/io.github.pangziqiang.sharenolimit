package io.github.pangziqiang.misharenolimit;

import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private static final String MI_SHARE_PACKAGE = "com.miui.mishare.connectivity";
    private static final String COLOR_OS_SHARE_PACKAGE = "com.coloros.oshare";
    private static final String OPLUS_SHARE_PACKAGE = "com.oplus.oshare";
    private static final String VIVO_SHARE_PACKAGE = "com.vivo.easyshare";
    private static final String SAMSUNG_SHARE_PACKAGE = "com.samsung.android.app.sharelive";
    private static final String COLOR_OS_SWITCH_TIMEOUT = "oplus.action.oshare.switch.timeout";
    private static final String SAMSUNG_UPDATE_DISCOVERY_ALARM =
            "com.samsung.android.app.sharelive.UPDATE_DISCOVERY_ALARM";
    private static final String SAMSUNG_LAZY_BOOT_COMPLETE =
            "com.samsung.intent.action.LAZY_BOOT_COMPLETE";
    private static final String TAG = "[ShareNoLimit] ";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (MI_SHARE_PACKAGE.equals(lpparam.packageName)) {
            hookMiShare(lpparam.classLoader);
        } else if (COLOR_OS_SHARE_PACKAGE.equals(lpparam.packageName)
                || OPLUS_SHARE_PACKAGE.equals(lpparam.packageName)) {
            hookColorOsShare(lpparam.classLoader);
        } else if (VIVO_SHARE_PACKAGE.equals(lpparam.packageName)) {
            hookVivoShare(lpparam.classLoader);
        } else if (SAMSUNG_SHARE_PACKAGE.equals(lpparam.packageName)) {
            hookSamsungShare(lpparam.classLoader);
        }
    }

    private static void hookMiShare(ClassLoader classLoader) {
        Class<?> clazz;
        try {
            clazz = XposedHelpers.findClass("T0.b", classLoader);
        } catch (Throwable t) {
            XposedBridge.log(TAG + "MiShare AutoClose class not found: " + t);
            return;
        }

        hookMiAutoCloseMethod(clazz, "f");
        hookMiAutoCloseMethod(clazz, "d");
    }

    private static void hookMiAutoCloseMethod(Class<?> clazz, String methodName) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedBridge.log(TAG + "skip MiShare AutoClose." + methodName + "()");
                    param.setResult(null);
                }
            });
            XposedBridge.log(TAG + "hooked MiShare AutoClose." + methodName + "()");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "MiShare AutoClose." + methodName
                    + "() hook failed: " + t);
        }
    }

    private static void hookColorOsShare(ClassLoader classLoader) {
        hookColorOsConfig(classLoader);
        hookTimeoutManager(classLoader);
        hookLastTurnOnTime(classLoader);
        hookTimeoutReceiver(classLoader);
    }

    private static void hookColorOsConfig(ClassLoader classLoader) {
        hookLongMethodReturning(classLoader, "jb.b", "l", -1L);
        hookLongMethodReturning(classLoader, "wa.b", "l", -1L);
    }

    private static void hookTimeoutManager(ClassLoader classLoader) {
        hookSkipMethodIfExists(classLoader, "v9.e", "d");
        hookSkipMethodIfExists(classLoader, "v9.e", "e");
        hookSkipMethodIfExists(classLoader, "y8.f", "d");
        hookSkipMethodIfExists(classLoader, "y8.f", "e");
    }

    private static void hookLastTurnOnTime(ClassLoader classLoader) {
        hookLongMethodReturning(classLoader, "com.oplus.oshare.utils.s1", "g", 0L);
        hookLongMethodReturning(classLoader, "lb.s1", "g", 0L);
    }

    private static void hookVivoShare(ClassLoader classLoader) {
        boolean hooked = hookVivoScheduler(classLoader, "ic.m");
        hooked |= hookVivoScheduler(classLoader, "hc.m");
        if (!hooked) {
            XposedBridge.log(TAG + "vivo ShakeDevicesConnectManager not found; unsupported version");
        }
    }

    private static boolean hookVivoScheduler(ClassLoader classLoader, String className) {
        try {
            Class<?> clazz = XposedHelpers.findClass(className, classLoader);
            XposedHelpers.findAndHookMethod(clazz, "I", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Boolean.TRUE.equals(param.args[0])) {
                        XposedBridge.log(TAG + "skip vivo auto-disband timers");
                        param.setResult(null);
                    }
                }
            });
            try {
                XposedHelpers.findAndHookMethod(clazz, "S", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(false);
                    }
                });
            } catch (Throwable ignored) {
            }
            XposedBridge.log(TAG + "hooked vivo " + className + ".I(boolean)");
            return true;
        } catch (Throwable t) {
            XposedBridge.log(TAG + "vivo " + className + " hook failed: " + t);
            return false;
        }
    }

    private static void hookSamsungShare(ClassLoader classLoader) {
        hookSamsungDiscoveryScheduler(classLoader, "oc.o5", "n");
        hookSamsungDiscoveryScheduler(classLoader, "b7.F2", "o");
        hookSamsungDiscoveryAlarmReceiver(classLoader);
    }

    private static void hookSamsungDiscoveryScheduler(
            ClassLoader classLoader, String className, String methodName) {
        try {
            Class<?> clazz = XposedHelpers.findClass(className, classLoader);
            XposedHelpers.findAndHookMethod(clazz, methodName, Long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedBridge.log(TAG + "skip Samsung discovery alarm "
                            + className + "." + methodName + "(Long)");
                    param.setResult(null);
                }
            });
            XposedBridge.log(TAG + "hooked Samsung " + className + "." + methodName + "(Long)");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "Samsung " + className + "." + methodName
                    + "(Long) hook failed: " + t);
        }
    }

    private static void hookSamsungDiscoveryAlarmReceiver(ClassLoader classLoader) {
        try {
            Class<?> clazz = XposedHelpers.findClass(
                    "com.samsung.android.app.sharelive.presentation.receiver."
                            + "DiscoverySettingAlarmReceiver",
                    classLoader);
            XposedHelpers.findAndHookMethod(clazz, "onReceive", Context.class, Intent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Intent intent = (Intent) param.args[1];
                            String action = intent == null ? null : intent.getAction();
                            if (SAMSUNG_UPDATE_DISCOVERY_ALARM.equals(action)
                                    || SAMSUNG_LAZY_BOOT_COMPLETE.equals(action)) {
                                XposedBridge.log(TAG + "skip Samsung discovery receiver: " + action);
                                param.setResult(null);
                            }
                        }
                    });
            XposedBridge.log(TAG + "hooked Samsung DiscoverySettingAlarmReceiver");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "Samsung DiscoverySettingAlarmReceiver hook failed: " + t);
        }
    }

    private static void hookTimeoutReceiver(ClassLoader classLoader) {
        try {
            Class<?> clazz = XposedHelpers.findClass("com.oplus.oshare.device.TimeoutReceiver", classLoader);
            XposedHelpers.findAndHookMethod(clazz, "onReceive", Context.class, Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Intent intent = (Intent) param.args[1];
                    if (intent != null && COLOR_OS_SWITCH_TIMEOUT.equals(intent.getAction())) {
                        XposedBridge.log(TAG + "skip ColorOS switch timeout broadcast");
                        param.setResult(null);
                    }
                }
            });
            XposedBridge.log(TAG + "hooked ColorOS TimeoutReceiver");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "ColorOS TimeoutReceiver hook failed: " + t);
        }
    }

    private static void hookSkipMethodIfExists(ClassLoader classLoader, String className, String methodName) {
        try {
            Class<?> clazz = XposedHelpers.findClass(className, classLoader);
            XposedHelpers.findAndHookMethod(clazz, methodName, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedBridge.log(TAG + "skip ColorOS " + clazz.getSimpleName() + "." + methodName + "()");
                    param.setResult(null);
                }
            });
            XposedBridge.log(TAG + "hooked ColorOS " + className + "." + methodName + "()");
        } catch (Throwable t) {
            XposedBridge.log(TAG + "ColorOS " + className + "." + methodName + " hook failed: " + t);
        }
    }

    private static void hookLongMethodReturning(
            ClassLoader classLoader, String className, String methodName, long result) {
        try {
            Class<?> clazz = XposedHelpers.findClass(className, classLoader);
            Method method = XposedHelpers.findMethodExact(clazz, methodName, Context.class);
            if (method.getReturnType() != long.class) {
                XposedBridge.log(TAG + "skip " + className + "." + methodName
                        + "(): return type is " + method.getReturnType().getName());
                return;
            }
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult(result);
                }
            });
            XposedBridge.log(TAG + "hooked " + className + "." + methodName + "() -> " + result);
        } catch (Throwable t) {
            XposedBridge.log(TAG + className + "." + methodName + " hook failed: " + t);
        }
    }
}
