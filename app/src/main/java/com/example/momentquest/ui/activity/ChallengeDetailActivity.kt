package com.example.momentquest.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.momentquest.MainActivity
import com.example.momentquest.R
import com.example.momentquest.databinding.ActivityChallengeDetailBinding
import com.example.momentquest.model.Challenge
import com.example.momentquest.model.Memory
import com.example.momentquest.ui.fragment.AddMemoryBottomSheet
import com.example.momentquest.viewmodel.ChallengeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeDetailBinding
    private val viewModel: ChallengeViewModel by viewModels()
    private var challengeId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        challengeId = intent.getStringExtra("challenge_id") ?: ""
        if (challengeId.isEmpty()) {
            Toast.makeText(this, "Challenge ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupButtons()
        observeViewModel()

        viewModel.loadChallengeDetails(challengeId)
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Challenge")
                .setMessage("Are you sure you want to delete this challenge and all of its memories?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteChallenge(challengeId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnEdit.setOnClickListener {
            val challenge = viewModel.challengeDetails.value
            if (challenge != null) {
                val intent = android.content.Intent(this, MainActivity::class.java).apply {
                    putExtra("edit_challenge_id", challenge.id)
                    putExtra("edit_challenge_title", challenge.title)
                    putExtra("edit_challenge_category", challenge.category)
                    putExtra("edit_challenge_deadline", challenge.deadline)
                    flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }
        }

        binding.btnMarkComplete.setOnClickListener {
            val bottomSheet = AddMemoryBottomSheet.newInstance(challengeId) {
                // Refresh details when marked complete
                viewModel.loadChallengeDetails(challengeId)
            }
            bottomSheet.show(supportFragmentManager, "AddMemoryBottomSheet")
        }

        binding.btnDeleteMemory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Memory")
                .setMessage("Are you sure you want to delete this memory? The challenge will return to pending.")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteMemory(challengeId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnShareMemory.setOnClickListener {
            Toast.makeText(this, "Share coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.challengeDetails.observe(this) { challenge ->
            challenge?.let { bindChallengeDetails(it) }
        }

        viewModel.memoriesList.observe(this) { memories ->
            if (memories.isNotEmpty()) {
                bindMemoryDetails(memories.first())
            }
        }

        viewModel.saveSuccess.observe(this) { success ->
            if (success) {
                val challenge = viewModel.challengeDetails.value
                if (challenge == null) {
                    Toast.makeText(this, "Challenge deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (challenge.status == "PENDING") {
                    Toast.makeText(this, "Memory deleted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Challenge completed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindChallengeDetails(challenge: Challenge) {
        binding.tvChallengeTitle.text = challenge.title
        binding.tvChallengeCategory.text = challenge.category.uppercase(Locale.getDefault())

        val categoryColorBg = when (challenge.category.lowercase(Locale.getDefault())) {
            "travel" -> R.color.category_travel_bg
            "learning" -> R.color.category_learning_bg
            "fitness" -> R.color.category_fitness_bg
            "social" -> R.color.category_social_bg
            "career" -> R.color.category_career_bg
            else -> R.color.category_others_bg
        }
        val categoryColorFg = when (challenge.category.lowercase(Locale.getDefault())) {
            "travel" -> R.color.category_travel_fg
            "learning" -> R.color.category_learning_fg
            "fitness" -> R.color.category_fitness_fg
            "social" -> R.color.category_social_fg
            "career" -> R.color.category_career_fg
            else -> R.color.category_others_fg
        }
        val categoryIcon = when (challenge.category.lowercase(Locale.getDefault())) {
            "travel" -> R.drawable.ic_flight
            "learning" -> R.drawable.ic_lightbulb
            "fitness" -> R.drawable.ic_fitness
            "social" -> R.drawable.ic_groups
            "career" -> R.drawable.ic_career
            else -> R.drawable.ic_others
        }
        binding.categoryTagLayout.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(getColor(categoryColorBg))
        )
        binding.ivCategoryIcon.setImageResource(categoryIcon)
        binding.ivCategoryIcon.setImageTintList(
            android.content.res.ColorStateList.valueOf(getColor(categoryColorFg))
        )
        binding.tvChallengeCategory.setTextColor(getColor(categoryColorFg))

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvSetDate.text = "Set on " + sdf.format(Date(challenge.createdAt))

        if (challenge.deadline != null) {
            binding.deadlineLayout.visibility = View.VISIBLE
            binding.tvDeadlineDate.text = "Due on " + sdf.format(Date(challenge.deadline))
        } else {
            binding.deadlineLayout.visibility = View.GONE
        }

        if (challenge.status == "COMPLETED") {
            binding.tvChallengeStatus.text = "Completed"
            binding.statusTagLayout.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.tertiary_fixed))
            )
            binding.tvChallengeStatus.setTextColor(getColor(R.color.on_tertiary_fixed_variant))

            // Hide pending views, show completed views
            binding.pendingSection.visibility = View.GONE
            binding.completedMemorySection.visibility = View.VISIBLE

            binding.btnMarkComplete.visibility = View.GONE
            binding.completedFooterButtons.visibility = View.VISIBLE
        } else {
            binding.tvChallengeStatus.text = "Pending"
            binding.statusTagLayout.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container_highest))
            )
            binding.tvChallengeStatus.setTextColor(getColor(R.color.on_surface_variant))

            // Hide completed views, show pending views
            binding.pendingSection.visibility = View.VISIBLE
            binding.completedMemorySection.visibility = View.GONE

            binding.btnMarkComplete.visibility = View.VISIBLE
            binding.completedFooterButtons.visibility = View.GONE
        }
    }

    private fun bindMemoryDetails(memory: Memory) {
        binding.tvMemoryNotes.text = memory.notes

        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvCompletedDate.text = "Completed on " + sdf.format(Date(memory.completedAt))

        if (!memory.photoUrl.isNullOrEmpty()) {
            binding.cardMemoryPhoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(memory.photoUrl)
                .into(binding.ivMemoryPhoto)
        } else {
            binding.cardMemoryPhoto.visibility = View.GONE
        }

        if (memory.latitude != null && memory.longitude != null) {
            binding.cardLocation.visibility = View.VISIBLE
            binding.tvLocationCoords.text = String.format(
                Locale.US,
                "%.4f° N, %.4f° E",
                memory.latitude,
                memory.longitude
            )
        } else {
            binding.cardLocation.visibility = View.GONE
        }
    }
}
