package com.kangrio.shellserver.server

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.kangrio.shellserver.Constants

// The shell server
internal class ShellServer {
    private lateinit var binder: ShellServerImpl
    private var hostPackageName = ""

    private fun sendBinder(receiverPackage: String?) {
        val context = ContextHelper.getProcessContext()
        val intent = Intent(Constants.ACTION_RECEIVE_BINDER).apply {
            `package` = receiverPackage
            putExtras(Bundle().apply {
                putBinder("binder", binder)
            })
        }

        context.sendBroadcast(intent)
    }

    private fun run() {
        hostPackageName = if (args.isNotEmpty()) args[0] else error("No receiver package")
        if (!::binder.isInitialized) {
            binder = ShellServerImpl(ContextHelper.getProcessContext(), hostPackageName)
        }

        registerService("shellserver_$hostPackageName")
        sendBinder(hostPackageName)
    }

    @SuppressLint("PrivateApi")
    private fun registerService(service: String) {
        try {
            val sm = Class.forName("android.os.ServiceManager")

            val addService = sm.getDeclaredMethod(
                "addService",
                String::class.java,
                IBinder::class.java
            )
            addService.isAccessible = true
            addService.invoke(null, service, binder)
        } catch (e: Throwable) {
            Log.e("ShellServer", "registerService: ", e)
        }
    }

    companion object {
        var args = arrayOf<String>()

        @JvmStatic
        fun main(args: Array<String>) {
            Log.i("ShellServer", "Starting ShellServer")
            this.args = args
            try {
                if (Looper.getMainLooper() == null) {
                    Looper.prepareMainLooper()
                }
                ShellServer().also {
                    it.run()
                }
                Looper.loop()
            } catch (e: Exception) {
                Log.e("ShellServer", "crashed", e)
            }
        }
    }
}