package com.janaushadhi.finder.ui.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.janaushadhi.finder.R
import com.janaushadhi.finder.data.model.Prescription
import com.janaushadhi.finder.databinding.ItemReminderBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderAdapter(
    private val onToggle: (Prescription, Boolean) -> Unit,
    private val onDelete: (Prescription) -> Unit
) : ListAdapter<Prescription, ReminderAdapter.ReminderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(prescription: Prescription) = with(binding) {
            val context = root.context
            val days = TimeUnit.MILLISECONDS.toDays(prescription.refillDate.toDate().time - System.currentTimeMillis())
            medicineNameText.text = prescription.brandName
            refillDateText.text = "Next refill: ${dateFormat.format(prescription.refillDate.toDate())}"
            daysChip.text = when {
                days < 0 -> "Due now"
                days == 0L -> "Today"
                else -> "$days days"
            }
            val chipColor = when {
                days < 3 -> R.color.error_red
                days <= 7 -> R.color.accent_orange
                else -> R.color.savings_green
            }
            daysChip.setChipBackgroundColorResource(chipColor)
            daysChip.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            enableSwitch.setOnCheckedChangeListener(null)
            enableSwitch.isChecked = prescription.enabled
            enableSwitch.setOnCheckedChangeListener { _, checked -> onToggle(prescription, checked) }
            deleteButton.setOnClickListener { onDelete(prescription) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Prescription>() {
        override fun areItemsTheSame(oldItem: Prescription, newItem: Prescription): Boolean =
            oldItem.brandName == newItem.brandName && oldItem.refillDate == newItem.refillDate

        override fun areContentsTheSame(oldItem: Prescription, newItem: Prescription): Boolean = oldItem == newItem
    }

    companion object {
        private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
    }
}
