package com.louis.app.cavity.ui.manager.county

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogConfirmDeleteBinding
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.FragmentManager
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showKeyboard
import com.louis.app.cavity.util.showSnackbar

class FragmentManageCounty : Fragment(R.layout.fragment_manage_base) {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val countyAdapter = CountyRecyclerAdapter(
        onDragIconTouched = { vh: RecyclerView.ViewHolder -> requestDrag(vh) },
        onRename = { county: County -> showEditCountyDialog(county) },
        onDelete = { county: County -> showConfirmDeleteDialog(county) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)

        applyInsets()
        initRecyclerView()
        initEmptyState()
    }

    private fun applyInsets() {
        binding.coordinator.prepareWindowInsets { view, windowInsets, left, _, right, _ ->
            view.updatePadding(left = left, right = right)
            windowInsets
        }

        binding.recyclerView.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = countyAdapter
        }

        managerViewModel.getCountiesWithWines().observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            countyAdapter.setCounties(it)
        }

        val callback = CountyItemTouchHelperCallback(countyAdapter)

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun initEmptyState() {
        binding.emptyState.apply {
            setIcon(R.drawable.ic_bottle)
            setText(getString(R.string.empty_county))
            setActionText(getString(R.string.add_county))
            setOnActionClickListener {
                (parentFragment as? FragmentManager)?.showAddCountyDialog()
            }
        }
    }

    private fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    private fun showEditCountyDialog(county: County) {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.rename_county,
            hint = R.string.county
        ) {
            val updatedCounty = county.copy(name = it)
            managerViewModel.updateCounty(updatedCounty)
        }

        simpleInputDialog.showForEdit(dialogResources, county.name)
    }

    private fun showConfirmDeleteDialog(county: County) {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)

        LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
            .setTitle(R.string.warning)
            .setMessage(R.string.delete_county_warn)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                if (dialogBinding.countyName.text.toString() == county.name) {
                    managerViewModel.deleteCounty(county.id)
                } else {
                    binding.coordinator.showSnackbar(R.string.cannot_delete)
                }
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.countyName.post { dialogBinding.countyName.showKeyboard() }
    }

    override fun onPause() {
        super.onPause()
        managerViewModel.updateCounties(countyAdapter.getCounties())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
