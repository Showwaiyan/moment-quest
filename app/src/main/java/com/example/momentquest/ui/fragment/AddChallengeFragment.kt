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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupDatePicker()
        setupButtons()
        observeViewModel()
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
            viewModel.addChallenge(title, category, selectedDeadline)
        }
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Challenge set successfully!", Toast.LENGTH_SHORT).show()
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
}
