package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.addbottle.FragmentAddBottle.Companion.EDIT_BOTTLE_ID
import com.louis.app.cavity.ui.home.FragmentWines.Companion.BOTTLE_ID
import com.louis.app.cavity.ui.home.FragmentWines.Companion.WINE_ID

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottleDetailsBinding.bind(view)

        binding.grapeBar.addAllGrapes(
            listOf(
                Grape(0, "Merlot", 30, 0),
                Grape(0, "Syrah", 20, 0),
                Grape(0, "Grenache", 5, 0),
            )
        )

        binding.grapeBar.triggerAnimation()

        setListener()
    }

    private fun setListener() {
        binding.buttonEditBottle.setOnClickListener {
//            val wineId = arguments?.getLong(WINE_ID)
//            val bottleId = arguments?.getLong(BOTTLE_ID)
//            bottleId?.let {
//                val bundle = bundleOf(WINE_ID to wineId, EDIT_BOTTLE_ID to it)
//                findNavController().navigate(R.id.bottleDetailsToAddBottle, bundle)
//            }

            binding.grapeBar.triggerAnimation()
        }
    }

    private fun observe() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
