package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddCountyGrapeBinding
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.stepper.FragmentStepper
import com.louis.app.cavity.util.Event
import com.louis.app.cavity.util.showKeyboard
import com.louis.app.cavity.util.showSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: use material dialogs instead of text fields, same for expert advices
class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var grapeAdapter: GrapeRecyclerAdapter
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!
    private var totalGrapePercentage: Int? = null
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireGrapesBinding.bind(view)

        snackbarProvider = parentFragment as SnackbarProvider

        registerStepperWatcher()
        initRecyclerView()
        setListener()
        observe()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = validateGrapes()

            override fun onPageRequestAccepted() {
            }
        })

    }

    private fun initRecyclerView() {
        grapeAdapter = GrapeRecyclerAdapter(
            onDeleteListener = { addBottleViewModel.removeGrape(it) },
            onValueChangeListener = { addBottleViewModel.updateGrape(it) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = grapeAdapter
        }

        addBottleViewModel.grapes.observe(viewLifecycleOwner) {
            totalGrapePercentage = it.map { grape -> grape.percentage }.sum()

            // Using toMutableList() to change the list reference, otherwise our call submitList will be ignored
            grapeAdapter.submitList(it.toMutableList())
        }
    }
    private fun setListener() {
        binding.buttonAddGrape.setOnClickListener { showDialog() }
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }
    }

    private fun addGrape(grapeName: String) {
        if (grapeName.isEmpty()) {
            binding.coordinator.showSnackbar(R.string.empty_grape_name)
            return
        }

        if (grapeName == resources.getString(R.string.grape_other)) {
            binding.coordinator.showSnackbar(R.string.reserved_name)
            return
        }
        val defaultPercentage = if (grapeAdapter.currentList.size >= 1) 0 else 25

        addBottleViewModel.addGrape(grapeName, defaultPercentage)
    }

    private fun validateGrapes(): Boolean {
        return if (grapeAdapter.currentList.any { it.percentage == 0 }) {
            binding.coordinator.showSnackbar(R.string.empty_grape_percent)
            false
        } else {
            true
        }
    }

    private fun showDialog() {
        val dialogBinding = DialogAddCountyGrapeBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_grapes)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                addGrape(dialogBinding.countyName.text.toString())
            }
            .setView(dialogBinding.root)
            .show()

        lifecycleScope.launch(Main) {
            delay(300)
            dialogBinding.countyName.showKeyboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
