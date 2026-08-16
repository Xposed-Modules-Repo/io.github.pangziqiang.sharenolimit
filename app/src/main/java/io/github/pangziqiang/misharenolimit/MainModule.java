package io.github.pangziqiang.misharenolimit;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

public class MainModule extends XposedModule {

    private static final String TARGET_PACKAGE = "com.miui.mishare.connectivity";
    private static final String AUTO_CLOSE_CLASS = "T0.b";
    private static final String TAG = "MiShareNoLimit";

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        if (!TARGET_PACKAGE.equals(param.getPackageName())) {
            return;
        }

        try {
            ClassLoader cl = param.getDefaultClassLoader();
            Class<?> clazz = cl.loadClass(AUTO_CLOSE_CLASS);

            Method f = clazz.getDeclaredMethod("f");
            f.setAccessible(true);
            hook(f).intercept(new SkipHooker("f"));
            android.util.Log.i(TAG, "hooked AutoClose.f()");

            Method d = clazz.getDeclaredMethod("d");
            d.setAccessible(true);
            hook(d).intercept(new SkipHooker("d"));
            android.util.Log.i(TAG, "hooked AutoClose.d()");
        } catch (Throwable t) {
            android.util.Log.e(TAG, "hook failed: " + t, t);
        }
    }

    private static final class SkipHooker implements XposedInterface.Hooker {
        private final String methodName;

        SkipHooker(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public Object intercept(XposedInterface.Chain chain) throws Throwable {
            android.util.Log.i(TAG, "skip AutoClose." + methodName + "()");
            return null;
        }
    }
}
