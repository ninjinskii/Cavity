package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.ActivityMain

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
                Grape(0, "Bruh", 15, 0),
            )
        )

        binding.grapeBar.triggerAnimation()
        setupCollapsingToolbar()
        setListener()
    }

    private fun setupCollapsingToolbar() {
//        binding.collapsingToolbar.addTransitionListener(object : MotionLayout.TransitionListener {
//            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
//            }
//
//            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
//                binding.fabEditBottle.run { if (p3 > 0.6) hide() else show() }
//            }
//
//            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
//            }
//
//            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
//            }
//        })
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

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observe() {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
