package com.louis.app.cavity.ui.manager.grape

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.FragmentManager
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.setVisible

class FragmentManageGrape : Fragment(R.layout.fragment_manage_base) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)

        initRecyclerView()
        initEmptyState()
    }

    private fun initRecyclerView() {
        val grapeAdapter = GrapeRecyclerAdapter(
            onRename = { grape: Grape -> showEditGrapeDialog(grape) },
            onDelete = { grape: Grape -> showConfirmDeleteDialog(grape) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = grapeAdapter
        }

        managerViewModel.getGrapeWithQuantifiedGrapes().observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            grapeAdapter.submitList(it)
        }
    }

    private fun initEmptyState() {
        binding.emptyState.apply {
            setIcon(R.drawable.ic_grape)
            setText(getString(R.string.empty_grape_manager))
            setActionText(getString(R.string.add_grape))
            setOnActionClickListener {
                (parentFragment as? FragmentManager)?.showAddGrapeDialog()
            }
        }
    }

    private fun showEditGrapeDialog(grape: Grape) {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.rename_grape,
            hint = R.string.grape_name,
            icon = R.drawable.ic_grape
        ) {
            val updatedGrape = grape.copy(name = it)
            managerViewModel.updateGrape(updatedGrape)
        }

        simpleInputDialog.show(dialogResources)
    }

    private fun showConfirmDeleteDialog(grape: Grape) {
        LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
            .setMessage(R.string.confirm_grape_delete)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                managerViewModel.deleteGrape(grape)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
