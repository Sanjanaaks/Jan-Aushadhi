package com.janaushadhi.finder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.janaushadhi.finder.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentEmail: String?
        get() = auth.currentUser?.email

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }.mapCatching { }

    suspend fun register(name: String, email: String, phone: String, password: String): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Unable to create user profile")
        val user = User(uid = uid, name = name, email = email, phone = phone, createdAt = Timestamp.now())
        firestore.collection("users").document(uid).set(user).await()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        if (email.isBlank()) {
            error("Email address is required")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error("Invalid email address format")
        }
        auth.sendPasswordResetEmail(email).await()
    }.mapCatching { }

    suspend fun getCurrentUser(): Result<User> = runCatching {
        val uid = currentUserId ?: error("User not signed in")
        val snapshot = firestore.collection("users").document(uid).get().await()
        snapshot.toObject(User::class.java)?.copy(uid = uid) ?: error("Profile not found")
    }

    suspend fun updateProfile(name: String, phone: String): Result<Unit> = runCatching {
        val uid = currentUserId ?: error("User not signed in")
        val updates = mapOf(
            "name" to name,
            "phone" to phone
        )
        firestore.collection("users").document(uid).update(updates).await()
    }

    fun logout() {
        auth.signOut()
    }

    fun friendlyAuthMessage(error: Throwable): String {
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        return when (code) {
            "ERROR_INVALID_EMAIL" -> "Invalid email"
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Wrong password"
            "ERROR_USER_NOT_FOUND" -> "User not found"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Please check your internet connection"
            else -> error.localizedMessage ?: "Something went wrong"
        }
    }
}
