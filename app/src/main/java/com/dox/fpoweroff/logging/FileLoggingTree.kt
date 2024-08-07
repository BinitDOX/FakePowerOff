package com.dox.fpoweroff.logging

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dox.fpoweroff.utility.Constants.APP_ID
import com.dox.fpoweroff.utility.Constants.APP_NAME
import com.dox.fpoweroff.utility.Constants.LOG_WORKER_NAME
import com.dox.fpoweroff.utility.Constants.PERIODIC_LOG_DELETION_INTERVAL_DAYS
import com.dox.fpoweroff.worker.LogMaintenanceWorker
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class FileLoggingTree(private val context: Context,
                      private val writeToPrivateDir: Boolean = true,
) : Timber.DebugTree(), Thread.UncaughtExceptionHandler {
    private var isLoggingException = false

    init {
        // Set this class as the default uncaught exception handler
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        schedulePeriodicWorker()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        // Check if the exception is already being logged to prevent recursion
        if (!isLoggingException) {
            isLoggingException = true
            // Log uncaught exception to the file
            logException(e)

            Log.e("FileLoggingTree", "[Global Exception Handler] Uncaught Exception: ${e}")

            val crashMessage = "Something went wrong, $APP_NAME will crash"
            Toast.makeText(context, crashMessage, Toast.LENGTH_LONG).show()

            isLoggingException = false
            defaultUncaughtExceptionHandler?.uncaughtException(t, e)
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logFile = getLogFile()

        try {
            FileWriter(logFile, true).use { writer ->
                writer.append("[${getCurrentDateTime()}] [${logLevelToString(priority)}] [$tag]: $message\n")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun logException(throwable: Throwable) {
        val logFile = getLogFile()

        try {
            FileWriter(logFile, true).use { fileWriter ->
                PrintWriter(fileWriter).use { printWriter ->
                    printWriter.append("${getCurrentDateTime()} [${logLevelToString(Log.ERROR)}] [Global Exception Handler]: " +
                            "Uncaught Exception: ${throwable.message}\n")
                    throwable.printStackTrace(printWriter)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLogFile(): File {
        val logDir = if (writeToPrivateDir) {
            File(context.filesDir, "${APP_ID}-logs")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "${APP_ID}-logs")
        }

        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val logFile = File(logDir, "log_$currentDate.txt")
        return logFile
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun logLevelToString(priority: Int): String {
        return when (priority) {
            Log.DEBUG -> "DEBUG"
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            Log.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    private fun schedulePeriodicWorker(){
        val repeatInterval = PERIODIC_LOG_DELETION_INTERVAL_DAYS

        val workRequest = PeriodicWorkRequestBuilder<LogMaintenanceWorker>(repeatInterval, TimeUnit.DAYS)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag(LOG_WORKER_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            LOG_WORKER_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Timber.d("[${::schedulePeriodicWorker.name}] [${LOG_WORKER_NAME}] Scheduled")
    }

    companion object {
        private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null
    }
}
