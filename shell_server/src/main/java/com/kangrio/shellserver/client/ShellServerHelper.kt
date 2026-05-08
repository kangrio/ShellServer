package com.kangrio.shellserver.client

import android.content.Context
import android.os.IBinder
import com.kangrio.shellserver.client.utils.ServerUtil
import com.kangrio.shellserver.client.utils.ServerUtil.getRemote
import com.kangrio.shellserver.shared.ShellServerRunnable

class ShellServerHelper {
    companion object {
        fun init(
            context: Context,
            killExisting: Boolean = true,
            onServerStarted: ((IBinder?) -> Unit)
        ) {
            ServerUtil.init(context, killExisting, onServerStarted)
        }

        fun exec(cmd: String): String? {
            return getRemote()?.exec(cmd)
        }

        fun runOnce(runnable: ShellServerRunnable, initialDelay: Long = 0): Int {
            return getRemote()?.runOnce(runnable.javaClass.name, initialDelay) ?: -1
        }

        fun schedule(
            runnable: ShellServerRunnable, initialDelay: Long = 0, period: Long,
        ): Int {
            return getRemote()?.schedule(runnable.javaClass.name, initialDelay, period) ?: -1
        }

        fun cancel(id: Int) {
            getRemote()?.cancel(id)
        }
    }
}