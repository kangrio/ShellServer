package com.kangrio.shellserverexample

import android.hardware.input.InputManager
import android.os.SystemClock
import android.util.Log
import android.view.InputEvent
import android.view.KeyEvent
import com.kangrio.shellserver.shared.BaseShellServerRunnable

class NavigationRunnable(private val keyCode: Int) : BaseShellServerRunnable() {

    // No-arg constructor for ShellServer
    constructor() : this(KeyEvent.KEYCODE_HOME)

    override fun run() {
        val context = serverContext ?: return
        try {
            val im = context.getSystemService(InputManager::class.java)
            
            // We use reflection to access the hidden injectInputEvent method
            val injectMethod = im.javaClass.getMethod(
                "injectInputEvent",
                InputEvent::class.java,
                Int::class.java
            )

            val now = SystemClock.uptimeMillis()
            
            // Inject Down
            val downEvent = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0)
            injectMethod.invoke(im, downEvent, 0) // 0 = INJECT_INPUT_EVENT_MODE_ASYNC
            
            // Inject Up
            val upEvent = KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0)
            injectMethod.invoke(im, upEvent, 0)

            Log.i("NavigationRunnable", "Injected KeyEvent: $keyCode")
        } catch (e: Exception) {
            Log.e("NavigationRunnable", "Failed to inject navigation event", e)
        }
    }
}
