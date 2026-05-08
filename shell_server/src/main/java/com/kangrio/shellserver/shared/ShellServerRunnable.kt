package com.kangrio.shellserver.shared

import java.io.Serializable

interface ShellServerRunnable : Serializable {
    fun run()
}