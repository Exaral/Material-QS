package com.example.customqs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48,64,48,48) }
        root.addView(TextView(this).apply { text="Android 16 Quick Settings"; textSize=28f; setPadding(0,0,0,24) })
        root.addView(TextView(this).apply { text="Enable the Accessibility Service. Then swipe down from the invisible strip at the very top of the screen to open the QS panel.\n\nThe app is designed as an overlay replacement, not a launcher-style fake QS."; textSize=17f; setPadding(0,0,0,32) })
        root.addView(android.widget.Button(this).apply { text="Open Accessibility settings"; setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } })
        root.addView(android.widget.Button(this).apply { text="Allow brightness control"; setOnClickListener { if (!Settings.System.canWrite(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))) } })
        setContentView(root)
    }
}
