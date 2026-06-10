package com.example.momentquest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momentquest.databinding.FragmentTimelineBinding
import com.example.momentquest.ui.adapter.TimelineAdapter
import com.example.momentquest.viewmodel.TimelineViewModel

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var adapter: TimelineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTimeline()
    }

    private fun setupRecyclerView() {
        adapter = TimelineAdapter(
            onMarkDoneClick = { challengeId ->
                val bottomSheet = AddMemoryBottomSheet.newInstance(challengeId) {
                    viewModel.loadTimeline()
                }
                bottomSheet.show(parentFragmentManager, "AddMemoryBottomSheet")
            },
            onMomentClick = { moment ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Moment")
                    .setMessage("Are you sure you want to delete this moment?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteMoment(moment.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.recyclerViewTimeline.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTimeline.adapter = adapter
    }

    fun filterTimeline(query: String) {
        viewModel.loadTimeline(query = query)
    }

    private fun setupFilters() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                com.example.momentquest.R.id.chipCatTravel -> "Travel"
                com.example.momentquest.R.id.chipCatLearning -> "Learning"
                com.example.momentquest.R.id.chipCatFitness -> "Fitness"
                com.example.momentquest.R.id.chipCatSocial -> "Social"
                else -> "All"
            }
            viewModel.loadTimeline(category = category)
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                com.example.momentquest.R.id.chipStatusPending -> "PENDING"
                com.example.momentquest.R.id.chipStatusCompleted -> "COMPLETED"
                else -> "ALL"
            }
            viewModel.loadTimeline(status = status)
        }
    }

    private fun observeViewModel() {
        viewModel.timelineItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            if (items.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
