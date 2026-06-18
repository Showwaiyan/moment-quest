package com.example.momentquest.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.momentquest.databinding.DialogUsabilityFeedbackBinding
import com.example.momentquest.model.UsabilityResult
import com.example.momentquest.repository.UsabilityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object UsabilityTracker {
    private const val PREFS_NAME = "usability_prefs"
    private const val KEY_TRACKING_ENABLED = "tracking_enabled"
    private const val KEY_SELECTED_VARIANT = "selected_variant"
    private const val KEY_START_TIME = "start_time"

    fun isTrackingEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_TRACKING_ENABLED, false)
    }

    fun setTrackingEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_TRACKING_ENABLED, enabled).apply()
    }

    fun getSelectedVariant(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_VARIANT, "A") ?: "A"
    }

    fun setSelectedVariant(context: Context, variant: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_VARIANT, variant).apply()
    }

    fun recordStartTime(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_START_TIME, System.currentTimeMillis()).apply()
    }

    fun getElapsedTimeMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val startTime = prefs.getLong(KEY_START_TIME, 0L)
        if (startTime == 0L) return 0L
        return System.currentTimeMillis() - startTime
    }

    fun showFeedbackDialog(
        activity: Activity,
        variant: String,
        timeMs: Long,
        coroutineScope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        val binding = DialogUsabilityFeedbackBinding.inflate(LayoutInflater.from(activity))
        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        val seconds = timeMs / 1000.0
        binding.tvDuration.text = String.format("Time taken: %.2f seconds", seconds)

        // Generate proposed Participant ID
        coroutineScope.launch {
            val repository = UsabilityRepository()
            val results = repository.getResults(activity)
            val participantIds = results.map { it.participantName }.distinct()
            val nextNum = participantIds.size + 1
            val proposedName = "P$nextNum"

            withContext(Dispatchers.Main) {
                binding.etParticipantName.setText(proposedName)
            }
        }

        binding.btnSkip.setOnClickListener {
            dialog.dismiss()
            onComplete()
        }

        binding.btnSaveLog.setOnClickListener {
            val participantName = binding.etParticipantName.text.toString().trim()
            if (participantName.isEmpty()) {
                Toast.makeText(activity, "Please enter a participant ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val easeRating = when (binding.chipGroupEase.checkedChipId) {
                com.example.momentquest.R.id.chipEase1 -> 1
                com.example.momentquest.R.id.chipEase2 -> 2
                com.example.momentquest.R.id.chipEase3 -> 3
                com.example.momentquest.R.id.chipEase5 -> 5
                else -> 4
            }

            val errorCount = when (binding.chipGroupErrors.checkedChipId) {
                com.example.momentquest.R.id.chipError1 -> 1
                com.example.momentquest.R.id.chipError2 -> 2
                com.example.momentquest.R.id.chipError3Plus -> 3
                else -> 0
            }

            val usabilityResult = UsabilityResult(
                id = UUID.randomUUID().toString(),
                participantName = participantName,
                variant = variant,
                timeMs = timeMs,
                easeRating = easeRating,
                errorCount = errorCount,
                timestamp = System.currentTimeMillis()
            )

            coroutineScope.launch {
                val repository = UsabilityRepository()
                repository.addResult(activity, usabilityResult)
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Result logged successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    onComplete()
                }
            }
        }

        dialog.show()
    }
}
