package com.dox.fpoweroff.worker

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dox.fpoweroff.utility.Constants
import com.dox.fpoweroff.utility.Constants.PRIVATE_LOGS
import timber.log.Timber
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LogMaintenanceWorker (
    private val appContext: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("[doWork] [LOG] Performing log maintenance.")

        if(!deleteOldLogs()) return Result.failure()

        Timber.d("[doWork] [LOG] Completed log maintenance.")
        return Result.success()
    }

    private fun deleteOldLogs(): Boolean {
        try {
            val logDir = if (PRIVATE_LOGS) {
                File(appContext.filesDir, "${Constants.APP_ID}-logs")
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "${Constants.APP_ID}-logs")
            }

            if (!logDir.exists()) {
                Timber.e("[deleteOldLogs] [LOG] Log directory does not exist.")
                return false
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for (i in 0..30) {  // Delete logs older than 7 days
                val currentDate = Calendar.getInstance()
                currentDate.add(Calendar.DAY_OF_YEAR, -i)
                val thresholdDate = currentDate.timeInMillis

                val file = File(logDir, "log_${dateFormat.format(thresholdDate)}.txt")

                if(file.exists()){
                    try {
                        Files.delete(file.toPath())
                        Timber.d("[${::deleteOldLogs.name}] Deleted log file: ${file.name}")
                    } catch (e: Exception){
                        if(!file.canWrite()){
                            Timber.e("[${::deleteOldLogs.name}] No permission to delete log file: ${file.name}")
                        }
                        Timber.e("[${::deleteOldLogs.name}] Error deleting log file: $e")
                    }
                }
            }
            return true
        } catch (exception: Exception) {
            Timber.e("[${::deleteOldLogs.name}] Error: $exception")
            return false
        }
    }
}