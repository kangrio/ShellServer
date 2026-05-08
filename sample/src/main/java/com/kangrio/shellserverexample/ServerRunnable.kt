package com.kangrio.shellserverexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.kangrio.shellserver.shared.BaseShellServerRunnable

@SuppressLint("MissingPermission")
class ServerRunnable : BaseShellServerRunnable() {
    override fun run() {
        val context = serverContext ?: return
        
        Log.i("ServerRunnable", "Running in shell process context!")
        
        // Example: List some installed packages using system context
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
            Log.i("ServerRunnable", "Total packages found: ${packages.size}")
            
            // Log first 5 packages
            packages.take(5).forEach {
                Log.i("ServerRunnable", "Package: ${it.packageName}")
            }
        } catch (e: Exception) {
            Log.e("ServerRunnable", "Error accessing package manager", e)
        }
    }
}
