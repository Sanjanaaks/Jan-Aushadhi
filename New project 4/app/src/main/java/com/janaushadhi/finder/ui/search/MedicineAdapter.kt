package com.janaushadhi.finder.ui.search

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.janaushadhi.finder.R
import com.janaushadhi.finder.data.model.Medicine
import com.janaushadhi.finder.databinding.ItemMedicineBinding

class MedicineAdapter(
    private val onAdd: (Medicine) -> Unit,
    private val onFindStore: (Medicine) -> Unit
) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicineViewHolder(private val binding: ItemMedicineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medicine: Medicine) = with(binding) {
            brandNameText.text = medicine.brandName
            genericNameText.text = medicine.genericName
            categoryText.text = medicine.category
            brandPriceText.text = "₹${medicine.brandPrice.toInt()}"
            brandPriceText.paintFlags = brandPriceText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            genericPriceText.text = "₹${medicine.genericPrice.toInt()}"
            savingsText.text = "You save ₹${medicine.savingsAmount} (${medicine.savingsPercent}%)"
            savingsBadge.text = "${medicine.savingsPercent}%"
            medicineIcon.setColorFilter(ContextCompat.getColor(root.context, categoryColor(medicine.category)))
            addPrescriptionButton.setOnClickListener { onAdd(medicine) }
            findStoreButton.setOnClickListener { onFindStore(medicine) }
        }
    }

    private fun categoryColor(category: String): Int = when (category) {
        "Antibiotic" -> R.color.accent_orange
        "Painkiller" -> R.color.error_red
        "Cardiac" -> R.color.primary_green_dark
        "Diabetes" -> R.color.savings_green
        "Gastro" -> R.color.text_secondary
        else -> R.color.primary_green
    }

    object DiffCallback : DiffUtil.ItemCallback<Medicine>() {
        override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean = oldItem == newItem
    }
}
