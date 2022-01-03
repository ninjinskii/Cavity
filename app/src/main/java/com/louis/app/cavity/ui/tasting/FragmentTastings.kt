package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingsBinding
import com.louis.app.cavity.ui.tasting.notifications.TastingAlarmScheduler
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setupNavigation

class FragmentTastings : Fragment(R.layout.fragment_tastings) {
    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentTastingsBinding? = null
    private val binding get() = _binding!!
    private val tastingViewModel: TastingViewModel by viewModels()
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

        initRecyclerView()
        setListener()
    }

    private fun initRecyclerView() {
        val tastingAdapter = TastingRecyclerAdapter(friendViewPool, transitionHelper)

        binding.tastingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tastingAdapter
            setHasFixedSize(true)
        }

        tastingViewModel.undoneTastings.observe(viewLifecycleOwner) {
            tastingAdapter.submitList(it)
            TastingAlarmScheduler.setIsBootCompletedReceiverEnabled(
                requireContext(),
                it.isNotEmpty()
            )
        }
    }

    private fun setListener() {
        binding.buttonAddTasting.setOnClickListener {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

            val action = FragmentTastingsDirections.tastingToAddTasting()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
