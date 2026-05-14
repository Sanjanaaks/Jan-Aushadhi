package com.janaushadhi.finder.ui.reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.janaushadhi.finder.data.model.Prescription
import com.janaushadhi.finder.data.repository.AuthRepository
import com.janaushadhi.finder.worker.RefillReminderWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val analytics = Firebase.analytics

    private val _reminders = MutableLiveData<List<Prescription>>(emptyList())
    val reminders: LiveData<List<Prescription>> = _reminders

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadReminders() {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            runCatching {
                val snapshot = firestore.collection("users").document(uid).get().await()
                val raw = snapshot.get("prescriptions") as? List<Map<String, Any>> ?: emptyList()
                raw.map { it.toPrescription() }
            }.onSuccess { _reminders.value = it }
                .onFailure { _message.value = it.localizedMessage ?: "Unable to load reminders" }
        }
    }

    fun addReminder(context: android.content.Context, medicineName: String, qty: Int, refillDateMillis: Long) {
        val uid = authRepository.currentUserId ?: return
        val prescription = mapOf(
            "brandName" to medicineName,
            "genericName" to medicineName,
            "qty" to qty,
            "refillDate" to Timestamp(java.util.Date(refillDateMillis)),
            "brandPrice" to 0.0,
            "genericPrice" to 0.0,
            "enabled" to true
        )
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid).update("prescriptions", FieldValue.arrayUnion(prescription)).await()
            }.onSuccess {
                analytics.logEvent("reminder_set", android.os.Bundle().apply { putString("medicine_name", medicineName) })
                scheduleDailyWorker(context)
                _message.value = "Reminder saved"
                loadReminders()
            }.onFailure { _message.value = it.localizedMessage ?: "Unable to save reminder" }
        }
    }

    fun deleteReminder(prescription: Prescription) {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            val map = prescription.toMap()
            runCatching {
                firestore.collection("users").document(uid).update("prescriptions", FieldValue.arrayRemove(map)).await()
            }.onSuccess {
                _message.value = "Reminder deleted"
                loadReminders()
            }.onFailure { _message.value = it.localizedMessage ?: "Unable to delete reminder" }
        }
    }

    fun toggleReminder(prescription: Prescription, enabled: Boolean) {
        val uid = authRepository.currentUserId ?: return
        viewModelScope.launch {
            val oldMap = prescription.toMap()
            val newMap = prescription.copy(enabled = enabled).toMap()
            runCatching {
                val doc = firestore.collection("users").document(uid)
                doc.update("prescriptions", FieldValue.arrayRemove(oldMap)).await()
                doc.update("prescriptions", FieldValue.arrayUnion(newMap)).await()
            }.onSuccess {
                _message.value = if (enabled) "Reminder enabled" else "Reminder disabled"
                loadReminders()
            }.onFailure { _message.value = it.localizedMessage ?: "Unable to update reminder" }
        }
    }

    fun scheduleDailyWorker(context: android.content.Context) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val request = PeriodicWorkRequestBuilder<RefillReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(next.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "refill_reminders_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun Map<String, Any>.toPrescription(): Prescription = Prescription(
        brandName = this["brandName"] as? String ?: "",
        genericName = this["genericName"] as? String ?: "",
        qty = (this["qty"] as? Number)?.toInt() ?: 1,
        refillDate = this["refillDate"] as? Timestamp ?: Timestamp.now(),
        brandPrice = (this["brandPrice"] as? Number)?.toDouble() ?: 0.0,
        genericPrice = (this["genericPrice"] as? Number)?.toDouble() ?: 0.0,
        enabled = this["enabled"] as? Boolean ?: true
    )

    private fun Prescription.toMap(): Map<String, Any> = mapOf(
        "brandName" to brandName,
        "genericName" to genericName,
        "qty" to qty,
        "refillDate" to refillDate,
        "brandPrice" to brandPrice,
        "genericPrice" to genericPrice,
        "enabled" to enabled
    )
}
