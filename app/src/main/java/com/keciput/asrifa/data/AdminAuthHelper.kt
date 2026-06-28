package com.keciput.asrifa.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminAuthHelper @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()

    suspend fun isAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return try {
            val result: GetTokenResult = user.getIdToken(true).await()
            result.claims["admin"] == true
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
}
