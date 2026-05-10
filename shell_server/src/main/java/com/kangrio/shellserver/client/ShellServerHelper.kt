package com.kangrio.shellserver.client

import android.content.Context
import android.os.IBinder
import android.os.IInterface
import com.kangrio.shellserver.client.utils.ServerUtil
import com.kangrio.shellserver.client.utils.ServerUtil.getRemote
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
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

        fun getSystemServiceInterface(
            serviceName: String
        ): IInterface? {
            return ServerUtil.getSystemServiceInterface(serviceName)
        }

        fun exec(cmd: String): ShellResponse {
            val result = getRemote()?.exec(cmd)
            val response = ShellResponse(
                result?.getString("output", "") ?: "",
                result?.getString("error", "") ?: "",
                result?.getInt("exitCode", -1) ?: -1
            )
            return response
        }

        private fun serialize(obj: Any): ByteArray? {
            return try {
                val baos = ByteArrayOutputStream()
                val oos = ObjectOutputStream(baos)
                oos.writeObject(obj)
                oos.flush()
                baos.toByteArray()
            } catch (e: Exception) {
                android.util.Log.e("ShellServerHelper", "Serialization failed for ${obj.javaClass.name}. " +
                        "Ensure all member variables (except @Transient ones) are Serializable. " +
                        "Avoid passing Context, Activity, or View objects.", e)
                null
            }
        }

        fun runOnce(runnable: ShellServerRunnable, initialDelay: Long = 0): Int {
            val data = serialize(runnable) ?: return -1
            return getRemote()?.runOnce(data, initialDelay) ?: -1
        }

        fun schedule(
            runnable: ShellServerRunnable, initialDelay: Long = 0, period: Long,
        ): Int {
            val data = serialize(runnable) ?: return -1
            return getRemote()?.schedule(data, initialDelay, period) ?: -1
        }

        fun cancel(id: Int) {
            getRemote()?.cancel(id)
        }
    }
}

class ShellResponse(
    val output: String,
    val errorOutput: String,
    val exitCode: Int
)