package com.kangrio.shellserverexample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.kangrio.shellserver.client.ShellServerHelper

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ShellServerHelper.init(this) {
            Log.i("MainActivity", "Server connected")
        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            ShellServerHelper.runOnce(ServerRunnable())
        }
    }
}