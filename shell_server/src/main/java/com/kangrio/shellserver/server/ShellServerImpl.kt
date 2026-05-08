package com.kangrio.shellserver.server

import android.content.Context
import android.os.Parcel
import android.util.Log
import com.kangrio.shellserver.Constants
import com.kangrio.shellserver.IShellServer
import com.kangrio.shellserver.shared.BaseShellServerRunnable
import com.kangrio.shellserver.shared.ShellServerRunnable
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class ShellServerImpl : IShellServer.Stub() {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val nextId = AtomicInteger()
    private val tasks = ConcurrentHashMap<Int?, ScheduledFuture<*>?>()

    private val loaders = ConcurrentHashMap<String, DexClassLoader>()

    override fun invodeSystemService() {
    }

    fun getRunnableObject(className: String): ShellServerRunnable {
        val context = ContextHelper.getProcessContext()

        val uid = getCallingUid()

        val pkg = context.packageManager
            .getPackagesForUid(uid)
            ?.firstOrNull()
            ?: error("No package")

        val apkPath = context.packageManager
            .getApplicationInfo(pkg, 0)
            .sourceDir

        val loader = loaders.getOrPut(apkPath) {
            DexClassLoader(
                apkPath,
                context.codeCacheDir.absolutePath,
                null,
                BaseShellServerRunnable::class.java.classLoader
            )
        }

        val clazz = loader.loadClass(className)

        val runnable = clazz
            .getDeclaredConstructor()
            .newInstance() as BaseShellServerRunnable

        runnable.serverContext = context

        return runnable
    }

    override fun exec(cmd: String): String {
        Log.i("ShellServer", "exec: $cmd")
        return Runtime.getRuntime().exec(cmd).inputStream.bufferedReader().readText()
    }

    override fun runOnce(className: String, delayMs: Long): Int {
        Log.i("ShellServer", "runOnce: $delayMs")
        val id = nextId.incrementAndGet()
        val task = getRunnableObject(className)

        val future = executor.schedule({
            try {
                task.run()
            } catch (e: Throwable) {
                Log.e("ShellServer", "", e)
            } finally {
                tasks.remove(id)
            }
        }, delayMs, TimeUnit.MILLISECONDS)

        tasks[id] = future

        return id
    }

    override fun schedule(
        className: String,
        initialDelayMs: Long,
        intervalMs: Long
    ): Int {
        Log.i("ShellServer", "schedule: $initialDelayMs, $intervalMs")
        val id = nextId.incrementAndGet()
        val task = getRunnableObject(className)

        val future =
            executor.scheduleWithFixedDelay(
                {
                    try {
                        task.run()
                    } catch (e: Throwable) {
                        Log.e("ShellServer", "", e)
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
        try {
            val id = clearCallingIdentity()
            targetBinder.transact(targetCode, newData, reply, targetFlags)
            restoreCallingIdentity(id)
        } finally {
            newData.recycle()
        }
        return true
    }
}