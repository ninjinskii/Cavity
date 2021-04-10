package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.addbottle.adapter.QuantifiedGrapeRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.GrapeManager
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var stepperx: Stepper
    private lateinit var grapeManager: GrapeManager
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireGrapesBinding.bind(view)

        stepperx = parentFragment as Stepper
        grapeManager = addBottleViewModel.grapeManager

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val quantifiedGrapeAdapter = QuantifiedGrapeRecyclerAdapter(
            onDeleteListener = { grapeManager.removeQuantifiedGrape(it) },
            onValueChangeListener = { qGrape, newValue ->
                grapeManager.updateQuantifiedGrape(qGrape, newValue)
            },
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = quantifiedGrapeAdapter
        }

        grapeManager.qGrapes.observe(viewLifecycleOwner) {
            L.v("grpae list size: ${it.size}")
            toggleRvPlaceholder(it.isEmpty())
            quantifiedGrapeAdapter.submitList(it)
            lifecycleScope.launch(Main) {
                delay(700)
                quantifiedGrapeAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observe() {
        grapeManager.grapeDialogEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { checkableGrapes ->
                val copy = checkableGrapes.map { it.copy() }.toMutableList()
                val names = checkableGrapes.map { it.name }.toTypedArray()
                val bool = checkableGrapes.map { it.isChecked }.toBooleanArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.select_grapes)
                    .setMultiChoiceItems(names, bool) { _, pos, checked ->
                        copy[pos].isChecked = checked
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.submit) { _, _ ->
                        grapeManager.submitCheckedGrapes(copy)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private fun setListeners() {
        with(binding) {
            buttonSelectGrape.setOnClickListener { grapeManager.requestGrapeDialog() }
            buttonSelectGrapeSecondary.setOnClickListener { grapeManager.requestGrapeDialog() }
            buttonAddGrape.setOnClickListener { showAddGrapeDialog() }
            buttonSkip.setOnClickListener { stepperx.requestNextPage() }
            stepper.next.setOnClickListener { stepperx.requestNextPage() }
            stepper.previous.setOnClickListener { stepperx.requestPreviousPage() }
        }
    }

    private fun showAddGrapeDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_grape,
            hint = R.string.grape_name,
            icon = R.drawable.ic_grape
        ) {
            grapeManager.addGrapeAndQGrape(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
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
