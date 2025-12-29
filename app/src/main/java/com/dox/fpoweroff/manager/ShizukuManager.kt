package com.dox.fpoweroff.manager

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var originalBrightness: Int = 100
    private var originalMode: Int = 1
    private var originalExtraDim: Int = 0

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Timber.d("Shizuku Binder Received!")
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                Timber.d("Shizuku Permission Granted")
            }
        }
    }

    init {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
    }

    fun isShizukuAvailable(): Boolean {
        return Shizuku.pingBinder()
    }

    fun checkPermission(): Boolean {
        if (!isShizukuAvailable()) return false
        return try {
            if (Shizuku.isPreV11()) {
                true
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission() {
        if (isShizukuAvailable() && !checkPermission()) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    suspend fun enableVoidMode() {
        if (!checkPermission()) return

        withContext(Dispatchers.IO) {
            try {
                val currentBright = runShellCommand("settings get system screen_brightness")
                val currentMode = runShellCommand("settings get system screen_brightness_mode")
                // Check Extra Dim state (Android 12+)
                val currentExtraDim = runShellCommand("settings get secure reduce_bright_colors_activated")

                originalBrightness = currentBright.toIntOrNull() ?: 100
                originalMode = currentMode.toIntOrNull() ?: 1
                originalExtraDim = currentExtraDim.toIntOrNull() ?: 0

                Timber.d("VoidMode: Saved - B:$originalBrightness, M:$originalMode, Dim:$originalExtraDim")

                // Enable "Extra Dim" feature (Android 12+)
                runShellCommand("settings put secure reduce_bright_colors_activated 1")
                // Maximize the "Extra Dim" intensity (optional, usually 0-100)
                runShellCommand("settings put secure reduce_bright_colors_level 100")

                // Disable Auto-Brightness
                runShellCommand("settings put system screen_brightness_mode 0")

                // Set Brightness to absolute 0
                runShellCommand("settings put system screen_brightness 0")

            } catch (e: Exception) {
                Timber.e(e, "Failed to enable Void Mode")
            }
        }
    }

    suspend fun disableVoidMode() {
        if (!checkPermission()) return

        withContext(Dispatchers.IO) {
            try {
                // Restore Everything
                runShellCommand("settings put system screen_brightness $originalBrightness")
                runShellCommand("settings put system screen_brightness_mode $originalMode")
                runShellCommand("settings put secure reduce_bright_colors_activated $originalExtraDim")

                Timber.d("VoidMode: Restored")
            } catch (e: Exception) {
                Timber.e(e, "Failed to disable Void Mode")
            }
        }
    }

    private fun runShellCommand(command: String): String {
        return try {
            // REFLECTION: Access the private 'newProcess' method
            // Signature: newProcess(String[] cmd, String[] env, String dir)
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true

            // Invoke: Shizuku.newProcess(cmdArray, null, null)
            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            // Read Standard Output
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText().trim()

            // Read Error Output (CRITICAL FOR DEBUGGING)
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val errorOutput = errorReader.readText().trim()

            process.waitFor()

            if (errorOutput.isNotEmpty()) {
                Timber.e("Shizuku Command Error for '$command': $errorOutput")
            } else {
                Timber.d("Shizuku Command Success: '$command' -> '$output'")
            }

            output
        } catch (e: Exception) {
            Timber.e(e, "Shizuku Exception: $command")
            ""
        }
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}