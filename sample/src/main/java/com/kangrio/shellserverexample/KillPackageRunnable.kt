package com.kangrio.shellserverexample

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.kangrio.shellserver.shared.BaseShellServerRunnable

class KillPackageRunnable(private val packageName: String) : BaseShellServerRunnable() {

    // No-arg constructor for ShellServer to instantiate
    constructor() : this("com.android.settings")

    override fun run() {
        val context = serverContext ?: return
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        
        try {
            Log.i("KillPackageRunnable", "Force stopping: $packageName")
            // forceStopPackage is a hidden/restricted API
            val method = am.javaClass.getMethod("forceStopPackage", String::class.java)
            method.invoke(am, packageName)
        } catch (e: Exception) {
            Log.e("KillPackageRunnable", "Failed to kill package: $packageName", e)
        }
    }
}
