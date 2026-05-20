package com.vitalzen.ai.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
