package com.louis.app.cavity.ui.history

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHistoryBinding
import com.louis.app.cavity.db.dao.BoundedHistoryEntry
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.history.adapter.HistoryDivider
import com.louis.app.cavity.ui.history.adapter.HistoryRecyclerAdapter
import com.louis.app.cavity.ui.history.adapter.HistoryRecyclerAdapter.Companion.TYPE_SEPARATOR
import com.louis.app.cavity.ui.history.adapter.ReboundingSwipeActionCallback
import com.louis.app.cavity.ui.history.adapter.StickyItemDecorator
import com.louis.app.cavity.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class FragmentHistory : Fragment(R.layout.fragment_history) {
    private lateinit var colorUtil: ColorUtil
    private lateinit var transitionHelper: TransitionHelper
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModels()
    private val args: FragmentHistoryArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transitionHelper = TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = false)
            setFadeThrough(navigatingForward = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        _binding = FragmentHistoryBinding.bind(view)

        setupNavigation(binding.toolbar)

        colorUtil = ColorUtil(requireContext())

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
            colorUtil,
            onHeaderClick = { historyViewModel.requestDatePicker() },
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
        binding.historyEntryList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
            setHasFixedSize(false)

            addItemDecoration(HistoryDivider(height, color))
            addItemDecoration(StickyItemDecorator(this, isHeader) {
                historyViewModel.requestDatePicker()
            })

            itemTouchHelper.attachToRecyclerView(this)
        }

        historyViewModel.entries.observe(viewLifecycleOwner) {
            historyAdapter.submitData(viewLifecycleOwner.lifecycle, it)

            if (historyViewModel.filter.value is HistoryFilter.DateFilter) {
                lifecycleScope.launch {
                    delay(1000)
                    historyViewModel.setFilter(HistoryFilter.NoFilter)
                }
            }
        }

        historyAdapter.addLoadStateListener { loadState ->
            if (loadState.append.endOfPaginationReached) {
                binding.emptyState.setVisible(historyAdapter.itemCount < 1)
            }
        }
    }

    private fun observe() {
        // Reuse when find a way to jump scroll into paged list
        /*historyViewModel.scrollTo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { pos ->
                L.v("Start scrolling to position: $pos")
                val scroller = JumpSmoothScroller(requireContext(), jumpThreshold = 5)
                scroller.targetPosition = pos
                val item = (binding.historyEntryList.adapter as HistoryRecyclerAdapter)


                lifecycleScope.launch(Main) {
                    repeat(20) {
                        delay(100)
                        item.refresh()
                    }
                }

                binding.historyEntryList.layoutManager?.startSmoothScroll(scroller)
            }
        }*/

        historyViewModel.selectedEntry.observe(viewLifecycleOwner) {
            bindBottomSheet(it)
        }

        historyViewModel.showDatePicker.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { oldestEntryDate ->
                showDatePicker(oldestEntryDate)
            }
        }
    }

    private fun setListeners() {
        binding.filterChipGroup.setOnCheckedChangeListener { _, checkedId ->
            historyViewModel.setFilter(HistoryFilter.TypeFilter(checkedId))

            lifecycleScope.launch {
                delay(100)

                binding.emptyState.apply {
                    if (checkedId == R.id.chipFavorites) {
                        setIcon(R.drawable.ic_star)
                        setText(getString(R.string.empty_history_favorite))
                    } else {
                        setIcon(R.drawable.ic_history)
                        setText(getString(R.string.empty_history))
                    }
                }
            }
        }

        binding.bottleDetails.buttonCloseBottomSheet.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.bottleDetails.root.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.bottleDetails.buttonShowBottle.setOnClickListener {
            historyViewModel.selectedEntry.value?.let {
                val (bottle, wine) = it.bottleAndWine
                val action = FragmentHistoryDirections.historyToBottle(wine.id, bottle.id)

                findNavController().navigate(action)
            }
        }

        bottomSheetBehavior?.addBottomSheetCallback(object :
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

    private fun showDatePicker(startDate: Long) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val min = min(startDate, today)
        val max = max(startDate, today)

        val constraint = CalendarConstraints.Builder()
            .setEnd(max)
            .setStart(min)
            .setValidator(object : CalendarConstraints.DateValidator {
                override fun describeContents() = -1

                override fun writeToParcel(p0: Parcel?, p1: Int) = Unit

                override fun isValid(date: Long) = date in min..max
            })
            .build()

        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setCalendarConstraints(constraint)
            .setTitleText(R.string.go_to)
            .build()

        datePicker.addOnPositiveButtonClickListener {
            it?.let { timestamp ->
                // TODO: Reuse when find a way to jump scroll into paged list
                //historyViewModel.requestScrollToDate(timestamp)

                historyViewModel.setFilter(HistoryFilter.DateFilter(timestamp))
            }
        }

        datePicker.show(childFragmentManager, getString(R.string.tag_date_picker))
    }

    private fun bindBottomSheet(entry: BoundedHistoryEntry?) {
        if (entry == null) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            val (bottle, wine) = entry.bottleAndWine
            val wineColor = ContextCompat.getColor(requireContext(), wine.color.colorRes)
            val label = if (entry.tastingWithBottles != null) {
                entry.tastingWithBottles?.tasting?.opportunity ?: ""
            } else {
                getString(entry.historyEntry.getResources().detailsLabel)
            }

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED

            // Ensure friends are in the correct order by forcing all chips to be inflated again
            binding.bottleDetails.friendChipGroup.removeAllViews()

            with(binding.bottleDetails) {
                ChipLoader.Builder()
                    .with(lifecycleScope)
                    .useInflater(layoutInflater)
                    .toInflate(R.layout.chip_friend)
                    .load(entry.friends)
                    .into(friendChipGroup)
                    .useAvatar(true)
                    .selectable(false)
                    .build()
                    .go()

                vintage.text = bottle.vintage.toString()

                wineDetails.wineColorIndicator.setColorFilter(wineColor)
                wineDetails.wineName.text = wine.name
                wineDetails.wineNaming.text = wine.naming
                wineDetails.organicImage.setVisible(wine.isOrganic.toBoolean())

                participants.setVisible(entry.friends.isNotEmpty(), invisible = true)
                participants.text = label

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

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetBehavior = null
        _binding = null
    }
}
