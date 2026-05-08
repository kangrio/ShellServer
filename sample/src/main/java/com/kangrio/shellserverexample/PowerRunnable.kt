package com.kangrio.shellserverexample

import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import com.kangrio.shellserver.shared.BaseShellServerRunnable

class PowerRunnable(private val action: Action) : BaseShellServerRunnable() {

    enum class Action { SLEEP, WAKE }

    // No-arg constructor for ShellServer to instantiate
    constructor() : this(Action.SLEEP)

    override fun run() {
        val context = serverContext ?: return
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        try {
            when (action) {
                Action.SLEEP -> {
                    Log.i("PowerRunnable", "Going to sleep...")
                    // goToSleep is a hidden/restricted API, but we have shell context
                    val method = powerManager.javaClass.getMethod("goToSleep", Long::class.java)
                    method.invoke(powerManager, SystemClock.uptimeMillis())
                }
                Action.WAKE -> {
                    Log.i("PowerRunnable", "Waking up...")
                    // wakeUp is also restricted
                    val method = powerManager.javaClass.getMethod("wakeUp", Long::class.java)
                    method.invoke(powerManager, SystemClock.uptimeMillis())
                }
            }
        } catch (e: Exception) {
            Log.e("PowerRunnable", "Failed to toggle power state", e)
        }
    }
}
