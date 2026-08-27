package com.example.customqs

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showSetup() }
    private fun showSetup() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(48,48,48,48); setBackgroundColor(Color.rgb(22,9,13)) }
        val title = TextView(this).apply { text="Android 16\nQuick Settings"; textSize=30f; gravity=Gravity.CENTER; setTextColor(Color.WHITE) }
        val info = TextView(this).apply { text="Enable the accessibility service to use the top-edge trigger."; textSize=16f; gravity=Gravity.CENTER; setTextColor(Color.LTGRAY); setPadding(0,24,0,32) }
        val button = TextView(this).apply { text="OPEN ACCESSIBILITY SETTINGS"; textSize=15f; gravity=Gravity.CENTER; setTextColor(Color.rgb(30,10,14)); setBackgroundColor(Color.rgb(224,83,112)); setPadding(32,24,32,24); setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) } }
        root.addView(title); root.addView(info); root.addView(button); setContentView(root)
    }
}
