package com.louis.app.cavity.ui.bottle

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.util.setVisible

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
        setupCollapsingToolbar()
        setListener()
    }

    private fun setupCollapsingToolbar() {
//        val toolbarLayoutHeight by lazy { binding.collapsingToolbar.height }
//        val trigger by lazy { binding.collapsingToolbar.scrimVisibleHeightTrigger }
//
//        binding.appBarLayout.addOnOffsetChangedListener(
//            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
//                val showComponents = toolbarLayoutHeight + verticalOffset > trigger
//                binding.shaper.setVisible(showComponents, invisible = true)
//                binding.fabEditBottle.run { if (showComponents) show() else hide() }
//            })
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

    override fun onResume() {
        super.onResume()
        (activity as ActivityMain).hideToolbar()
    }

    override fun onPause() {
        super.onPause()
        (activity as ActivityMain).showToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
