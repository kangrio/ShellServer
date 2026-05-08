package com.kangrio.shellserver.shared

import android.content.Context

open class BaseShellServerRunnable : ShellServerRunnable {
    var serverContext: Context? = null
    override fun run() {
    }
}