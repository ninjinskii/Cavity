package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetWineOptionsBinding
import com.louis.app.cavity.util.setVisible

class WineOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetWineOptionsBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val args: WineOptionsBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWineOptionsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wineColor = ContextCompat.getColor(requireContext(), args.color.colorRes)

        with(binding) {
            currentWine.wineName.text = args.wineName
            currentWine.wineNaming.text = args.wineNaming
            currentWine.wineColorIndicator.setColorFilter(wineColor)
            currentWine.organicImage.setVisible(args.isOrganic)

            addBottle.setOnClickListener {
                dismiss()

                val action = WineOptionsBottomSheetDirections.wineOptionsToAddBottle(args.wineId)

                findNavController().navigate(action)
            }

            editWine.setOnClickListener {
                dismiss()

                val action = WineOptionsBottomSheetDirections.wineOptionsToEditWine(
                    args.wineId,
                    args.countyId
                )

                findNavController().navigate(action)
            }

            deleteWine.setOnClickListener {
                context?.let { context ->
                    MaterialAlertDialogBuilder(context)
                        .setMessage(resources.getString(R.string.confirm_wine_delete))
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                            homeViewModel.deleteWine(args.wineId)
                            dismiss()
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
}
