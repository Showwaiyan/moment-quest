package com.example.momentquest.ui.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.momentquest.databinding.FragmentAddChallengeBinding
import com.example.momentquest.viewmodel.ChallengeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddChallengeFragment : Fragment() {

    private var _binding: FragmentAddChallengeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChallengeViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    private var selectedDeadline: Long? = null
    private var challengeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengeId = arguments?.getString("challenge_id")

        setupSpinner()
        setupDatePicker()
        setupButtons()
        observeViewModel()

        if (!challengeId.isNullOrEmpty()) {
            binding.tvScreenTitle.text = "Edit Challenge"
            binding.btnSetChallenge.text = "Update Challenge"
            binding.etTitle.setText(arguments?.getString("title"))
            
            val category = arguments?.getString("category") ?: "Others"
            val categories = arrayOf("Travel", "Learning", "Fitness", "Social", "Career", "Others")
            val spinnerIndex = categories.indexOf(category).coerceAtLeast(0)
            binding.spinnerCategory.setSelection(spinnerIndex)
            
            val deadlineVal = arguments?.getLong("deadline", 0L) ?: 0L
            if (deadlineVal > 0L) {
                selectedDeadline = deadlineVal
                calendar.timeInMillis = deadlineVal
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.etDeadline.setText(sdf.format(calendar.time))
            }
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Travel", "Learning", "Fitness", "Social", "Career", "Others")
        val adapter = ArrayAdapter(requireContext(), com.example.momentquest.R.layout.spinner_item, categories)
        adapter.setDropDownViewResource(com.example.momentquest.R.layout.spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            
            selectedDeadline = calendar.timeInMillis
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.etDeadline.setText(sdf.format(calendar.time))
        }

        binding.etDeadline.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSetChallenge.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            if (!challengeId.isNullOrEmpty()) {
                viewModel.updateChallenge(challengeId!!, title, category, selectedDeadline)
            } else {
                viewModel.addChallenge(title, category, selectedDeadline)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val msg = if (!challengeId.isNullOrEmpty()) "Challenge updated successfully!" else "Challenge set successfully!"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSetChallenge.isEnabled = !isLoading
            binding.btnCancel.isEnabled = !isLoading
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

    companion object {
        fun newInstance(challengeId: String, title: String, category: String, deadline: Long?): AddChallengeFragment {
            return AddChallengeFragment().apply {
                arguments = Bundle().apply {
                    putString("challenge_id", challengeId)
                    putString("title", title)
                    putString("category", category)
                    if (deadline != null) {
                        putLong("deadline", deadline)
                    }
                }
            }
        }
    }
}
