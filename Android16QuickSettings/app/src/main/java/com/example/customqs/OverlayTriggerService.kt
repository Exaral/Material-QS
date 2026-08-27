package com.example.customqs

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.drawable.GradientDrawable

class OverlayTriggerService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var trigger: View? = null
    private var panel: LinearLayout? = null
    private var open = false
    private var torchOn = false

    override fun onServiceConnected() { super.onServiceConnected(); wm=getSystemService(WINDOW_SERVICE) as WindowManager; createTrigger(); createPanel() }

    private fun lp(h:Int, touchable:Boolean=true): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        (if (touchable) 0 else WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP }

    private fun createTrigger() {
        trigger=View(this).apply { setBackgroundColor(Color.TRANSPARENT); setOnTouchListener { _,e -> if(e.action==MotionEvent.ACTION_DOWN){ toggle(); true } else true } }
        wm.addView(trigger,lp(dp(64)))
    }

    private fun createPanel() {
        panel=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(76),dp(20),dp(24)); visibility=View.GONE
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(40,12,20),Color.rgb(22,9,13)))
            addView(TextView(this@OverlayTriggerService).apply { text="Quick Settings"; textSize=30f; setTextColor(Color.WHITE); setPadding(dp(8),0,0,dp(16)) })
            val grid=GridLayout(this@OverlayTriggerService).apply { columnCount=2; rowCount=2 }
            grid.addView(tile("Internet","#E05370"){ startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) })
            grid.addView(tile("Bluetooth","#802238"){ startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) })
            grid.addView(tile("Torch","#802238"){ toggleTorch() })
            grid.addView(tile("Settings","#5E1D30"){ startActivity(Intent(Settings.ACTION_SETTINGS)) })
            addView(grid)
        }
        wm.addView(panel,lp(dp(430)))
    }

    private fun tile(label:String,color:String,onClick:()->Unit):TextView = TextView(this).apply {
        text=label; textSize=17f; gravity=Gravity.CENTER; setTextColor(Color.WHITE); isClickable=true; isFocusable=true
        background=GradientDrawable().apply { setColor(Color.parseColor(color)); cornerRadius=dp(28).toFloat() }
        setPadding(dp(12),0,dp(12),0); setOnClickListener { performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY); onClick() }
        layoutParams=GridLayout.LayoutParams().apply { width=0; height=dp(150); columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1,1f); rowSpec=GridLayout.spec(GridLayout.UNDEFINED,1,1f); setMargins(dp(6),dp(6),dp(6),dp(6)) }
    }

    private fun toggle(){ open=!open; panel?.visibility=if(open) View.VISIBLE else View.GONE }

    private fun toggleTorch(){
        val cm=getSystemService(CAMERA_SERVICE) as CameraManager
        val id=cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)==true } ?: return
        torchOn=!torchOn
        try { cm.setTorchMode(id,torchOn) } catch(_:Exception) { torchOn=!torchOn }
    }

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy(){ super.onDestroy(); listOf(trigger,panel).forEach { if(it?.parent!=null) wm.removeView(it) }; trigger=null; panel=null }
}
