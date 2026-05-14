package com.janaushadhi.finder.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.janaushadhi.finder.databinding.FragmentSearchBinding
import com.janaushadhi.finder.ui.main.MainActivity

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: MedicineAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = MedicineAdapter(
            onAdd = { medicine ->
                viewModel.addToPrescription(medicine) { message ->
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                }
            },
            onFindStore = { medicine ->
                (activity as? MainActivity)?.openStoresForMedicine(medicine.brandName)
            }
        )
        binding.medicineRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.medicineRecycler.adapter = adapter

        binding.searchInput.doAfterTextChanged { viewModel.updateQuery(it?.toString().orEmpty()) }
        binding.categoryChips.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            viewModel.updateCategory(chip?.text?.toString() ?: "All")
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
            binding.emptyState.visibility = if (medicines.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.loadingView.visibility = if (state is SearchUiState.Loading) View.VISIBLE else View.GONE
            if (state is SearchUiState.Error) Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
