package com.louis.app.cavity.ui.history

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHistoryBinding
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.WineColorResolver
import com.louis.app.cavity.ui.history.adapter.*
import com.louis.app.cavity.ui.history.adapter.HistoryRecyclerAdapter.Companion.TYPE_SEPARATOR
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.toBoolean
import com.louis.app.cavity.util.toggleBoolean

class FragmentHistory : Fragment(R.layout.fragment_history), WineColorResolver {
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

        scroller = JumpSmoothScroller(requireContext(), jumpThreshold = 20)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            isFitToContents = true
        }

        historyViewModel.start(args.bottleId)

        initRecyclerView()
        observe()
        setListeners()
        applyBottomSheetShape()
    }

    private fun initRecyclerView() {
        val height = resources.getDimensionPixelSize(R.dimen.divider_height)
        val color = ContextCompat.getColor(requireContext(), R.color.divider_color)
        val itemTouchHelper = ItemTouchHelper(ReboundingSwipeActionCallback())
        val historyAdapter = HistoryRecyclerAdapter(
            requireContext(),
            onHeaderClick = { showDatePicker() },
            onItemClick = {
                binding.filterChipGroup.clearCheck()
                historyViewModel.setFilter(HistoryFilter.BottleFilter(it.model.bottleAndWine.bottle.id))
                historyViewModel.setSelectedHistoryEntry(it.model)
            },
            onSwiped = {
                val entry = it.model.historyEntry
                val newEntry = entry.copy(favorite = entry.favorite.toggleBoolean())
                historyViewModel.updateHistoryEntry(newEntry)
            }
        )

        val isHeader = { itemPos: Int -> historyAdapter.getItemViewType(itemPos) == TYPE_SEPARATOR }
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
            setHasFixedSize(false)

            addItemDecoration(HistoryDivider(height, color))
            addItemDecoration(StickyItemDecorator(this, isHeader) {
                showDatePicker()
            })

            itemTouchHelper.attachToRecyclerView(this)
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
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.bottleDetails.root.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.bottleDetails.buttonShowBottle.setOnClickListener {
            historyViewModel.selectedEntry.value?.let {
                val (bottle, wine) = it.bottleAndWine
                val action =
                    FragmentHistoryDirections.historyToBottle(wine.id, bottle.id)

                findNavController().navigate(action)
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val checkedId = binding.filterChipGroup.checkedChipId

                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (checkedId != ChipGroup.NO_ID) {
                        historyViewModel.setFilter(HistoryFilter.TypeFilter(checkedId))
                    } else {
                        resetFilters()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(R.string.go_to)
            .build()

        datePicker.addOnPositiveButtonClickListener {
            it?.let { timestamp ->
                historyViewModel.requestScrollToDate(timestamp)
            }
        }

        datePicker.show(childFragmentManager, getString(R.string.tag_date_picker))
    }

    private fun bindBottomSheet(entry: BoundedHistoryEntry?) {
        if (entry == null) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            val (bottle, wine) = entry.bottleAndWine
            val label = entry.historyEntry.getResources().detailsLabel

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            with(binding.bottleDetails) {
                friendChipGroup.removeAllViews()

                ChipLoader.Builder()
                    .with(lifecycleScope)
                    .useInflater(layoutInflater)
                    .load(entry.friends)
                    .into(friendChipGroup)
                    .selectable(false)
                    .build()
                    .go()

                vintage.text = bottle.vintage.toString()

                wineDetails.wineColorIndicator.setColorFilter(resolveColor(wine.color))
                wineDetails.wineName.text = wine.name
                wineDetails.wineNaming.text = wine.naming
                wineDetails.organicImage.setVisible(wine.isOrganic.toBoolean())

                participants.setVisible(entry.friends.isNotEmpty())
                participants.text = getString(label)

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

    private fun resetFilters() {
        binding.filterChipGroup.clearCheck()
        historyViewModel.setFilter(HistoryFilter.NoFilter)
    }

    override fun getOverallContext() = requireContext()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
