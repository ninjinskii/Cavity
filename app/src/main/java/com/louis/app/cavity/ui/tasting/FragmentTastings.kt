package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingsBinding
import com.louis.app.cavity.ui.notifications.TastingAlarmScheduler
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.extractMargin
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentTastings : Fragment(R.layout.fragment_tastings) {
    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentTastingsBinding? = null
    private val binding get() = _binding!!
    private val tastingViewModel: TastingViewModel by activityViewModels()
    private val friendViewPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(R.layout.chip_friend, 8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transitionHelper = TransitionHelper(this).apply {
            setFadeThroughOnEnterAndExit()
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        _binding = FragmentTastingsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        applyInsets()
        initRecyclerView()
        setListener()
    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }

        binding.tastingList.prepareWindowInsets { view, _, left, _, right, bottom ->
            view.updatePadding(bottom = bottom, left = left, right = right)
            WindowInsetsCompat.CONSUMED
        }

        val initialMargin = binding.buttonAddTasting.extractMargin()
        binding.buttonAddTasting.prepareWindowInsets { view, _, _, _, right, bottom ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(
                right = right + initialMargin.right,
                bottom = bottom + initialMargin.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        val tastingAdapter = TastingRecyclerAdapter(friendViewPool, transitionHelper)

        binding.tastingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tastingAdapter
            setHasFixedSize(true)
        }

        tastingViewModel.undoneTastings.observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            tastingAdapter.submitList(it)
            TastingAlarmScheduler.setIsBootCompletedReceiverEnabled(
                requireContext(),
                it.isNotEmpty()
            )
        }
    }

    private fun setListener() {
        binding.buttonAddTasting.setOnClickListener {
            navigateToAddTasting()
        }

        binding.emptyState.setOnActionClickListener {
            navigateToAddTasting()
        }
    }

    private fun navigateToAddTasting() {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

        val action = FragmentTastingsDirections.tastingToAddTasting()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
