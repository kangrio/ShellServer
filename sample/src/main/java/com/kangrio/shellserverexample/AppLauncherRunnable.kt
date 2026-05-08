package com.kangrio.shellserverexample

import android.content.Intent
import android.util.Log
import com.kangrio.shellserver.shared.BaseShellServerRunnable

class AppLauncherRunnable(private val packageName: String = "com.android.settings") : BaseShellServerRunnable() {
    override fun run() {
        val context = serverContext ?: return
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                Log.i("AppLauncherRunnable", "Attempting to launch $packageName with ZERO flags")
                
                val amClass = Class.forName("android.app.ActivityManager")
                val iAm = amClass.getMethod("getService").invoke(null)
                
                val methods = iAm.javaClass.methods
                val startActivity = methods.find { 
                    it.name == "startActivity" && it.parameterTypes.any { type -> type == Intent::class.java }
                }

                if (startActivity != null) {
                    val args = arrayOfNulls<Any>(startActivity.parameterCount)
                    for (i in 0 until startActivity.parameterCount) {
                        val type = startActivity.parameterTypes[i]
                        when {
                            type == Intent::class.java -> args[i] = intent
                            type == String::class.java -> if (i == 1) args[i] = "com.android.shell" else args[i] = null
                            type == Int::class.javaPrimitiveType -> args[i] = 0
                            else -> args[i] = null
                        }
                    }
                    startActivity.invoke(iAm, *args)
                    Log.i("AppLauncherRunnable", "Successfully invoked IActivityManager.startActivity")
                } else {
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("AppLauncherRunnable", "Error: ${e.message}")
        }
    }
}
