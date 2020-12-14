package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!
    private val bottleDetailsViewModel: BottleDetailsViewModel by viewModels()
    private val args: FragmentBottleDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottleDetailsBinding.bind(view)

        //binding.grapeBar.addAllGrapes()

        observe()
        setListeners()
    }

    private fun observe() {
        bottleDetailsViewModel.getBottleById(args.bottleId).observe(viewLifecycleOwner) {
            // update ui
        }

        bottleDetailsViewModel.getQGrapesForBottle(args.bottleId).observe(viewLifecycleOwner) {
            binding.grapeBar.apply {
                addAllGrapes(it)
                triggerAnimation()
            }
        }

        bottleDetailsViewModel.getFReviewForBottle(args.bottleId).observe(viewLifecycleOwner) {

        }
    }

    private fun setListeners() {
        binding.fabEditBottle.setOnClickListener {
            val action = FragmentBottleDetailsDirections.bottleDetailsToEditBottle(
                args.wineId,
                args.bottleId
            )

            findNavController().navigate(action)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
