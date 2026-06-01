package com.codedab.medsymptom.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.codedab.medsymptom.data.local.dao.SymptomDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val symptomDao: SymptomDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val unsynced = symptomDao.getUnsyncedSymptoms()
            for (entity in unsynced) {
                symptomDao.markSynced(entity.id)
            }
            // BUG 4a: returns success in catch — should be failure
            Result.success()
        } catch (e: Exception) {
            Result.success() // BUG 4a: should be Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "MedSymptomSyncWorker"

        fun buildPeriodicRequest(): PeriodicWorkRequest {
            // BUG 4b: no network constraint — NetworkType.CONNECTED is missing
            return PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .build()
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // BUG 4c: uses REPLACE instead of KEEP — kills existing work
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                SyncWorker.buildPeriodicRequest(),
            )
        }
    }
}
