package com.janaushadhi.finder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.janaushadhi.finder.data.model.Prescription
import com.janaushadhi.finder.utils.NotificationHelper
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class RefillReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        return try {
            val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            val rawPrescriptions = snapshot.get("prescriptions") as? List<Map<String, Any>> ?: emptyList()
            rawPrescriptions.map { map ->
                Prescription(
                    brandName = map["brandName"] as? String ?: "",
                    genericName = map["genericName"] as? String ?: "",
                    qty = (map["qty"] as? Number)?.toInt() ?: 1,
                    refillDate = map["refillDate"] as? Timestamp ?: Timestamp.now(),
                    brandPrice = (map["brandPrice"] as? Number)?.toDouble() ?: 0.0,
                    genericPrice = (map["genericPrice"] as? Number)?.toDouble() ?: 0.0,
                    enabled = map["enabled"] as? Boolean ?: true
                )
            }.filter { it.enabled && shouldNotify(it.refillDate) }
                .forEachIndexed { index, prescription ->
                    NotificationHelper.showRefillNotification(
                        applicationContext,
                        prescription.brandName.ifBlank { prescription.genericName },
                        prescription.brandName.hashCode() + index
                    )
                }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun shouldNotify(refillDate: Timestamp): Boolean {
        val now = System.currentTimeMillis()
        val refillMillis = refillDate.toDate().time
        val days = TimeUnit.MILLISECONDS.toDays(refillMillis - now)
        return days in 0..3
    }
}
