package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireScheduleBinding
import com.louis.app.cavity.ui.stepper.Step

class FragmentInquireSchedule : Step(R.layout.fragment_inquire_schedule) {
    private var _binding: FragmentInquireScheduleBinding? = null
    private val binding get() = _binding!!
    private val addTastingViewModel: AddTastingViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireScheduleBinding.bind(view)

        initRecylerView()
    }

    private fun initRecylerView() {
        val tastingBottleAdapter = TastingBottleAdapter()
        val spanCount = 2
        val space = requireContext().resources.getDimension(R.dimen.small_margin)

        binding.tastingBottleList.apply {
            adapter = tastingBottleAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
            setHasFixedSize(true)
            addItemDecoration(SpaceGridItemDecoration(space.toInt()))
        }

        addTastingViewModel.tastingBottles.observe(viewLifecycleOwner) {
            tastingBottleAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
