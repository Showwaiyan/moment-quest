package com.example.momentquest.ui.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.momentquest.R
import com.example.momentquest.databinding.BottomSheetUsabilityDashboardBinding
import com.example.momentquest.model.UsabilityResult
import com.example.momentquest.repository.UsabilityRepository
import com.example.momentquest.util.UsabilityTracker
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class UsabilityDashboardBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetUsabilityDashboardBinding? = null
    private val binding get() = _binding!!

    private val usabilityRepository = UsabilityRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetUsabilityDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupConfigControls()
        setupActionButtons()
        loadResults()
    }

    private fun setupConfigControls() {
        // Close button
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Active Tracking Switch
        binding.switchTracking.isChecked = UsabilityTracker.isTrackingEnabled(requireContext())
        binding.switchTracking.setOnCheckedChangeListener { _, isChecked ->
            UsabilityTracker.setTrackingEnabled(requireContext(), isChecked)
            val msg = if (isChecked) "Usability Tracking Session Started!" else "Usability Tracking Paused."
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // Active Variant Toggle
        val activeVariant = UsabilityTracker.getSelectedVariant(requireContext())
        if (activeVariant == "A") {
            binding.toggleVariantGroup.check(R.id.btnVariantA)
        } else {
            binding.toggleVariantGroup.check(R.id.btnVariantB)
        }

        binding.toggleVariantGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val variant = if (checkedId == R.id.btnVariantA) "A" else "B"
                UsabilityTracker.setSelectedVariant(requireContext(), variant)
                val variantLabel = if (variant == "A") "Variant A (Full Screen)" else "Variant B (Bottom Sheet)"
                Toast.makeText(requireContext(), "Switched to $variantLabel", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupActionButtons() {
        // Populate Demo Data
        binding.btnPopulateMock.setOnClickListener {
            lifecycleScope.launch {
                usabilityRepository.insertMockResults(requireContext())
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Populated counterbalanced mock data!", Toast.LENGTH_SHORT).show()
                    loadResults()
                }
            }
        }

        // Clear Data
        binding.btnClearData.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear Usability Data")
                .setMessage("Are you sure you want to delete all logged usability test runs?")
                .setPositiveButton("Clear") { _, _ ->
                    lifecycleScope.launch {
                        usabilityRepository.clearResults(requireContext())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "All logs cleared.", Toast.LENGTH_SHORT).show()
                            loadResults()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadResults() {
        lifecycleScope.launch {
            val results = usabilityRepository.getResults(requireContext())
            withContext(Dispatchers.Main) {
                updateMetricsAndTable(results)
            }
        }
    }

    private fun updateMetricsAndTable(results: List<UsabilityResult>) {
        val context = requireContext()

        // 1. Calculate Averages
        val resultsA = results.filter { it.variant == "A" }
        val resultsB = results.filter { it.variant == "B" }

        val avgTimeA = if (resultsA.isNotEmpty()) resultsA.map { it.timeMs }.average() else null
        val avgTimeB = if (resultsB.isNotEmpty()) resultsB.map { it.timeMs }.average() else null

        val avgEaseA = if (resultsA.isNotEmpty()) resultsA.map { it.easeRating }.average() else null
        val avgEaseB = if (resultsB.isNotEmpty()) resultsB.map { it.easeRating }.average() else null

        val totalErrorsA = resultsA.sumOf { it.errorCount }
        val totalErrorsB = resultsB.sumOf { it.errorCount }

        // Update Variant A Cards
        if (avgTimeA != null && avgEaseA != null) {
            binding.tvAvgTimeA.text = String.format(Locale.getDefault(), "Time: %.2fs", avgTimeA / 1000.0)
            binding.tvAvgEaseA.text = String.format(Locale.getDefault(), "Ease: %.1f ⭐ (Err: %d)", avgEaseA, totalErrorsA)
        } else {
            binding.tvAvgTimeA.text = "Time: --"
            binding.tvAvgEaseA.text = "Ease: --"
        }

        // Update Variant B Cards
        if (avgTimeB != null && avgEaseB != null) {
            binding.tvAvgTimeB.text = String.format(Locale.getDefault(), "Time: %.2fs", avgTimeB / 1000.0)
            binding.tvAvgEaseB.text = String.format(Locale.getDefault(), "Ease: %.1f ⭐ (Err: %d)", avgEaseB, totalErrorsB)
        } else {
            binding.tvAvgTimeB.text = "Time: --"
            binding.tvAvgEaseB.text = "Ease: --"
        }

        // 2. Dynamic Recommendation
        if (avgTimeA != null && avgTimeB != null && avgEaseA != null && avgEaseB != null) {
            val speedDiffPercent = ((avgTimeA - avgTimeB) / avgTimeA) * 100
            val easeDiff = avgEaseB - avgEaseA

            if (avgTimeB < avgTimeA) {
                // Variant B (Bottom Sheet) is faster
                binding.tvRecommendation.text = String.format(
                    Locale.getDefault(),
                    "Recommendation: Variant B (Bottom Sheet) is %.1f%% faster and rated %.1f points higher. Adopt Variant B!",
                    speedDiffPercent,
                    easeDiff
                )
            } else {
                // Variant A is faster
                binding.tvRecommendation.text = String.format(
                    Locale.getDefault(),
                    "Recommendation: Variant A (Full Screen) is %.1f%% faster and rated %.1f points higher. Adopt Variant A!",
                    -speedDiffPercent,
                    -easeDiff
                )
            }
        } else {
            binding.tvRecommendation.text = "Recommendation: Add usability logs to calculate the optimal capture variant."
        }

        // 3. Populate Table Layout
        // Clear all except the header row (index 0)
        val childCount = binding.tableResults.childCount
        if (childCount > 1) {
            binding.tableResults.removeViews(1, childCount - 1)
        }

        // Group results by participant
        val participants = results.map { it.participantName }.distinct()
        for (participant in participants) {
            val resultA = results.find { it.participantName == participant && it.variant == "A" }
            val resultB = results.find { it.participantName == participant && it.variant == "B" }

            val row = TableRow(context).apply {
                layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
                setPadding(0, 12, 0, 12)
            }

            // Col 1: Participant ID
            val tvName = createTableCell(participant, false)
            row.addView(tvName)

            // Col 2: Variant A Time
            val timeAText = if (resultA != null) String.format(Locale.getDefault(), "%.1fs", resultA.timeMs / 1000.0) else "-"
            row.addView(createTableCell(timeAText, false))

            // Col 3: Variant A Ease
            val easeAText = if (resultA != null) "${resultA.easeRating}⭐" else "-"
            row.addView(createTableCell(easeAText, false))

            // Col 4: Variant B Time
            val timeBText = if (resultB != null) String.format(Locale.getDefault(), "%.1fs", resultB.timeMs / 1000.0) else "-"
            row.addView(createTableCell(timeBText, true))

            // Col 5: Variant B Ease
            val easeBText = if (resultB != null) "${resultB.easeRating}⭐" else "-"
            row.addView(createTableCell(easeBText, true))

            binding.tableResults.addView(row)
        }
    }

    private fun createTableCell(text: String, isVariantB: Boolean): TextView {
        return TextView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
            this.text = text
            gravity = Gravity.CENTER
            textSize = 12f
            typeface = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.space_grotesk)
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isVariantB) R.color.primary else R.color.on_surface
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
