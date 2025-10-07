package com.dox.fpoweroff.manager

import android.app.Service
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.WindowManager
import com.dox.fpoweroff.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.view.WindowInsetsController
import android.view.WindowInsets

@Singleton
class OverlayManager @Inject constructor() {
    private var overlayView: View? = null
    private val vibrationDuration = 700L
    private val vibrationIntensity = VibrationEffect.DEFAULT_AMPLITUDE

    fun showShutdownSequence(context: Context) {
        if (overlayView != null) return
        if (context !is Service) return

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val windowType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

        val view = View.inflate(context, R.layout.shutdown_overlay, null)
        overlayView = view

        val flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType,
            flags,
            -1
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        windowManager.addView(overlayView, layoutParams)
        makeViewImmersive(overlayView)

        view.findViewById<View>(R.id.countdown_text).visibility = View.GONE
        view.findViewById<View>(R.id.input_text).visibility = View.GONE

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000L) // Wait 3 seconds
            vibrateDevice(context)
            delay(2000L) // Wait 2 more seconds
            overlayView?.findViewById<View>(R.id.shutdown_view)?.visibility = View.INVISIBLE
        }
    }

    fun hideOverlay(context: Context) {
        if (overlayView != null) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    private fun makeViewImmersive(view: View?) {
        view?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = it.windowInsetsController
                controller?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                it.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }

    private fun vibrateDevice(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, vibrationIntensity))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, vibrationIntensity))
            }
        }
    }
}