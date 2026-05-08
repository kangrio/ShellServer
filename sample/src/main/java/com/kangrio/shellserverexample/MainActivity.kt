package com.kangrio.shellserverexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.kangrio.shellserver.client.ShellServerHelper

class MainActivity : Activity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvOutput: TextView
    private lateinit var etCommand: EditText
    private lateinit var btnCancel: Button

    private var scheduledTaskId: Int = -1
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        tvOutput = findViewById(R.id.tv_output)
        etCommand = findViewById(R.id.et_command)
        btnCancel = findViewById(R.id.btn_cancel)

        val btnRunOnce = findViewById<Button>(R.id.btn_run_once)
        val btnSchedule = findViewById<Button>(R.id.btn_schedule)
        val btnExec = findViewById<Button>(R.id.btn_exec)
        val btnHome = findViewById<Button>(R.id.btn_home)
        val btnRecents = findViewById<Button>(R.id.btn_recents)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val btnVolUp = findViewById<Button>(R.id.btn_vol_up)
        val btnLaunchCalc = findViewById<Button>(R.id.btn_launch_calc)
        val btnBattery = findViewById<Button>(R.id.btn_battery)
        val btnScreenshot = findViewById<Button>(R.id.btn_screenshot)
        val btnTap = findViewById<Button>(R.id.btn_tap)
        val btnText = findViewById<Button>(R.id.btn_text)
        val btnLogcat = findViewById<Button>(R.id.btn_logcat)
        val btnTopProc = findViewById<Button>(R.id.btn_top_proc)
        val btnListDataSystem = findViewById<Button>(R.id.btn_list_data_system)
        val btnWmInfo = findViewById<Button>(R.id.btn_wm_info)
        val btnSleep = findViewById<Button>(R.id.btn_sleep)
        val btnWake = findViewById<Button>(R.id.btn_wake)
        val btnKillSettings = findViewById<Button>(R.id.btn_kill_settings)

        updateStatus("Connecting...")

        // Initialize ShellServerHelper
        ShellServerHelper.init(this) { binder ->
            mainHandler.post {
                if (binder != null) {
                    updateStatus("Status: Connected")
                } else {
                    updateStatus("Status: Failed to connect")
                }
            }
        }

        btnHome.setOnClickListener {
            val id = ShellServerHelper.runOnce(NavigationRunnable(KeyEvent.KEYCODE_HOME))
            showOutput("Injected Home (Task ID: $id)")
        }

        btnRecents.setOnClickListener {
            val id = ShellServerHelper.runOnce(NavigationRunnable(KeyEvent.KEYCODE_APP_SWITCH))
            showOutput("Injected Recents (Task ID: $id)")
        }

        btnBack.setOnClickListener {
            val id = ShellServerHelper.runOnce(NavigationRunnable(KeyEvent.KEYCODE_BACK))
            showOutput("Injected Back (Task ID: $id)")
        }

        btnVolUp.setOnClickListener {
            val id = ShellServerHelper.runOnce(NavigationRunnable(KeyEvent.KEYCODE_VOLUME_UP))
            showOutput("Injected Vol+ (Task ID: $id)")
        }

        btnLaunchCalc.setOnClickListener {
            val id = ShellServerHelper.runOnce(AppLauncherRunnable("com.android.documentsui"))
            showOutput("Launching Calculator (Task ID: $id)")
        }

        btnBattery.setOnClickListener {
            val id = ShellServerHelper.runOnce(DiagnosticsRunnable(DiagnosticsRunnable.Type.BATTERY))
            showOutput("Fetching Battery Info (Task ID: $id)\nCheck Logcat.")
        }

        btnScreenshot.setOnClickListener {
            Thread {
                val path = "${externalCacheDir?.absolutePath}/screenshot.png"
                ShellServerHelper.exec("screencap -p $path")
                mainHandler.post { showOutput("Screenshot saved to:\n$path") }
            }.start()
        }

        btnTap.setOnClickListener {
            Thread {
                ShellServerHelper.exec("input tap 540 1100")
            }.start()
            showOutput("Sent Tap event at (540, 1100)")
        }

        btnText.setOnClickListener {
            etCommand.requestFocus()
            Thread {
                Thread.sleep(500)
                ShellServerHelper.exec("input text 'Hello_from_Shell!'")
            }.start()
            showOutput("Typing text...")
        }

        btnLogcat.setOnClickListener {
            Thread {
                val result = ShellServerHelper.exec("logcat -d -t 50")
                mainHandler.post { showOutput("Recent Logcat:\n$result") }
            }.start()
        }

        btnTopProc.setOnClickListener {
            Thread {
                val result = ShellServerHelper.exec("ps -A | head -n 6")
                mainHandler.post { showOutput("Top 5 Processes:\n$result") }
            }.start()
        }

        btnListDataSystem.setOnClickListener {
            Thread {
                val result = ShellServerHelper.exec("ls -l /data/system | head -n 20")
                mainHandler.post { showOutput("Restricted Files (/data/system):\n$result") }
            }.start()
        }

        btnWmInfo.setOnClickListener {
            val id = ShellServerHelper.runOnce(DiagnosticsRunnable(DiagnosticsRunnable.Type.DISPLAY))
            showOutput("Fetching Display Info (Task ID: $id)\nCheck Logcat.")
        }

        btnSleep.setOnClickListener {
            val id = ShellServerHelper.runOnce(PowerRunnable(PowerRunnable.Action.SLEEP))
            showOutput("Triggered Sleep (Task ID: $id)")
        }

        btnWake.setOnClickListener {
            val id = ShellServerHelper.runOnce(PowerRunnable(PowerRunnable.Action.WAKE))
            showOutput("Triggered Wake (Task ID: $id)")
        }

        btnKillSettings.setOnClickListener {
            val id = ShellServerHelper.runOnce(KillPackageRunnable("com.android.settings"))
            showOutput("Triggered Kill Settings (Task ID: $id)")
        }

        btnRunOnce.setOnClickListener {
            val id = ShellServerHelper.runOnce(ServerRunnable())
            showOutput("RunOnce task ID: $id\nCheck Logcat for 'ServerRunnable' tags.")
        }

        btnSchedule.setOnClickListener {
            scheduledTaskId = ShellServerHelper.schedule(ServerRunnable(), initialDelay = 0, period = 5000)
            if (scheduledTaskId != -1) {
                btnCancel.isEnabled = true
                showOutput("Scheduled task ID: $scheduledTaskId (Every 5 seconds)\nCheck Logcat for 'ServerRunnable' tags.")
            } else {
                showOutput("Failed to schedule task.")
            }
        }

        btnCancel.setOnClickListener {
            if (scheduledTaskId != -1) {
                ShellServerHelper.cancel(scheduledTaskId)
                showOutput("Cancelled task ID: $scheduledTaskId")
                scheduledTaskId = -1
                btnCancel.isEnabled = false
            }
        }

        btnExec.setOnClickListener {
            val cmd = etCommand.text.toString()
            if (cmd.isNotBlank()) {
                Thread {
                    val result = ShellServerHelper.exec(cmd)
                    mainHandler.post {
                        showOutput("Command: $cmd\nResult:\n$result")
                    }
                }.start()
            }
        }
    }

    private fun updateStatus(status: String) {
        tvStatus.text = status
    }

    private fun showOutput(text: String) {
        tvOutput.text = text
    }
}
