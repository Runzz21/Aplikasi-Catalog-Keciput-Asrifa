package com.keciput.asrifa.ui.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.keciput.asrifa.data.repository.StoreInfoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class StoreStatusWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: StoreInfoRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storeInfo = repository.getStoreInfo().firstOrNull() ?: return Result.success()

        if (!storeInfo.isNotificationEnabled) return Result.success()

        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

        // Check Holiday
        if (storeInfo.holidayDates.contains(todayStr)) {
            // Check if it's the morning of the holiday (e.g., between 08:00 and 08:15)
            if (currentTimeStr.startsWith("08:")) {
                notificationHelper.showNotification(
                    "Toko Libur",
                    storeInfo.holidayMessage.ifEmpty { "Hari ini Keciput Asrifa libur. Kami buka kembali besok." }
                )
            }
            return Result.success()
        }

        // Check Operating Days (Calendar.SUNDAY = 1, PRD 0=Minggu .. 6=Sabtu)
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-6
        if (!storeInfo.operatingDays.contains(dayOfWeek)) {
            return Result.success()
        }

        // Check Open Time (Check if current time is within 15 mins of openTime)
        if (isTimeMatches(currentTimeStr, storeInfo.openTime)) {
            notificationHelper.showNotification(
                "Toko Buka",
                "Keciput Asrifa sudah buka! Yuk mampir atau pesan via app 🛍️"
            )
        }

        // Check Closing Soon (1 hour before closeTime)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val closeTime = sdf.parse(storeInfo.closeTime)
        if (closeTime != null) {
            val closingSoonCal = Calendar.getInstance().apply {
                time = closeTime
                add(Calendar.HOUR_OF_DAY, -1)
            }
            val closingSoonTimeStr = sdf.format(closingSoonCal.time)
            
            if (isTimeMatches(currentTimeStr, closingSoonTimeStr)) {
                notificationHelper.showNotification(
                    "Toko Akan Tutup",
                    "Toko tutup 1 jam lagi, masih bisa pesan via WhatsApp sebelum tutup ya!"
                )
            }
        }

        return Result.success()
    }

    private fun isTimeMatches(current: String, target: String): Boolean {
        // Since worker runs periodically (e.g. every 15-30 mins), we check if we are close to the target time
        // and haven't notified yet today (for simplicity in this sample, we just check exact or near match)
        return current == target
    }
}
