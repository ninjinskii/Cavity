package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetWineOptionsBinding
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean

class WineOptionsBottomSheet(private val wine: Wine) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetWineOptionsBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetWineOptionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.colorAccent)
            )
        } ?: return

        //val wine = homeViewModel.modalSheetWine ?: return

        with(binding) {
            currentWine.wineName.text = wine.name
            currentWine.wineNaming.text = wine.naming
            currentWine.wineColorIndicator.setColorFilter(colors[wine.color])
            currentWine.organicImage.setVisible(wine.isOrganic.toBoolean())

            addBottle.setOnClickListener {
                dismiss()

                val bundle = bundleOf(ARG_WINE_ID to wine.wineId)
                findNavController().navigate(R.id.homeToAddBottle, bundle)
            }

            binding.editWine.setOnClickListener {
                dismiss()

                val bundle = bundleOf(ARG_WINE_ID to wine.wineId)
                findNavController().navigate(R.id.homeToAddWine, bundle)
            }

            deleteWine.setOnClickListener {
                dismiss()

                context?.let { context ->
                    MaterialAlertDialogBuilder(context)
                        .setMessage(resources.getString(R.string.confirmation_delete))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                            homeViewModel.deleteWine(wine)
                        }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_WINE_ID = "com.louis.app.cavity.ui.home.FragmentWines.ARG_WINE_ID"
    }
}
