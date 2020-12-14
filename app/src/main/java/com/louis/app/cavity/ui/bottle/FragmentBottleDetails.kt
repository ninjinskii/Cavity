package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: FragmentBottleDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottleDetailsBinding.bind(view)

        //binding.grapeBar.addAllGrapes()

        binding.grapeBar.triggerAnimation()
        setListeners()
    }

    private fun setListeners() {
        binding.buttonEditBottle.setOnClickListener {
            binding.grapeBar.triggerAnimation()
        }

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
