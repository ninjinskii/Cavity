package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireGrapesBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.QuantifiedGrapeAndGrape
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentInquireGrapes : Fragment(R.layout.fragment_inquire_grapes) {
    private lateinit var quantifiedGrapeAdapter: QuantifiedGrapeRecyclerAdapter
    private var _binding: FragmentInquireGrapesBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private val grapeViewModel: GrapeViewModel by viewModels()
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
                    .setTitle(R.string.add_grapes)
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
        binding.buttonAddGrape.setOnClickListener { grapeViewModel.requestGrapeDialog() }

        binding.buttonCreateGrape.setOnClickListener {
            grapeViewModel.insertGrape(Grape(grapeId = 0, name = binding.grapeName.text.toString()))
        }
    }

    private fun addGrape(grapeName: String) {
        if (grapeName == resources.getString(R.string.grape_other)) {
            binding.coordinator.showSnackbar(R.string.reserved_name)
            return
        }

        grapeViewModel.insertGrape(Grape(0, grapeName))
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
