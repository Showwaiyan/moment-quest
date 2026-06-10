package com.example.momentquest.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.momentquest.databinding.FragmentStatsBinding
import com.example.momentquest.viewmodel.StatsViewModel
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Settings option coming soon!", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    private fun observeViewModel() {
        viewModel.totalChallenges.observe(viewLifecycleOwner) { total ->
            binding.tvTotalChallenges.text = total.toString()
            updateCompletionRatioText()
        }

        viewModel.completedChallenges.observe(viewLifecycleOwner) { completed ->
            updateCompletionRatioText()
        }

        viewModel.completionRate.observe(viewLifecycleOwner) { rate ->
            binding.progressBarCompletion.progress = rate
            binding.tvCompletionPercentage.text = String.format(Locale.getDefault(), "%d%%", rate)
        }

        viewModel.totalMoments.observe(viewLifecycleOwner) { totalMoments ->
            binding.tvTotalMoments.text = totalMoments.toString()
        }

        viewModel.categoryBreakdown.observe(viewLifecycleOwner) { breakdown ->
            // Travel category
            breakdown["Travel"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvTravelStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressTravel.progress = percentage
            }

            // Learning category
            breakdown["Learning"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvLearningStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressLearning.progress = percentage
            }

            // Fitness category
            breakdown["Fitness"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvFitnessStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressFitness.progress = percentage
            }

            // Social category
            breakdown["Social"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvSocialStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressSocial.progress = percentage
            }

            // Career category
            breakdown["Career"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvCareerStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressCareer.progress = percentage
            }

            // Others category
            breakdown["Others"]?.let { (completed, total) ->
                val percentage = if (total > 0) (completed * 100) / total else 0
                binding.tvOthersStats.text = String.format(Locale.getDefault(), "%d / %d Completed (%d%%)", completed, total, percentage)
                binding.progressOthers.progress = percentage
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateCompletionRatioText() {
        val total = viewModel.totalChallenges.value ?: 0
        val completed = viewModel.completedChallenges.value ?: 0
        binding.tvCompletionRatioText.text = String.format(
            Locale.getDefault(),
            "%d of %d challenges completed",
            completed,
            total
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
