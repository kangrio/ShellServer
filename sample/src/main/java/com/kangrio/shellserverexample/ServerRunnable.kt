package com.kangrio.shellserverexample

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.kangrio.shellserver.shared.BaseShellServerRunnable

@SuppressLint("MissingPermission")
class ServerRunnable : BaseShellServerRunnable() {
    override fun run() {
        if (serverContext == null) return

        val activityManager = serverContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        Log.i("ServerRunnable" ,activityManager.getRunningServices(10).joinToString("\n") { it.process + ":" + it.pid })
    }
}