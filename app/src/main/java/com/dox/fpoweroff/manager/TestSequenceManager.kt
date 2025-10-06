package com.dox.fpoweroff.manager

import android.app.Dialog
import android.content.Context
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
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                controller?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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