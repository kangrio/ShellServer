package com.kangrio.shellserverexample

import android.content.Context
import android.os.BatteryManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.kangrio.shellserver.shared.BaseShellServerRunnable

class DiagnosticsRunnable(private val type: Type) : BaseShellServerRunnable() {
    enum class Type { BATTERY, DISPLAY }

    // No-arg constructor for ShellServer
    constructor() : this(Type.BATTERY)

    override fun run() {
        val context = serverContext ?: return
        try {
            when (type) {
                Type.BATTERY -> {
                    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                    val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    val status = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
                    Log.i("DiagnosticsRunnable", "Battery Level: $level%, Status: $status")
                }
                Type.DISPLAY -> {
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    val metrics = DisplayMetrics()
                    // Using defaultDisplay.getRealMetrics to get actual resolution
                    wm.defaultDisplay.getRealMetrics(metrics)
                    Log.i("DiagnosticsRunnable", "Display: ${metrics.widthPixels}x${metrics.heightPixels}, Density: ${metrics.densityDpi}dpi")
                }
            }
        } catch (e: Exception) {
            Log.e("DiagnosticsRunnable", "Failed to fetch diagnostics: ${type.name}", e)
        }
    }
}
