package com.keciput.asrifa.ui.home

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.keciput.asrifa.R
import com.keciput.asrifa.data.local.DatabaseSeeder
import com.keciput.asrifa.data.local.UserPreferencesRepository
import com.keciput.asrifa.data.repository.CartRepository
import com.keciput.asrifa.data.repository.CategoryRepository
import com.keciput.asrifa.data.repository.StoreInfoRepository
import com.keciput.asrifa.domain.model.Banner
import com.keciput.asrifa.domain.model.CartItem
import com.keciput.asrifa.domain.model.Category
import com.keciput.asrifa.domain.model.Snack
import com.keciput.asrifa.domain.model.StoreInfo
import com.keciput.asrifa.domain.usecase.GetFeaturedSnacksUseCase
import com.keciput.asrifa.domain.usecase.GetFlashSaleSnacksUseCase
import com.keciput.asrifa.domain.usecase.GetPopularSnacksUseCase
import com.keciput.asrifa.ui.notification.StoreStatusWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TimerState(
    val hour: String = "00",
    val minute: String = "00",
    val second: String = "00"
)

data class HomeUiState(
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val featuredSnacks: List<Snack> = emptyList(),
    val popularSnacks: List<Snack> = emptyList(),
    val flashSaleSnacks: List<Snack> = emptyList(),
    val selectedCategory: String = "Semua",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val storeInfo: StoreInfo = StoreInfo()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeaturedSnacksUseCase: GetFeaturedSnacksUseCase,
    private val getPopularSnacksUseCase: GetPopularSnacksUseCase,
    private val getFlashSaleSnacksUseCase: GetFlashSaleSnacksUseCase,
    private val categoryRepo: CategoryRepository,
    private val cartRepo: CartRepository,
    private val storeInfoRepo: StoreInfoRepository,
    private val seeder: DatabaseSeeder,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var countDownTimer: CountDownTimer? = null

    init {
        observeStoreInfo()
        refreshData()
        startFlashSaleTimer()
    }

    private fun observeStoreInfo() {
        viewModelScope.launch {
            storeInfoRepo.getStoreInfo().collect { info ->
                if (info != null) {
                    _uiState.update { it.copy(storeInfo = info) }
                }
            }
        }
    }

    fun toggleNotification() {
        viewModelScope.launch {
            val currentInfo = _uiState.value.storeInfo
            val newValue = !currentInfo.isNotificationEnabled
            storeInfoRepo.saveStoreInfo(currentInfo.copy(isNotificationEnabled = newValue))
            
            if (newValue) {
                scheduleStoreStatusWorker()
            } else {
                cancelStoreStatusWorker()
            }
        }
    }

    private fun scheduleStoreStatusWorker() {
        val workRequest = PeriodicWorkRequestBuilder<StoreStatusWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "StoreStatusWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cancelStoreStatusWorker() {
        WorkManager.getInstance(context).cancelUniqueWork("StoreStatusWork")
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isRefreshing = true) }
            
            try {
                // Pastikan database terisi
                seeder.seedIfEmpty()

                // Dummy Banners
                val dummyBanners = listOf(
                    Banner(1, "Flash Sale", "Promo Flash Sale Akhir Bulan", "PROMO50", R.drawable.flashsale),
                    Banner(2, "Produk Baru", "Cicipi kelezatan baru", "NEW", R.drawable.produkterbaru),
                    Banner(3, "Gratis Ongkir", "Khusus area Mojokerto", "FREE", R.drawable.screen)
                )

                // Gabungkan semua aliran data
                combine(
                    getFeaturedSnacksUseCase(),
                    getPopularSnacksUseCase(),
                    getFlashSaleSnacksUseCase(),
                    categoryRepo.getAllCategories()
                ) { featured, popular, flash, categories ->
                    val allCategory = Category(id = "all", name = "Semua", iconUrl = "")
                    _uiState.update { it.copy(
                        banners = dummyBanners,
                        featuredSnacks = featured,
                        popularSnacks = popular,
                        flashSaleSnacks = flash,
                        categories = listOf(allCategory) + categories,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    ) }
                }.collect()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Gagal memuat data: ${e.message}"
                ) }
            }
        }
    }

    fun addToCart(snack: Snack) {
        viewModelScope.launch {
            val cartItem = CartItem(
                id = UUID.randomUUID().toString(),
                snackId = snack.id,
                snackName = snack.name,
                imageUrl = snack.imageUrl,
                quantity = 1,
                pricePerUnit = snack.price
            )
            cartRepo.addToCart(cartItem)
        }
    }

    private fun startFlashSaleTimer() {
        val totalMillis = TimeUnit.HOURS.toMillis(3)
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val h = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val m = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val s = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                _timerState.value = TimerState(
                    hour = String.format("%02d", h),
                    minute = String.format("%02d", m),
                    second = String.format("%02d", s)
                )
            }
            override fun onFinish() {
                _timerState.value = TimerState("00", "00", "00")
            }
        }.start()
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
