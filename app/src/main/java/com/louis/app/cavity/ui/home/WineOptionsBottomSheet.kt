package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetWineOptionsBinding
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineOptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetWineOptionsBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetWineOptionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editWine = homeViewModel.editWine ?: return
        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.colorAccent)
            )
        } ?: return

        with(binding) {
            currentWine.wineName.text = editWine.name
            currentWine.wineNaming.text = editWine.naming
            currentWine.wineColorIndicator.setColorFilter(colors[editWine.color])
            currentWine.organicImage.setVisible(editWine.isOrganic.toBoolean())

            addBottle.setOnClickListener {
                dismiss()
                findNavController().navigate(R.id.homeToAddBottle)
            }

            binding.editWine.setOnClickListener {
                dismiss()
                findNavController().navigate(R.id.homeToAddWine)
            }

            deleteWine.setOnClickListener {
                dismiss()

                context?.let { context ->
                    MaterialAlertDialogBuilder(context)
                        .setMessage(resources.getString(R.string.confirmation_delete))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                            homeViewModel.deleteWine(editWine)
                        }
                        .show()
                }
            }
        }
    }
}
