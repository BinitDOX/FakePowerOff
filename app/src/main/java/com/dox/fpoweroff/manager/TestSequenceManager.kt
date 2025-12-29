package com.dox.fpoweroff.manager

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import com.dox.fpoweroff.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSequenceManager @Inject constructor(
    private val testOverlayManager: TestOverlayManager
) {
    private val vibrationDuration = 700L
    private val vibrationIntensity = VibrationEffect.DEFAULT_AMPLITUDE
    private var currentDialog: Dialog? = null

    fun startTestSequence(
        context: Context,
        sequenceToTest: String,
        onSuccess: () -> Unit
    ) {
        if (currentDialog?.isShowing == true) return

        val dialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val view = View.inflate(context, R.layout.shutdown_overlay, null)
        dialog.setContentView(view)

        val shutdownView = view.findViewById<RelativeLayout>(R.id.shutdown_view)
        val countdownText = view.findViewById<TextView>(R.id.countdown_text)
        val inputText = view.findViewById<TextView>(R.id.input_text)

        val dialogScope = CoroutineScope(Dispatchers.Main + Job())

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnDismissListener {
            dialogScope.cancel()
            testOverlayManager.stopTest()
            currentDialog = null
        }

        currentDialog = dialog
        dialog.show()
        makeDialogImmersive(dialog)

        dialogScope.launch {
            testOverlayManager.dismissEventFlow
                .onEach {
                    onSuccess()
                    dialog.dismiss()
                }
                .launchIn(this)

            testOverlayManager.inputStateFlow
                .onEach { input -> inputText.text = input }
                .launchIn(this)

            testOverlayManager.startTest(sequenceToTest)

            delay(3000L)
            vibrateDevice(context)
            shutdownView.visibility = View.INVISIBLE

            inputText.visibility = View.VISIBLE
            countdownText.visibility = View.VISIBLE

            for (i in 10 downTo 0) {
                countdownText.text = i.toString()
                delay(1000L)
            }
            if (dialog.isShowing) dialog.dismiss()
        }
    }
    private fun makeDialogImmersive(dialog: Dialog) {
        dialog.window?.let { window ->
            // 1. Remove the default dim background and make the window fill the screen
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setBackgroundDrawableResource(android.R.color.black)

            // 2. Allow the window to extend into the "No Man's Land" (behind nav bars)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

            // 3. Set bar colors to transparent so they don't show a 'scrim' or tint
            window.statusBarColor = Color.BLACK
            window.navigationBarColor = Color.BLACK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.navigationBarDividerColor = Color.BLACK
            }

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                val controller = window.insetsController
                controller?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // Android 10 (API 29) and below
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS

                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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