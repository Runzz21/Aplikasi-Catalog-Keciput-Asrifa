package com.keciput.asrifa.ui.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreStatusScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule() {
        val workRequest = PeriodicWorkRequestBuilder<StoreStatusWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "StoreStatusWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork("StoreStatusWork")
    }
}
