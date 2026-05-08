package com.kangrio.shellserver.shared

import android.content.Context

open class BaseShellServerRunnable : ShellServerRunnable {
    @Transient
    var serverContext: Context? = null
    override fun run() {
    }
}