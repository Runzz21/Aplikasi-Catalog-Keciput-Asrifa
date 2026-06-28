package com.keciput.asrifa.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.local.DatabaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val seeder: DatabaseSeeder
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _loadingText = MutableStateFlow("Menyiapkan katalog...")
    val loadingText: StateFlow<String> = _loadingText.asStateFlow()

    init {
        viewModelScope.launch {
            // Update loading text
            launch {
                delay(700)
                _loadingText.value = "Memuat data..."
                delay(700)
                _loadingText.value = "Sebentar lagi..."
            }

            val seedJob = async { seeder.seedIfEmpty() }
            val delayJob = async { delay(2000L) }

            seedJob.await()
            delayJob.await()
            _isReady.value = true
        }
    }
}
