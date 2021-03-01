package com.louis.app.cavity.ui.history

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHistoryBinding
import com.louis.app.cavity.model.HistoryEntryType
import com.louis.app.cavity.model.relation.history.HistoryEntryWithBottleAndTastingAndFriends
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.history.HistoryRecyclerAdapter.Companion.TYPE_SEPARATOR
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.toBoolean

class FragmentHistory : Fragment(R.layout.fragment_history) {
    private lateinit var scroller: LinearSmoothScroller
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
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

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            isFitToContents = true
        }

        // TODO: uniformize viewmodels initialization
        if (args.bottleId != -1L) {
            historyViewModel.setFilter(HistoryFilter.BottleFilter(args.bottleId))
        }

        initRecyclerView()
        observe()
        setListeners()
        applyBottomSheetShape()
    }

    private fun initRecyclerView() {
        val historyAdapter = HistoryRecyclerAdapter(requireContext(), { showDatePicker() }) {
            historyViewModel.setFilter(HistoryFilter.BottleFilter(it.model.bottleAndWine.bottle.id))
            historyViewModel.setSelectedHistoryEntry(it.model)
        }
        val isHeader = { itemPos: Int -> historyAdapter.getItemViewType(itemPos) == TYPE_SEPARATOR }

        binding.historyRecyclerView.apply {
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
                binding.historyRecyclerView.layoutManager?.startSmoothScroll(scroller)
            }
        }

        historyViewModel.selectedEntry.observe(viewLifecycleOwner) {
            bindBottomSheet(it)
        }
    }

    private fun setListeners() {
        binding.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            historyViewModel.setFilter(HistoryFilter.TypeFilter(checkedId))
        }

        binding.bottleDetails.buttonCloseBottomSheet.setOnClickListener {
            historyViewModel.setFilter(HistoryFilter.NoFilter)
        }

        binding.bottleDetails.buttonShowBottle.setOnClickListener {
            historyViewModel.selectedEntry.value?.let {
                val (bottle, wine) = it.bottleAndWine
                val action = FragmentHistoryDirections.historyToBottle(wine.id, bottle.id)
                findNavController().navigate(action)
            }
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

    private fun bindBottomSheet(entry: HistoryEntryWithBottleAndTastingAndFriends?) {
        if (entry == null) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            val (bottle, wine) = entry.bottleAndWine
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            val colorAndFriendLabel = when (entry.historyEntry.type) {
                HistoryEntryType.TYPE_CONSUME -> R.color.cavity_red to R.string.consume_label
                HistoryEntryType.TYPE_REPLENISHMENT -> R.color.cavity_light_green to null
                HistoryEntryType.TYPE_GIFTED_TO -> R.color.cavity_red to R.string.gifted_to
                HistoryEntryType.TYPE_GIFTED_BY ->
                    R.color.cavity_light_green to R.string.gifted_by
                HistoryEntryType.TYPE_TASTING -> R.color.cavity_gold to R.string.tasting_label
            }

            with(binding.bottleDetails) {
                friendChipGroup.removeAllViews()
                ChipLoader(lifecycleScope, layoutInflater).loadChips(
                    friendChipGroup,
                    entry.friends,
                    emptyList()
                )

                vintage.text = bottle.vintage.toString()

                // wineDetails.wineColorIndicator.setColorFilter(wine.color)
                wineDetails.wineName.text = wine.name
                wineDetails.wineNaming.text = wine.naming
                wineDetails.organicImage.setVisible(wine.isOrganic.toBoolean())

                participants.setVisible(entry.friends.isNotEmpty())
                participants.text = colorAndFriendLabel.second?.let { getString(it) } ?: ""

                Glide.with(requireContext())
                    .load(Uri.parse(wine.imgPath))
                    .centerCrop()
                    .into(wineImage)
            }
        }
    }

    private fun applyBottomSheetShape() {
        val bg = MaterialShapeDrawable.createWithElevationOverlay(context).apply {
            binding.bottomSheet.let {
                elevation = it.elevation
                it.background = this
            }
        }

        binding.bottomSheet.doOnLayout {
            with(binding.bottleDetails.wineImage) {
                val diameter = marginStart * 2 + measuredWidth

                bg.shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setTopEdge(BinderEdgeTreatment(diameter.toFloat()))
                    .build()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
