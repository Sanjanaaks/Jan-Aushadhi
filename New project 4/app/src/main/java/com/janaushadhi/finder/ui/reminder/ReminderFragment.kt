package com.janaushadhi.finder.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.janaushadhi.finder.R
import com.janaushadhi.finder.databinding.BottomSheetAddReminderBinding
import com.janaushadhi.finder.databinding.FragmentReminderBinding

class ReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ReminderAdapter(
            onToggle = { prescription, enabled -> viewModel.toggleReminder(prescription, enabled) },
            onDelete = { viewModel.deleteReminder(it) }
        )
        binding.reminderRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.reminderRecycler.adapter = adapter
        binding.addReminderFab.setOnClickListener { showAddReminderSheet() }
        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            adapter.submitList(reminders)
            binding.emptyState.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.message.observe(viewLifecycleOwner) { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        viewModel.loadReminders()
        viewModel.scheduleDailyWorker(requireContext())
    }

    private fun showAddReminderSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = BottomSheetAddReminderBinding.inflate(layoutInflater)
        var selectedDate = MaterialDatePicker.todayInUtcMilliseconds()
        val suggestions = listOf("Crocin", "Augmentin", "Ecosprin", "Metformin SR", "Pan-D", "Shelcal", "Atorva", "Gluconorm")
        sheetBinding.medicineInput.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions))
        sheetBinding.dateInput.setText(getString(R.string.select_refill_date))
        sheetBinding.dateInput.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_refill_date))
                .setSelection(selectedDate)
                .build()
                .apply {
                    addOnPositiveButtonClickListener {
                        selectedDate = it
                        sheetBinding.dateInput.setText(headerText)
                    }
                }
                .show(parentFragmentManager, "refill_date")
        }
        sheetBinding.saveButton.setOnClickListener {
            val medicine = sheetBinding.medicineInput.text?.toString().orEmpty().trim()
            val qty = sheetBinding.qtyInput.text?.toString()?.toIntOrNull() ?: 1
            if (medicine.isBlank()) {
                sheetBinding.medicineLayout.error = "Medicine name is required"
                return@setOnClickListener
            }
            viewModel.addReminder(requireContext(), medicine, qty, selectedDate)
            dialog.dismiss()
        }
        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
