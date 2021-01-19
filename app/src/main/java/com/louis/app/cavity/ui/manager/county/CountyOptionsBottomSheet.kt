package com.louis.app.cavity.ui.manager.county

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetCountyOptionsBinding
import com.louis.app.cavity.databinding.DialogAddCountyBinding
import com.louis.app.cavity.databinding.DialogConfirmDeleteBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.showKeyboard

class CountyOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCountyOptionsBinding? = null
    private val binding get() = _binding!!
    private val args: CountyOptionsBottomSheetArgs by navArgs()
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCountyOptionsBinding.inflate(inflater)

        // Ensuring we are scoping our VM to the good fragment
        //L.v("${requireParentFragment().requireParentFragment()}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            countyName.text = args.countyName
            bottleCount.text = args.bottlesCount.toString()

            editCounty.setOnClickListener {
                showEditCountyDialog()
            }

            deleteCounty.setOnClickListener {
                showConfirmDeleteDialog()
            }
        }
    }

    private fun showEditCountyDialog() {
        val dialogBinding = DialogAddCountyBinding.inflate(layoutInflater)
        dialogBinding.countyName.setText(args.countyName)
        dialogBinding.countyName.setSelection(args.countyName.length)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_county)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.countyName.text.toString()
                val county = County(args.countyId, name, args.countyPrefOrder)
                managerViewModel.updateCounty(county)
                this@CountyOptionsBottomSheet.dismiss()
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.countyName.post { dialogBinding.countyName.showKeyboard() }
    }

    private fun showConfirmDeleteDialog() {
        val dialogBinding = DialogConfirmDeleteBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_county_short)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.delete) { _, _ ->
                if (dialogBinding.countyName.text.toString() == args.countyName) {
                    managerViewModel.deleteCounty(args.countyId)
                }

                this@CountyOptionsBottomSheet.dismiss()
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.countyName.post { dialogBinding.countyName.showKeyboard() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}