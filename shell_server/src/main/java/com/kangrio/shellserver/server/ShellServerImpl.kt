package com.kangrio.shellserver.server


import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Process
import android.util.Log
import com.kangrio.shellserver.Constants
import com.kangrio.shellserver.IShellServer
import com.kangrio.shellserver.shared.BaseShellServerRunnable
import com.kangrio.shellserver.shared.ShellServerRunnable
import dalvik.system.DexClassLoader
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ShellServerImpl(private val mContext: Context, hostPackageName: String) : IShellServer.Stub() {
    private val permission = "$hostPackageName.permission.SHELL_SERVER"
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val nextId = AtomicInteger()
    private val tasks = ConcurrentHashMap<Int, ScheduledFuture<*>>()

    private val loaders = ConcurrentHashMap<String, DexClassLoader>()

    override fun invodeSystemService() {
    }

    private fun getClassLoader(): ClassLoader {
        val uid = getCallingUid()
        val pkg = mContext.packageManager
            .getPackagesForUid(uid)
            ?.firstOrNull()
            ?: error("No package")

        val apkPath = mContext.packageManager
            .getApplicationInfo(pkg, 0)
            .sourceDir

        return loaders.getOrPut(apkPath) {
            DexClassLoader(
                apkPath,
                mContext.codeCacheDir.absolutePath,
                null,
                BaseShellServerRunnable::class.java.classLoader
            )
        }
    }

    private fun deserialize(data: ByteArray): ShellServerRunnable {
        val bais = ByteArrayInputStream(data)
        val loader = javaClass.classLoader
        val ois = object : ObjectInputStream(bais) {
            override fun resolveClass(desc: ObjectStreamClass): Class<*> {
                return try {
                    Class.forName(desc.name, false, loader)
                } catch (e: ClassNotFoundException) {
                    Log.e("ShellServer", "ClassNotFoundException: ${e.message}")
                    super.resolveClass(desc)
                }
            }
        }
        val runnable = ois.readObject() as BaseShellServerRunnable
        runnable.serverContext = mContext
        return runnable
    }

    private fun enforcePermission() {
        if (getCallingUid() == Process.myUid()) return
        mContext.enforceCallingPermission(permission, "Missing permission: $permission")
    }

    override fun exec(cmd: String): Bundle {
        Log.i("ShellServer", "exec: $cmd")

        return try {
            val process = Runtime.getRuntime().exec(cmd)

            var output = ""
            var error = ""

            val outThread = Thread {
                output = process.inputStream
                    .bufferedReader()
                    .use { it.readText() }
            }

            val errThread = Thread {
                error = process.errorStream
                    .bufferedReader()
                    .use { it.readText() }
            }

            outThread.start()
            errThread.start()

            val exitCode = process.waitFor()

            outThread.join()
            errThread.join()

            Bundle().apply {
                putString("output", output)
                putString("error", error)
                putInt("exitCode", exitCode)
            }
        } catch (e: Exception) {
            Bundle().apply {
                putString("output", "")
                putString("error", Log.getStackTraceString(e))
                putInt("exitCode", -1)
            }
        }
    }

    override fun runOnce(data: ByteArray, delayMs: Long): Int {
        Log.i("ShellServer", "runOnce: $delayMs")
        val id = nextId.incrementAndGet()
        val task = deserialize(data)

        val future = executor.schedule({
            try {
                task.run()
            } catch (e: Throwable) {
                Log.e("ShellServer", "Task failed", e)
            } finally {
                tasks.remove(id)
            }
        }, delayMs, TimeUnit.MILLISECONDS)

        tasks[id] = future
        return id
    }

    override fun schedule(
        data: ByteArray,
        initialDelayMs: Long,
        intervalMs: Long
    ): Int {
        Log.i("ShellServer", "schedule: $initialDelayMs, $intervalMs")
        val id = nextId.incrementAndGet()
        val task = deserialize(data)

        val future =
            executor.scheduleWithFixedDelay(
                {
                    try {
                        task.run()
                    } catch (e: Throwable) {
                        Log.e("ShellServer", "Task failed", e)
                    }
                }, initialDelayMs,
                intervalMs,
                TimeUnit.MILLISECONDS
            )
        tasks[id] = future
        return id
    }

    override fun cancel(taskId: Int) {
        Log.i("ShellServer", "cancel: $taskId")
        val future = tasks.remove(taskId)

        future?.cancel(true)
    }

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        enforcePermission()

        if (code != Constants.TRANSACTION_invodeSystemService) {
            return super.onTransact(code, data, reply, flags)
        }
        val targetBinder = data.readStrongBinder()
        val targetCode = data.readInt()
        val targetFlags = data.readInt()

        val newData = Parcel.obtain()
        try {
            newData.appendFrom(data, data.dataPosition(), data.dataAvail())
        } catch (tr: Throwable) {
            newData.recycle()
            return true
        }
        val id = clearCallingIdentity()
        try {
            targetBinder.transact(targetCode, newData, reply, targetFlags)
        } finally {
            restoreCallingIdentity(id)
            newData.recycle()
        }
        return true
    }
}