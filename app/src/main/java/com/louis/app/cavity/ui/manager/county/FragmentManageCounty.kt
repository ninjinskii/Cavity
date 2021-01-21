package com.louis.app.cavity.ui.manager.county

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogConfirmDeleteBinding
import com.louis.app.cavity.databinding.FragmentManageCountyBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.showKeyboard

class FragmentManageCounty : Fragment(R.layout.fragment_manage_county) {
    private var _binding: FragmentManageCountyBinding? = null
    private val binding get() = _binding!!

    // TODO: Check VM scope carefully
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val countyAdapter = CountyRecyclerAdapter(
        onDragIconTouched = { vh: RecyclerView.ViewHolder -> requestDrag(vh) },
        onRename = { county: County -> showEditCountyDialog(county) },
        onDelete = { county: County -> showConfirmDeleteDialog(county) }
    )
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageCountyBinding.bind(view)

        // Ensuring we are scoping our VM to the good fragment
        L.v("${requireParentFragment()}")

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = countyAdapter
        }

        managerViewModel.getCountiesWithWines().observe(viewLifecycleOwner) {
            countyAdapter.setCounties(it)
        }

        val callback = CountyItemTouchHelperCallback(countyAdapter)

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        //binding.recyclerView.itemAnimator = null
        itemTouchHelper.startDrag(viewHolder)
    }

    private fun showEditCountyDialog(county: County) {
        SimpleInputDialog(requireContext(), layoutInflater).showForEdit(
            title = R.string.rename_county,
            hint = R.string.county,
            icon = null,
            editedString = county.name
        ) {
            val updatedCounty = county.copy(name = it)
            managerViewModel.updateCounty(updatedCounty)
        }
    }

    private fun showConfirmDeleteDialog(county: County) {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_county_warn)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                if (dialogBinding.countyName.text.toString() == county.name) {
                    managerViewModel.deleteCounty(county.countyId)
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
