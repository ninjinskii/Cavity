package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddGrapeBinding
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showKeyboard
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var quantifiedGrapeAdapter: QuantifiedGrapeRecyclerAdapter
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!

    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val grapeViewModel: GrapeViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var bottleId = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireGrapesBinding.bind(view)

        bottleId = addBottleViewModel.bottleId
        grapeViewModel.start(bottleId)

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        lifecycleScope.launch(IO) {
            quantifiedGrapeAdapter = QuantifiedGrapeRecyclerAdapter(
                onDeleteListener = { grapeViewModel.removeQuantifiedGrape(it.qGrape) },
                onValueChangeListener = { qGrapeAndGrape, newValue ->
                    grapeViewModel.updateQuantifiedGrape(qGrapeAndGrape.qGrape, newValue)
                },
            )

            withContext(Main) {
                binding.recyclerView.apply {
                    layoutManager = LinearLayoutManager(activity)
                    setHasFixedSize(true)
                    adapter = quantifiedGrapeAdapter
                }
            }
        }

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
                    .show()
            }
        }
    }

    private fun setListeners() {
        binding.buttonSelectGrape.setOnClickListener { grapeViewModel.requestGrapeDialog() }

        binding.buttonAddGrape.setOnClickListener { showAddGrapeDialog() }
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
            .setCancelable(false)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.grapeName.post { dialogBinding.grapeName.showKeyboard() }
    }

    private fun toggleRvPlaceholder(toggle: Boolean) {
        with(binding) {
            grapeIconEmpty.setVisible(toggle)
            explanation.setVisible(toggle)
            buttonAddGrapeSecondary.setVisible(toggle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
