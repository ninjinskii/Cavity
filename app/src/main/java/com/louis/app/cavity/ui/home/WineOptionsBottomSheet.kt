package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetWineOptionsBinding
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.home.widget.WineOptionsViewModel
import com.louis.app.cavity.ui.navigation.WineOptionsRoute
import com.louis.app.cavity.ui.navigation.navigate
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.toBoolean
import kotlinx.coroutines.launch

class WineOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetWineOptionsBinding? = null
    private val binding get() = _binding!!

    /*private val wineOptionsViewModel: WineOptionsViewModel by viewModels(
        factoryProducer = { WineOptionsViewModel.Factory }
    )*/
    private val wineOptionsViewModel: WineOptionsViewModel by viewModels {
        WineOptionsViewModel.Factory(args.wineId)
    }
    private val homeViewModel: HomeViewModel by activityViewModels() // TODO: remove after complete home viewmodel refactoring & scoping. See HomeViewModel todo for mor info
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                wineOptionsViewModel.state.collect { state ->
                    state.currentWine?.let { updateUi(it) }
                }
            }
        }
    }

    private fun updateUi(wine: Wine) {
        val wineColor = ContextCompat.getColor(requireContext(), wine.color.colorRes)

        with(binding) {
            currentWine.wineName.text = wine.name
            currentWine.wineNaming.text = wine.naming
            currentWine.wineColorIndicator.setColorFilter(wineColor)
            currentWine.organicImage.setVisible(wine.isOrganic.toBoolean())

            addBottle.setOnClickListener {
                navigate(WineOptionsRoute.AddBottle(args.wineId))
            }

            editWine.setOnClickListener {
                navigate(WineOptionsRoute.EditWine(args.wineId, args.countyId))
            }

            showHistory.setOnClickListener {
                navigate(WineOptionsRoute.ShowWineHistory(args.wineId))
            }

            deleteWine.setVisible(!args.storageLocationEnabled)
            deleteWine.setOnClickListener {
                context?.let { context ->
                    LifecycleMaterialDialogBuilder(context, viewLifecycleOwner)
                        .setMessage(R.string.confirm_wine_delete)
                        .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                        }
                        .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                            wineOptionsViewModel.handleWineDeleteRequest(args.wineId)
                            dismiss()
                        }
                        .show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Avoid navigation controller setting up app title in toolbar when quitting fragment (storage_location in mind)
        homeViewModel.notifyStorageLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
