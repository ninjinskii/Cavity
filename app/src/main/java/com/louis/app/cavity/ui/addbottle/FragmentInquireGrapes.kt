package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddGrapeBinding
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.ui.addbottle.adapter.QuantifiedGrapeRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.viewmodel.GrapeViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showKeyboard

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var stepperx: Stepper
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val grapeViewModel: GrapeViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireGrapesBinding.bind(view)

        stepperx = parentFragment as Stepper
        grapeViewModel.start(addBottleViewModel.bottleId)

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val quantifiedGrapeAdapter = QuantifiedGrapeRecyclerAdapter(
            onDeleteListener = { grapeViewModel.removeQuantifiedGrape(it.qGrape) },
            onValueChangeListener = { qGrapeAndGrape, newValue ->
                grapeViewModel.updateQuantifiedGrape(qGrapeAndGrape.qGrape, newValue)
            },
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = quantifiedGrapeAdapter
        }

        val bottleId = addBottleViewModel.bottleId

        grapeViewModel.getQGrapesAndGrapeForBottle(bottleId).observe(viewLifecycleOwner) {
            toggleRvPlaceholder(it.isEmpty())
            quantifiedGrapeAdapter.submitList(it)
        }
    }

    private fun observe() {
        grapeViewModel.grapeDialogEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { checkableGrapes ->
                val copy = checkableGrapes.map { it.copy() }.toMutableList()
                val names = checkableGrapes.map { it.grape.name }.toTypedArray()
                val bool = checkableGrapes.map { it.isChecked }.toBooleanArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.select_grapes)
                    .setMultiChoiceItems(names, bool) { _, pos, checked ->
                        copy[pos].isChecked = checked
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.submit) { _, _ ->
                        grapeViewModel.submitCheckedGrapes(copy)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun setListeners() {
        with(binding) {
            buttonSelectGrape.setOnClickListener { grapeViewModel.requestGrapeDialog() }
            buttonSelectGrapeSecondary.setOnClickListener { grapeViewModel.requestGrapeDialog() }
            buttonAddGrape.setOnClickListener { showAddGrapeDialog() }
            buttonSkip.setOnClickListener { stepperx.requestNextPage() }
            stepper.next.setOnClickListener { stepperx.requestNextPage() }
            stepper.previous.setOnClickListener { stepperx.requestPreviousPage() }
        }
    }

    private fun showAddGrapeDialog() {
        val dialogBinding = DialogAddGrapeBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_grape)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.grapeName.text.toString()
                grapeViewModel.insertGrape(name)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.grapeName.post { dialogBinding.grapeName.showKeyboard() }
    }

    private fun toggleRvPlaceholder(toggle: Boolean) {
        with(binding) {
            grapeIconEmpty.setVisible(toggle)
            explanation.setVisible(toggle)
            buttonSelectGrapeSecondary.setVisible(toggle)
            buttonSkip.setVisible(toggle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
