package com.keciput.asrifa.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.repository.StoreInfoRepository
import com.keciput.asrifa.domain.model.StoreInfo
import com.keciput.asrifa.ui.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val storeInfoRepo: StoreInfoRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    fun checkStoreStatusAndNotify() {
        viewModelScope.launch {
            val storeInfo = storeInfoRepo.getStoreInfo().first() ?: return@launch
            
            // Cek apakah user mengizinkan notifikasi
            if (!storeInfo.isNotificationEnabled) return@launch

            val now = Calendar.getInstance()
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1 // 0=Minggu, 1=Senin...

            when {
                // 1. Cek apakah hari ini libur
                storeInfo.holidayDates.contains(todayDate) -> {
                    notificationHelper.showNotification(
                        "Keciput Asrifa Sedang Libur",
                        "Hari ini kami libur. ${storeInfo.holidayMessage.ifEmpty { "Buka kembali besok!" }}"
                    )
                }
                
                // 2. Cek apakah hari ini bukan hari operasional
                !storeInfo.operatingDays.contains(dayOfWeek) -> {
                    notificationHelper.showNotification(
                        "Toko Tutup",
                        "Hari ini kami tidak beroperasi. Sampai jumpa di hari kerja!"
                    )
                }

                // 3. Cek apakah sedang dalam jam buka
                currentTime >= storeInfo.openTime && currentTime < storeInfo.closeTime -> {
                    notificationHelper.showNotification(
                        "Toko Sudah Buka!",
                        "Keciput Asrifa sudah buka. Yuk mampir atau pesan lewat aplikasi! 🛍️"
                    )
                }

                // 4. Toko sudah tutup (di luar jam operasional)
                else -> {
                    notificationHelper.showNotification(
                        "Toko Sudah Tutup",
                        "Kami buka kembali besok pukul ${storeInfo.openTime} WIB."
                    )
                }
            }
        }
    }
}
