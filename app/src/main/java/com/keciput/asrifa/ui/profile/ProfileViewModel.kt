package com.keciput.asrifa.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keciput.asrifa.data.local.UserPreferencesRepository
import com.keciput.asrifa.data.repository.AuthRepository
import com.keciput.asrifa.data.repository.StoreInfoRepository
import com.keciput.asrifa.domain.model.StoreInfo
import com.keciput.asrifa.ui.notification.StoreStatusScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val storeInfo: StoreInfo = StoreInfo(),
    val appVersion: String = "1.0.0",
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val storeInfoRepo: StoreInfoRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepo: UserPreferencesRepository,
    private val scheduler: StoreStatusScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
        scheduleStoreStatusWorker()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val firebaseUser = authRepository.currentUser
            val prefsUser = userPreferencesRepo.userDataFlow.first()

            val name = firebaseUser?.displayName ?: prefsUser.name
            val email = firebaseUser?.email ?: prefsUser.email

            _uiState.update {
                it.copy(
                    isLoggedIn = authRepository.isLoggedIn(),
                    userName = name,
                    userEmail = email
                )
            }

            storeInfoRepo.getStoreInfo().collect { info ->
                if (info != null) {
                    _uiState.update { it.copy(
                        storeInfo = info,
                        appVersion = getAppVersion(),
                        isLoading = false
                    ) }
                }
            }
        }
    }

    fun refreshAuthState() {
        viewModelScope.launch {
            val firebaseUser = authRepository.currentUser
            val prefsUser = userPreferencesRepo.userDataFlow.first()
            _uiState.update {
                it.copy(
                    isLoggedIn = authRepository.isLoggedIn(),
                    userName = firebaseUser?.displayName ?: prefsUser.name,
                    userEmail = firebaseUser?.email ?: prefsUser.email
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userPreferencesRepo.clearUserData()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    userName = "",
                    userEmail = ""
                )
            }
        }
    }

    fun onNotificationToggle(enabled: Boolean) {
        viewModelScope.launch {
            val currentInfo = _uiState.value.storeInfo
            storeInfoRepo.saveStoreInfo(currentInfo.copy(isNotificationEnabled = enabled))

            if (enabled) {
                scheduleStoreStatusWorker()
            } else {
                cancelStoreStatusWorker()
            }
        }
    }

    private fun scheduleStoreStatusWorker() {
        scheduler.schedule()
    }

    private fun cancelStoreStatusWorker() {
        scheduler.cancel()
    }

    private fun getAppVersion(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}
