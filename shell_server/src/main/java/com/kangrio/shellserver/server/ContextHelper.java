package com.kangrio.shellserver.server;


import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.LoadedApk;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.MutableContextWrapper;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressLint({"PrivateApi", "StaticFieldLeak"})
public class ContextHelper extends MutableContextWrapper {
    static String TAG = "ContextHelper";
    private static Context mContext = null;

    private static ActivityThread mActivityThread = null;

    private ContextHelper(Context base) {
        super(base);
    }

    public static Context createAppContext(Context base, String packageName)
            throws Exception {

        Context ctx = base.createPackageContext(
                packageName,
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY
        );

        // Unwrap ContextWrapper chain
        while (ctx instanceof ContextWrapper) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }

        Method createAppContext = ctx.getClass().getDeclaredMethod(
                "createAppContext",
                ActivityThread.class,
                LoadedApk.class
        );
        createAppContext.setAccessible(true);

        ActivityThread at = getActivityThread();
        LoadedApk apk = at.peekPackageInfo(packageName, true);

        return (Context) createAppContext.invoke(null, at, apk);
    }

    public static ActivityThread getActivityThread() {
        if (mActivityThread != null) {
            return mActivityThread;
        }

        mActivityThread = ActivityThread.systemMain();
        return mActivityThread;
    }

    public static List<String> getPackagesForUid(Context ctx, int uid) {
        String[] pkgs = ctx.getPackageManager().getPackagesForUid(uid);
        return pkgs == null ? Collections.emptyList() : Arrays.asList(pkgs);
    }

    public static Context getSystemContext() {
        return getActivityThread().getSystemContext();
    }

    public static Context getProcessContext() {
        if (mContext != null) {
            return mContext;
        }
        Context system = getSystemContext();

        List<String> pkgs = getPackagesForUid(system, Process.myUid());
        Log.d(TAG, "getProcessContext: " + pkgs);

        if (pkgs.isEmpty()) {
            return system;
        }

        if (!pkgs.contains(system.getPackageName()) || Build.VERSION.SDK_INT >= 29) {
            try {
                Context ctx = createAppContext(system, pkgs.get(0));
                mContext = new ContextHelper(ctx);
                return mContext;
            } catch (Exception e) {
                Log.e(TAG, "getProcessContext: ", e);
                return system;
            }
        }

        return system;
    }
}