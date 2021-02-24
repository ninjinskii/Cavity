package com.louis.app.cavity.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.datepicker.MaterialDatePicker
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHistoryBinding
import com.louis.app.cavity.ui.history.HistoryRecyclerAdapter.Companion.TYPE_SEPARATOR
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setupNavigation

class FragmentHistory : Fragment(R.layout.fragment_history) {
    private lateinit var scroller: LinearSmoothScroller
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModels()
    private val args: FragmentHistoryArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupNavigation(binding.toolbar)

        scroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference() = SNAP_TO_START
        }

        // TODO: uniformize viewmodels initialization
        if (args.bottleId != -1L) {
            historyViewModel.setFilter(HistoryFilter.BottleFilter(args.bottleId))
        }

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val historyAdapter = HistoryRecyclerAdapter(requireContext()) {
            showDatePicker()
        }
        val isHeader = { itemPos: Int -> historyAdapter.getItemViewType(itemPos) == TYPE_SEPARATOR }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
            setHasFixedSize(true)
            addItemDecoration(StickyItemDecorator(this, isHeader) {
                showDatePicker()
            })
        }

        historyViewModel.entries.observe(viewLifecycleOwner) {
            historyAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun observe() {
        historyViewModel.scrollTo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { pos ->
                scroller.targetPosition = pos
                binding.recyclerView.layoutManager?.startSmoothScroll(scroller)
            }
        }
    }

    private fun setListeners() {
        binding.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            historyViewModel.setFilter(HistoryFilter.TypeFilter(checkedId))
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText("Naviguer à la date")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            it?.let { timestamp ->
                historyViewModel.requestScrollToDate(timestamp)
            }
        }

        datePicker.show(childFragmentManager, "random-tag")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
