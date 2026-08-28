package com.example.customqs

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.roundToInt

class OverlayTriggerService : AccessibilityService() {
    private lateinit var wm: WindowManager
    private var trigger: TriggerView? = null
    private var panel: QSView? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        trigger = TriggerView(this).also { v ->
            val p = WindowManager.LayoutParams(-1, dp(72), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP }
            wm.addView(v,p)
        }
    }

    private fun openPanel() {
        if (panel != null) return
        panel = QSView(this)
        val p = WindowManager.LayoutParams(-1, -1, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP }
        wm.addView(panel,p)
        trigger?.visibility = View.GONE
    }
    fun closePanel() { panel?.let { wm.removeView(it) }; panel=null; trigger?.visibility=View.VISIBLE }
    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { try { trigger?.let(wm::removeView) }; catch(_:Exception){}; try { panel?.let(wm::removeView) }; catch(_:Exception){}; super.onDestroy() }

    private fun dp(v:Int)= (v*resources.displayMetrics.density).roundToInt()

    private inner class TriggerView(c:Context): View(c) {
        var downY=0f; var downT=0L
        init { setBackgroundColor(Color.TRANSPARENT); isClickable=true }
        override fun onTouchEvent(e:MotionEvent):Boolean { when(e.actionMasked){ MotionEvent.ACTION_DOWN->{downY=e.rawY;downT=System.currentTimeMillis();return true}; MotionEvent.ACTION_UP->{ val dy=e.rawY-downY; if(dy>dp(36) || System.currentTimeMillis()-downT>350) openPanel(); return true } }; return true }
    }

    private inner class QSView(c:Context): View(c) {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG); private val rect=RectF(); private var downY=0f
        private var brightness=Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)/255f
        private var torch=false
        private val bg=Color.rgb(25,14,17); private val on=Color.rgb(255,181,194); private val off=Color.rgb(76,45,51); private val text=Color.rgb(255,235,238)
        init { setLayerType(View.LAYER_TYPE_SOFTWARE,null); paint.typeface=Typeface.create("sans",Typeface.NORMAL); isClickable=true }
        private fun rr(c:Canvas,l:Float,t:Float,r:Float,b:Float,rad:Float,color:Int){paint.color=color;paint.style=Paint.Style.FILL;c.drawRoundRect(l,t,r,b,rad,rad,paint)}
        override fun onDraw(c:Canvas){ super.onDraw(c); c.drawColor(bg); val w=width.toFloat();
            paint.color=text;paint.textSize=dp(23).toFloat();c.drawText("Quick Settings",dp(24).toFloat(),dp(52).toFloat(),paint)
            paint.textSize=dp(14).toFloat();paint.color=Color.rgb(190,165,170);c.drawText("Android 16 style",dp(24).toFloat(),dp(76).toFloat(),paint)
            val left=dp(20).toFloat(); val right=w-dp(20); val top=dp(98).toFloat(); val barH=dp(58).toFloat()
            rr(c,left,top,right,top+barH,barH/2,Color.rgb(66,42,47)); rr(c,left,top,left+(right-left)*brightness,top+barH,barH/2,on)
            paint.color=Color.rgb(42,20,24); c.drawCircle(left+(right-left)*brightness,top+barH/2,dp(20).toFloat(),paint)
            paint.color=text;paint.textSize=dp(18).toFloat();c.drawText("☀",left+dp(18).toFloat(),top+dp(37).toFloat(),paint)
            val gap=dp(12).toFloat(); val tileW=(w-dp(40)-gap)/2; val y=top+barH+dp(18); val h=dp(112).toFloat()
            tile(c,left,y,left+tileW,y+h,"Internet","Wi‑Fi & mobile",true); tile(c,left+tileW+gap,y,right,y+h,"Bluetooth","Bluetooth",false)
            val y2=y+h+gap; tile(c,left,y2,left+tileW,y2+h,"Flashlight",if(torch)"On" else "Off",torch); tile(c,left+tileW+gap,y2,right,y2+h,"Auto-rotate","Portrait",false)
            val y3=y2+h+gap; tile(c,left,y3,left+tileW,y3+h,"Airplane mode","Off",false); tile(c,left+tileW+gap,y3,right,y3+h,"Do Not Disturb","Off",false)
            paint.color=Color.rgb(150,125,130);paint.textSize=dp(13).toFloat();c.drawText("Swipe up to close",left,y3+h+dp(42).toFloat(),paint)
        }
        private fun tile(c:Canvas,l:Float,t:Float,r:Float,b:Float,title:String,sub:String,active:Boolean){ rr(c,l,t,r,b,dp(28).toFloat(),if(active)on else off); paint.color=if(active)Color.rgb(55,22,29) else text; paint.textSize=dp(18).toFloat(); c.drawText(title,l+dp(18),t+dp(42),paint); paint.textSize=dp(13).toFloat();paint.color=if(active)Color.rgb(85,45,53) else Color.rgb(210,190,194);c.drawText(sub,l+dp(18),t+dp(70),paint) }
        override fun onTouchEvent(e:MotionEvent):Boolean { val w=width.toFloat(); val left=dp(20).toFloat(); val right=w-dp(20); val top=dp(98).toFloat(); val barH=dp(58).toFloat(); when(e.actionMasked){ MotionEvent.ACTION_DOWN->{downY=e.y; return true}; MotionEvent.ACTION_MOVE->{ if(abs(e.y-downY)<dp(12)){ val x=e.x.coerceIn(left,right); brightness=((x-left)/(right-left)).coerceIn(.01f,1f); if(Settings.System.canWrite(this@OverlayTriggerService)) Settings.System.putInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS,(brightness*255).roundToInt()); invalidate() }; return true}; MotionEvent.ACTION_UP->{ val dy=e.y-downY; if(dy < -dp(100)){ closePanel(); return true }; val gap=dp(12).toFloat(); val tileW=(w-dp(40)-gap)/2; val y=top+barH+dp(18); val h=dp(112).toFloat(); val y2=y+h+gap; if(e.y in y2..y2+h && e.x in left..left+tileW){ toggleTorch(); invalidate() }; return true } }; return true }
        private fun toggleTorch(){ if(Build.VERSION.SDK_INT<23)return; try { val cm=getSystemService(CAMERA_SERVICE) as CameraManager; val id=cm.cameraIdList.firstOrNull { cm.getCameraCharacteristics(it).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)==true } ?: return; torch=!torch; cm.setTorchMode(id,torch) } catch(_:Exception){} }
        private fun dp(v:Int)= (v*resources.displayMetrics.density).roundToInt()
    }
}
