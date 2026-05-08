package com.kangrio.shellserver.client.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.util.Log
import androidx.core.content.ContextCompat
import com.kangrio.shellserver.Constants
import com.kangrio.shellserver.IShellServer
import com.kangrio.shellserver.client.BinderWrapper
import com.kangrio.shellserver.server.ShellServer
import dadb.Dadb
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
object ServerUtil {
    private var mRemote: IShellServer? = null
    private var remoteBinder: IBinder? = null
    private val serviceInterfaceCaches = mutableMapOf<String, IInterface>()

    private var mContext: Context? = null

    private val binderReceiver = BinderReceiver()

    private var onServerStarted: ((IBinder?) -> Unit)? = null

    fun init(
        context: Context,
        killExisting: Boolean = true,
        onServerStarted: ((IBinder?) -> Unit)
    ) {
        this.onServerStarted = onServerStarted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("L")
        }
        mContext = context
        handleShellBinder()
        Executors.newSingleThreadExecutor().execute {
            Log.i("ShellServerHelper", System.getProperties().toString())
            var home = System.getProperty("user.home")
            home = if (home.isNullOrEmpty()) context.dataDir.absolutePath else home
            System.setProperty("user.home", home)

            val ip = if (Build.FINGERPRINT.contains("generic")) {
                "10.0.2.2"
            } else {
                "localhost"
            }
            val adb = Dadb.discover(ip) ?: Dadb.create(ip, 5555)
            val apkPath = mContext?.applicationInfo?.sourceDir
            val packageName = mContext?.packageName
            val serverName = "shellserver_${packageName}"
            val shellServerClassName = ShellServer::class.java.name
            val killAllCommand =
                $$"""pids=$(pidof $$serverName) && [ -n "$pids" ] && kill $pids"""
            val killAllExceptLastOneCommand =
                $$"""pids=$(pidof $$serverName) && [ "$(echo "$pids" | wc -w)" -gt 1 ] && kill $(echo "$pids" | awk '{for (i=1; i<NF; i++) print $i}')"""
            val startCommand =
                """nohup app_process -Djava.class.path=$apkPath /system/bin --nice-name=$serverName $shellServerClassName $packageName > /dev/null 2>&1 & while [ -z "$(pidof $serverName)" ]; do sleep 1; done"""
            val startIfNotExistCommand =
                """$killAllExceptLastOneCommand; [ -z "$(pidof $serverName)" ] && $startCommand"""
            val startWithKillExistCommand = "$killAllCommand;$startCommand"

            val command =
                if (killExisting) startWithKillExistCommand else startIfNotExistCommand
            Log.w("ShellServerHelper", command)
            Log.w(this::class.java.name, "command: ${adb.shell(command)}")
        }
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getRemote(): IShellServer? {
        if (mRemote != null) {
            return mRemote
        }
        val sm = Class.forName("android.os.ServiceManager")
        val getService = sm.getDeclaredMethod(
            "getService",
            String::class.java
        )
        getService.isAccessible = true

        val binder = getService.invoke(null, "shellserver_${mContext?.packageName}") as IBinder?

        mRemote = IShellServer.Stub.asInterface(binder)

        return mRemote
    }

    fun getRemoteBinder(): IBinder? {
        if (remoteBinder == null) {
            remoteBinder = getRemote()?.asBinder()
        }
        return remoteBinder
    }

    private fun handleShellBinder() {
        ContextCompat.registerReceiver(
            mContext!!,
            binderReceiver,
            IntentFilter(Constants.ACTION_RECEIVE_BINDER),
            ContextCompat.RECEIVER_EXPORTED
        )
        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                mContext?.unregisterReceiver(binderReceiver)
            } catch (e: Throwable) {
                Log.e("ShellServerHelper", "handleShellBinder: ", e)
            }
        })
    }

    private fun getServiceInterface(serviceName: String): IInterface? {
        val binder = BinderWrapper(
            serviceName
        )

        val service = mContext?.getSystemService(serviceName) ?: return null
        val mServiceField = service.javaClass.declaredFields.firstOrNull { serviceFields ->
            IInterface::class.java.isAssignableFrom(serviceFields.type) && serviceFields.type.declaredClasses.any { it.simpleName == "Stub" }
        } ?: return null

        val stub = mServiceField.type.declaredClasses.firstOrNull {
            it.simpleName == "Stub"
        } ?: return null

        val asInterface = stub.getDeclaredMethod("asInterface", IBinder::class.java)
        val iInterface = asInterface.invoke(null, binder) as IInterface

        return iInterface
    }

    fun getSystemServiceInterface(
        serviceName: String
    ): IInterface? {

        val serviceInterfaceCache = serviceInterfaceCaches[serviceName]
        if (serviceInterfaceCache != null) {
            return serviceInterfaceCache
        }

        val iInterface = getServiceInterface(serviceName) ?: return null

        serviceInterfaceCaches[serviceName] = iInterface

        return iInterface
    }

    class BinderReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            val binder = intent?.extras?.getBinder("binder")
            remoteBinder = binder
            mRemote = IShellServer.Stub.asInterface(remoteBinder)
            onServerStarted?.invoke(remoteBinder)

            val result = if (binder != null) "success" else "failed"
            Log.i("ShellServerHelper", "binderReceiver: $result")
        }
    }
}