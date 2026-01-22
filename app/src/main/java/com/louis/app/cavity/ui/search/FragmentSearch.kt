package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Checkable
import android.widget.HorizontalScrollView
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogSortBinding
import com.louis.app.cavity.databinding.FragmentSearchBinding
import com.louis.app.cavity.databinding.SearchFiltersBinding
import com.louis.app.cavity.db.dao.BoundedBottle
import com.louis.app.cavity.model.*
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.addtasting.AddTastingViewModel
import com.louis.app.cavity.ui.search.filters.*
import com.louis.app.cavity.ui.search.widget.InsettableInfo
import com.louis.app.cavity.ui.search.widget.RecyclerViewDisabler
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.util.*
import com.robinhood.ticker.TickerUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * This fragment is used as step when adding tasting
 */
class FragmentSearch : Step(R.layout.fragment_search) {
    companion object {
        /* Saved state */
        private const val SLIDER_VINTAGE_START = "com.louis.app.cavity.SLIDER_VINTAGE_START"
        private const val SLIDER_VINTAGE_END = "com.louis.app.cavity.SLIDER_VINTAGE_END"
        private const val SWITCH_PRICE_ENABLED = "com.louis.app.cavity.SWITCH_PRICE_ENABLED"
        private const val SLIDER_PRICE_START = "com.louis.app.cavity.SLIDER_PRICE_START"
        private const val SLIDER_PRICE_END = "com.louis.app.cavity.SLIDER_PRICE_END"
        private const val SLIDER_ALCOHOL_START = "com.louis.app.cavity.SLIDER_ALCOHOL_START"
        private const val SLIDER_ALCOHOL_END = "com.louis.app.cavity.SLIDER_ALCOHOL_END"
        private const val DATE_BEYOND = "com.louis.app.cavity.DATE_BEYOND"
        private const val DATE_UNTIL = "com.louis.app.cavity.DATE_UNTIL"
        private const val CHIP_COLOR = "com.louis.app.cavity.CHIP_COLOR"
        private const val CHIP_MISC = "com.louis.app.cavity.CHIP_MISC"
        private const val SWITCH_SELECTED_ENABLED = "com.louis.app.cavity.SWITCH_SELECTED_ENABLED"
        private const val STORAGE_LOCATION = "com.louis.app.cavity.STORAGE_LOCATION"
        private const val RADIO_CAPACITY = "com.louis.app.cavity.RADIO_CAPACITY"

        const val PICK_MODE = "com.louis.app.cavity.ui.search.FragmentSearch.PICK_MODE"
    }

    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var bottlesAdapter: BottleRecyclerAdapter? = null
    private var _filtersBinding: SearchFiltersBinding? = null
    private val filtersBinding get() = _filtersBinding!!

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val addTastingViewModel: AddTastingViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val recyclerViewDisabler = RecyclerViewDisabler { binding.toggleBackdrop.toggle() }
    private val backdropHeaderHeight by lazy { fetchBackdropHeaderHeight() }
    private val revealShadowAnim by lazy { loadRevealShadowAnim() }
    private val hideShadowAnim by lazy { loadHideShadowAnim() }
    private var isHeaderShadowDisplayed = false
    private var isPickMode = false
    private var insetBottom = 0
    private var datePickerBeyond: DatePicker? = null
    private var datePickerUntil: DatePicker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transitionHelper = TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = false)
            setFadeThrough(navigatingForward = true)
        }

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        _binding = FragmentSearchBinding.bind(view)

        binding.bottleList.doOnPreDraw {
            isHeaderShadowDisplayed = false
            setHeaderShadow(binding.bottleList.canScrollVertically(-1))
        }

        setupNavigation(binding.fakeToolbar)

        isPickMode = arguments?.getBoolean(PICK_MODE) == true

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
            saveFlags = BottomSheetBehavior.SAVE_NONE
        }

        binding.fakeToolbar.setNavigationOnClickListener {
            if (isPickMode) {
                stepperFragment?.goToPreviousPage()
            } else {
                findNavController().navigateUp()
            }
        }

        applyInsets()
        initRecyclerView()
        initTickerView()
        setupMenu()
        setListeners()
        initSearchView()
        setupCustomBackNav()

        lifecycleScope.launch {
            delay(800)
            inflateFiltersStub(searchViewModel.onFragmentLeaveSavedState ?: savedInstanceState)
        }
    }

    private fun applyInsets() {
        binding.main.prepareWindowInsets { view, windowInsets, _, top, _, bottom ->
            insetBottom = bottom
            setBottomSheetPeekHeight() // Update peek height when keyboard opens
            view.updatePadding(top = top)
            windowInsets
        }

        binding.motionToolbar.prepareWindowInsets { view, _, left, _, right, _ ->
            view.updatePadding(left = left, right = right)
            WindowInsetsCompat.CONSUMED
        }

        binding.headerConstraint.prepareWindowInsets { view, _, left, _, right, _ ->
            view.updatePadding(left = left, right = right)
            WindowInsetsCompat.CONSUMED
        }

        binding.bottleList.prepareWindowInsets(false) { view, _, left, _, right, bottom ->
            view.updatePadding(left = left, right = right, bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }

        var selectedChipInitialMargin: Int? = null
        var cycleFriendInitialMargin: Int? = null

        // We want to inset individually each views in the layout to ensure horizontal scroll view
        // are edge to edge. At the date of this comment, this is the only screen that takes care
        // of horizontal scroll view edge to edge.
        binding.filtersStub.setOnInflateListener { _, inflatedView ->
            inflatedView.doOnAttach {
                val leftRightInsettable = intArrayOf(
                    R.id.countyScrollView, R.id.colorScrollView,
                    R.id.otherScrollView, R.id.divider1, R.id.vintageTitle, R.id.vintageSlider,
                    R.id.divider2, R.id.dateTitle, R.id.divider3, R.id.priceTitle, R.id.priceSlider,
                    R.id.warning, R.id.divider4, R.id.grapeTitle, R.id.grapeScrollView,
                    R.id.divider5, R.id.reviewTitle, R.id.reviewScrollView, R.id.divider6,
                    R.id.divider7, R.id.friendTitle, R.id.friendScrollView, R.id.bottleSizeTitle,
                    R.id.divider8, R.id.divider9, R.id.alcoholTitle, R.id.alcoholSlider,
                    R.id.storageLocationTitle, R.id.storageLocationLayout,
                    R.id.tagScrollView, R.id.tagTitle
                ).map { InsettableInfo(inflatedView.findViewById(it)) }

                val leftInsettable = intArrayOf(R.id.beyondLayout)
                    .map { InsettableInfo(inflatedView.findViewById(it)) }

                val rightInsettable = intArrayOf(R.id.untilLayout, R.id.togglePrice)
                    .map { InsettableInfo(inflatedView.findViewById(it)) }

                inflatedView.prepareWindowInsets { view, _, left, _, right, bottom ->
                    // Udpdates stub root scroll view bottom padding
                    view.updatePadding(bottom = bottom)

                    leftRightInsettable.forEach {
                        val layoutParams = it.view.layoutParams as ViewGroup.MarginLayoutParams

                        when (it.view) {
                            is RangeSlider ->
                                layoutParams.updateMargins(
                                    left = left + it.initialMargin.left,
                                    right = right + it.initialMargin.right
                                )

                            is HorizontalScrollView ->
                                it.view.updatePadding(
                                    left = left + it.initialPadding.left,
                                    right = right + it.initialPadding.right
                                )

                            else -> it.view.updatePadding(left = left, right = right)
                        }
                    }

                    leftInsettable.forEach {
                        it.view.updatePadding(left = left)
                    }

                    rightInsettable.forEach {
                        val layoutParams = it.view.layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.updateMargins(right = right + it.initialMargin.right)
                    }

                    val selectedChip = inflatedView.findViewById<Chip>(R.id.chipSelected)
                    val selectedLayoutParams =
                        selectedChip.layoutParams as ViewGroup.MarginLayoutParams
                    val selectedBaseMargin = selectedChipInitialMargin
                        ?: selectedChip.extractMargin().left.also { selectedChipInitialMargin = it }

                    selectedLayoutParams.updateMargins(left = left + selectedBaseMargin)

                    val cycleFriend = inflatedView.findViewById<ImageView>(R.id.cycleFriendFilter)
                    val cycleLayoutParams = cycleFriend.layoutParams as ViewGroup.MarginLayoutParams
                    val cycleBaseMargin = cycleFriendInitialMargin
                        ?: cycleFriend.extractMargin().right.also { cycleFriendInitialMargin = it }

                    cycleLayoutParams.updateMargins(right = right + cycleBaseMargin)

                    WindowInsetsCompat.CONSUMED
                }

                // Apply insets manually on stub view
                inflatedView.requestApplyInsets()
            }
        }
    }

    private fun inflateFiltersStub(savedInstanceState: Bundle?) {
        // binding might be null if someone (or the monkey) is spamming destinations
        val view = _binding?.filtersStub?.inflate() ?: return
        _filtersBinding = SearchFiltersBinding.bind(view)

        filtersBinding.root.doOnLayout {
            setBottomSheetPeekHeight()
        }

        observe()
        initColorChips(savedInstanceState)
        initOtherChips(savedInstanceState)
        initDatePickers(savedInstanceState)
        initSliders(savedInstanceState)
        initFriendTextSwitcher()
        initRadioButtons(savedInstanceState)
        initStorageLocationDropdown(savedInstanceState)
    }

    private fun setBottomSheetPeekHeight() {
        if (_filtersBinding == null) {
            return
        }

        val height = binding.root.height
        val childCount = filtersBinding.constraint.childCount
        val lastChild = filtersBinding.constraint.getChildAt(childCount - 1)
        val initialPadding = lastChild.paddingBottom
        val filtersBottom = lastChild.bottom
        val fill = height - filtersBottom - backdropHeaderHeight

        val peekHeight =
            max(backdropHeaderHeight + insetBottom, fill + initialPadding - insetBottom)
        bottomSheetBehavior?.setPeekHeight(peekHeight, true)
    }

    private fun observe() {
        searchViewModel.getAllCounties().observe(viewLifecycleOwner) { counties ->
            val chipGroupId = filtersBinding.countyChipGroup.id
            val preselectedCounties = searchViewModel.selectedCounties.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(counties)
                .into(filtersBinding.countyChipGroup)
                .preselect(preselectedCounties)
                .doOnClick { searchViewModel.submitFilter(chipGroupId, getCountyFilter()) }
                .emptyText(getString(R.string.empty_county))
                .build()
                .go()
        }

        searchViewModel.getAllGrapes().observe(viewLifecycleOwner) { grapes ->
            val chipGroupId = filtersBinding.grapeChipGroup.id
            val preselectedGrapes = searchViewModel.selectedGrapes.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(grapes)
                .into(filtersBinding.grapeChipGroup)
                .preselect(preselectedGrapes)
                .doOnClick { searchViewModel.submitFilter(chipGroupId, getGrapeFilter()) }
                .emptyText(getString(R.string.empty_grape))
                .build()
                .go()

        }

        searchViewModel.getAllReviews().observe(viewLifecycleOwner) { reviews ->
            val chipGroupId = filtersBinding.reviewChipGroup.id
            val preselectedReviews = searchViewModel.selectedReviews.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(reviews)
                .into(filtersBinding.reviewChipGroup)
                .preselect(preselectedReviews)
                .doOnClick { searchViewModel.submitFilter(chipGroupId, getReviewFilter()) }
                .emptyText(getString(R.string.empty_review))
                .build()
                .go()
        }

        searchViewModel.getAllFriends().observe(viewLifecycleOwner) { friends ->
            val preselectedFriends = searchViewModel.selectedFriends.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend)
                .load(friends)
                .into(filtersBinding.friendChipGroup)
                .useAvatar(true)
                .selectable(true) // friend chips are not selectablea by default
                .preselect(preselectedFriends)
                .doOnClick { submitFriendFilter() }
                .emptyText(getString(R.string.empty_friend))
                .build()
                .go()
        }

        searchViewModel.getAllTags().observe(viewLifecycleOwner) { tags ->
            val chipGroupId = filtersBinding.tagChipGroup.id
            val preselectedTags = searchViewModel.selectedTags.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_tag)
                .load(tags)
                .into(filtersBinding.tagChipGroup)
                .preselect(preselectedTags)
                .doOnClick { searchViewModel.submitFilter(chipGroupId, getTagFilter()) }
                .emptyText(getString(R.string.empty_tag))
                .build()
                .go()
        }

        if (isPickMode) {
            addTastingViewModel.selectedBottles.observe(viewLifecycleOwner) {
                this@FragmentSearch.binding.buttonSubmit.isEnabled = it.isNotEmpty()
                filtersBinding.chipSelected.text =
                    resources.getString(R.string.selected_bottles, it.size)
            }
        }
    }

    private fun initColorChips(savedInstanceState: Bundle?) {
        with(filtersBinding) {
            chipRed.setTag(R.string.tag_view_wine_filter, FilterColor(WineColor.RED))
            chipWhite.setTag(R.string.tag_view_wine_filter, FilterColor(WineColor.WHITE))
            chipSweet.setTag(R.string.tag_view_wine_filter, FilterColor(WineColor.SWEET))
            chipRose.setTag(R.string.tag_view_wine_filter, FilterColor(WineColor.ROSE))
        }

        filtersBinding.colorChipGroup.apply {
            val savedState = savedInstanceState?.getIntArray(CHIP_COLOR)
            savedState?.let { checkedIds ->
                children.forEach {
                    (it as Checkable).isChecked = it.id in checkedIds
                }
            }

            setOnCheckedStateChangeListener { _, _ ->
                val filters = getViewGroupFilters(this)
                searchViewModel.submitFilter(id, combineFilters(filters))
            }
        }
    }

    private fun initOtherChips(savedInstanceState: Bundle?) {
        val savedStateSelected = savedInstanceState?.getBoolean(SWITCH_SELECTED_ENABLED)
        val savedStateMisc = savedInstanceState?.getIntArray(CHIP_MISC)

        with(filtersBinding) {
            chipSelected.setTag(R.string.tag_view_wine_filter, FilterSelected())
            chipReadyToDrink.setTag(R.string.tag_view_wine_filter, FilterReadyToDrink())
            chipOrganic.setTag(R.string.tag_view_wine_filter, FilterOrganic())
            chipFavorite.setTag(R.string.tag_view_wine_filter, FilterFavorite())
            chipPdf.setTag(R.string.tag_view_wine_filter, FilterPdf())
            // No tag for chipConsume because this chip behaves a little bit differently since
            // we have to constantly update its filter based on its checked state
        }

        filtersBinding.chipSelected.apply {
            setVisible(isPickMode)
            isChecked = savedStateSelected == true
            setOnCheckedChangeListener { _, _ ->
                searchViewModel.submitFilter(id, getSelectedBottlesFilter())
            }
        }

        filtersBinding.otherChipGroup.apply {
            savedStateMisc?.let { checkedIds ->
                children.forEach {
                    (it as Chip).isChecked = it.id in checkedIds
                }
            }

            setOnCheckedStateChangeListener { _, _ ->
                searchViewModel.submitFilter(id, getOtherFilter())
            }
        }

        filtersBinding.chipConsume.apply {
            setVisible(!isPickMode)
            isEnabled = !searchViewModel.shouldShowConsumedAndUnconsumedBottles()
        }
    }

    private fun initDatePickers(savedInstanceState: Bundle?) {
        val beyondTitle = getString(R.string.buying_date_beyond)
        val untilTitle = getString(R.string.buying_date_until)
        val beyondLayout = filtersBinding.beyondLayout
        val untilLayout = filtersBinding.untilLayout

        val onBeyondDateChanged = { date: Long? ->
            searchViewModel.apply {
                currentBeyondDate = date
                val filter = FilterDate(currentBeyondDate, currentUntilDate)
                submitFilter(beyondLayout.id, filter)
            }
        }

        val onUntilDateChanged = { date: Long? ->
            searchViewModel.apply {
                currentUntilDate = date
                val filter = FilterDate(currentBeyondDate, currentUntilDate)
                submitFilter(untilLayout.id, filter)
            }
        }

        datePickerBeyond = DatePicker(parentFragmentManager, beyondLayout, beyondTitle).apply {
            onEndIconClickListener = { onBeyondDateChanged(null) }
            onDateChangedListener = { onBeyondDateChanged(it) }
        }

        datePickerBeyond = DatePicker(parentFragmentManager, untilLayout, untilTitle).apply {
            onEndIconClickListener = { onUntilDateChanged(null) }
            onDateChangedListener = { onUntilDateChanged(it) }
        }

        // In case a date were set and restored, the view model will keep the Long value,
        // but we need to restore the text
        filtersBinding.beyond.setText(savedInstanceState?.getString(DATE_BEYOND))
        filtersBinding.until.setText(savedInstanceState?.getString(DATE_UNTIL))
    }

    private fun initSliders(savedInstanceState: Bundle?) {
        filtersBinding.vintageSlider.apply {
            val year = Calendar.getInstance().get(Calendar.YEAR).toFloat()
            val start = savedInstanceState?.getFloat(SLIDER_VINTAGE_START)
            val end = savedInstanceState?.getFloat(SLIDER_VINTAGE_END)

            valueFrom = year - 20F
            valueTo = year
            values = listOf(
                max(valueFrom, start?.coerceAtMost(valueTo) ?: valueFrom),
                min(valueTo, end?.coerceAtLeast(valueFrom) ?: valueTo)
            )

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.submitFilter(id, getVintageFilter())
                }

                override fun onStartTrackingTouch(slider: RangeSlider) = Unit
            })
        }

        filtersBinding.togglePrice.apply {
            val savedState = savedInstanceState?.getBoolean(SWITCH_PRICE_ENABLED)

            isChecked = savedState == true
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            setOnCheckedChangeListener { _, isChecked ->
                filtersBinding.priceSlider.apply {
                    // Making sure the view has its chance to restore its state before grabbing values
                    doOnLayout {
                        isEnabled = isChecked
                        searchViewModel.submitFilter(id, getPriceFilter())
                    }
                }
            }
        }

        filtersBinding.priceSlider.apply {
            val start = savedInstanceState?.getFloat(SLIDER_PRICE_START)
            val end = savedInstanceState?.getFloat(SLIDER_PRICE_END)

            values = listOf(
                max(valueFrom, start?.coerceAtMost(valueTo) ?: valueFrom),
                min(valueTo, end?.coerceAtLeast(valueFrom) ?: valueTo)
            )
            isEnabled = filtersBinding.togglePrice.isChecked

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.submitFilter(id, getPriceFilter())
                }

                override fun onStartTrackingTouch(slider: RangeSlider) = Unit
            })
        }

        filtersBinding.alcoholSlider.apply {
            val start = savedInstanceState?.getFloat(SLIDER_ALCOHOL_START)
            val end = savedInstanceState?.getFloat(SLIDER_ALCOHOL_END)

            values = listOf(
                max(valueFrom, start?.coerceAtMost(valueTo) ?: valueFrom),
                min(valueTo, end?.coerceAtLeast(valueFrom) ?: valueTo)
            )

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.submitFilter(id, getAlcoholFilter())
                }

                override fun onStartTrackingTouch(slider: RangeSlider) = Unit
            })
        }
    }

    private fun initFriendTextSwitcher() {
        val friendFilterModeText =
            listOf(R.string.drunk_with, R.string.gifted_by, R.string.gifted_to)
        val index = searchViewModel.friendFilterMode

        filtersBinding.friendTitle.apply {
            setCurrentText(getString(friendFilterModeText[index]))

            inAnimation =
                AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left)

            outAnimation =
                AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_out_right)
        }

        filtersBinding.cycleFriendFilter.apply {
            setVisible(!isPickMode)

            setOnClickListener {
                val mode = searchViewModel.cycleFriendFilterMode()
                filtersBinding.friendTitle.setText(getString(friendFilterModeText[mode]))
                submitFriendFilter()

                val animation = RotateAnimation(
                    0f,
                    180f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                    RotateAnimation.RELATIVE_TO_SELF,
                    0.5f,
                ).apply {
                    duration = resources.getInteger(R.integer.cavity_motion_short).toLong()
                    interpolator = FastOutSlowInInterpolator()
                    fillAfter = true
                }

                it.startAnimation(animation)
            }
        }
    }

    private fun initRadioButtons(savedInstanceState: Bundle?) {
        with(filtersBinding) {
            rbSlim.setTag(R.string.tag_view_wine_filter, FilterCapacity(BottleSize.SLIM))
            rbSmall.setTag(R.string.tag_view_wine_filter, FilterCapacity(BottleSize.SMALL))
            rbNormal.setTag(R.string.tag_view_wine_filter, FilterCapacity(BottleSize.NORMAL))
            rbMagnum.setTag(R.string.tag_view_wine_filter, FilterCapacity(BottleSize.MAGNUM))
        }

        filtersBinding.rbGroupSize.apply {
            savedInstanceState?.getInt(RADIO_CAPACITY)?.let {
                if (it != View.NO_ID) {
                    (getChildAt(it) as Checkable).isChecked = true
                }
            }

            addOnButtonCheckedListener { _, _, _ ->
                val filters = getViewGroupFilters(this)
                searchViewModel.submitFilter(id, combineFilters(filters))
            }
        }
    }

    private fun initRecyclerView() {
        bottlesAdapter = BottleRecyclerAdapter(
            onItemClicked = { itemView: View, bottle: BoundedBottle ->
                val transition = getString(R.string.transition_bottle_details, bottle.wine.id)
                val action =
                    FragmentSearchDirections.searchToBottleDetails(bottle.wine.id, bottle.bottle.id)
                val extra = FragmentNavigatorExtras(itemView to transition)

                itemView.hideKeyboard()
                transitionHelper.setElevationScale()
                findNavController().navigate(action, extra)
            },
            isPickMode,
            onPicked = { bottle, isChecked ->
                addTastingViewModel.onBottleStateChanged(bottle, isChecked)
            }
        )

        binding.bottleList.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = bottlesAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Add shadow when RV is not on top
                    setHeaderShadow(recyclerView.canScrollVertically(-1))
                }
            })
        }

        var sortChanged = false

        searchViewModel.results.observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            binding.matchingWines.text =
                resources.getQuantityString(R.plurals.matching_wines, it.size, it.size)
            bottlesAdapter?.submitList(it.toMutableList()) {
                if (sortChanged) {
                    binding.bottleList.scrollToPosition(0)
                    sortChanged = false
                }
            }
        }

        searchViewModel.sort.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                sortChanged = true
            }
        }
    }

    private fun initTickerView() {
        val textAppearanceApplier = AppCompatTextView(requireContext()).apply {
            TextViewCompat.setTextAppearance(this, R.style.TextAppearance_Cavity_Body1)
        }

        binding.matchingWines.apply {
            textPaint.typeface = textAppearanceApplier.paint.typeface
            setCharacterLists(TickerUtils.provideNumberList())
        }
    }

    // Known issue: bottom sheet and the toggle button might misbehave
    // if for some reason the keyboard doesn't show up when calling showKeyboard()
    private fun setupMenu() {
        binding.motionToolbar.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, p0: Int, p1: Int) {
                if ((motionLayout?.progress ?: 0F) > 0.5F) {
                    with(binding) {
                        currentQuery.setVisible(true)
                        searchView.hideKeyboard()
                    }
                } else {
                    binding.currentQuery.setVisible(false)

//                    if (binding.toggleBackdrop.isChecked) {
//                        bottomSheetBehavior.peekHeight = backdropHeaderHeight
//                    }
                }
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) = Unit

            override fun onTransitionCompleted(motionLayout: MotionLayout?, id: Int) {
                if (id == R.id.end) {
                    binding.searchView.showKeyboard()
                    if (binding.toggleBackdrop.isChecked) {
                        bottomSheetBehavior?.peekHeight = backdropHeaderHeight + insetBottom
                    }
                } else {
                    setBottomSheetPeekHeight()
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) =
                Unit
        })

        binding.searchButton.setOnCheckedChangeListener {
            if (isSearchMode()) binding.motionToolbar.transitionToStart()
            else binding.motionToolbar.transitionToEnd()
        }

        binding.toggleBackdrop.setOnCheckedChangeListener {
            toggleBackdrop()
        }
    }

    private fun setListeners() {
        binding.bottomSheet.setOnClickListener {
            if (bottomSheetBehavior?.isCollapsed() == true) {
                binding.bottleList.removeOnItemTouchListener(recyclerViewDisabler)
                binding.toggleBackdrop.toggle()
            }
        }

        binding.currentQuery.setOnClickListener {
            binding.searchButton.performClick()
        }

        binding.buttonSubmit.apply {
            setVisible(isPickMode)
            setOnClickListener {
                stepperFragment?.goToNextPage()
            }
        }

        binding.buttonSort.also { it.rotation = 90f }.setOnClickListener {
            showSortDialog()
        }
    }

    private fun setHeaderShadow(setVisible: Boolean) {
        val header = binding.backdropHeader

        if (setVisible && !isHeaderShadowDisplayed) {
            header.stateListAnimator = revealShadowAnim
            isHeaderShadowDisplayed = true
        } else if (!setVisible && isHeaderShadowDisplayed) {
            header.stateListAnimator = hideShadowAnim
            isHeaderShadowDisplayed = false
        }
    }

    private fun toggleBackdrop() {
        bottomSheetBehavior?.run {
            when {
                isExpanded() -> {
                    toggleState()
                    binding.scrim.alpha = 0.76f
                    binding.bottleList.addOnItemTouchListener(recyclerViewDisabler)
                }

                isCollapsed() -> {
                    toggleState()
                    binding.scrim.alpha = 0f
                    binding.bottleList.removeOnItemTouchListener(recyclerViewDisabler)
                }
            }
        }
    }

    private fun submitFriendFilter() {
        val chipGroupId = filtersBinding.friendChipGroup.id
        val friendFilter = getFriendFilter()
        val firstMapEntry = chipGroupId to friendFilter
        val chipConsume = filtersBinding.chipConsume
        chipConsume.isEnabled = !searchViewModel.shouldShowConsumedAndUnconsumedBottles()

        val secondMapEntry = filtersBinding.otherChipGroup.id to getOtherFilter()
        val filters = mapOf(firstMapEntry, secondMapEntry)

        searchViewModel.submitFilters(filters)
    }

    private fun initSearchView() {
        binding.searchView.apply {
            doAfterTextChanged { newText ->
                if (!newText.isNullOrEmpty()) {
                    binding.currentQuery.text =
                        context?.getString(R.string.query_feedback, newText).orEmpty()
                } else {
                    binding.currentQuery.text = ""
                }

                searchViewModel.submitFilter(id, FilterText(text.toString()))
            }

            setOnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    binding.searchButton.performClick()
                }

                true
            }
        }
    }

    private fun initStorageLocationDropdown(savedInstanceState: Bundle?) {
        val storageLocationEnabled = settingsViewModel.getEnableBottleStorageLocation()

        if (!storageLocationEnabled) {
            return
        }

        filtersBinding.apply {
            divider7.setVisible(true)
            storageLocationTitle.setVisible(true)
            storageLocationLayout.setVisible(true)
        }

        val adapter = ArrayAdapter<String>(requireContext(), R.layout.item_naming)
        val clearText = getString(R.string.all)
        val text = savedInstanceState?.getString(STORAGE_LOCATION) ?: clearText

        filtersBinding.storageLocation.apply {
            setText(text)
            setAdapter(adapter)
            doAfterTextChanged { newText ->
                val hasReset = newText.toString() == clearText
                val filter = if (hasReset) NoFilter else FilterStorageLocation(newText.toString())
                searchViewModel.submitFilter(id, filter)
            }
        }

        searchViewModel.getAllStorageLocations(clearText).observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)
        }
    }

    private fun getCountyFilter(): WineFilter {
        return filtersBinding.countyChipGroup
            .collectAs<County>()
            .also { searchViewModel.selectedCounties = it }
            .map { FilterCounty(it) }
            .fold(NoFilter as WineFilter) { acc, filterCounty -> acc.orCombine(filterCounty) }
    }

    private fun getOtherFilter(): WineFilter {
        // We want to handle specific consumed filter ourselves in this method
        val filters = getViewGroupFilters(filtersBinding.otherChipGroup)
            .filter { wineFilter -> wineFilter !is FilterConsumed }
            .toMutableList()

        val consumed = filtersBinding.chipConsume.isChecked
        val consumedFilter = when {
            isPickMode -> FilterConsumed(false)
            searchViewModel.shouldShowConsumedAndUnconsumedBottles() -> NoFilter
            consumed -> FilterConsumed(true)
            else -> FilterConsumed(false)
        }

        // Consumed have a special treatment since even if it is unchecked, a filter is necessary
        filters.add(consumedFilter)

        return filters.fold(NoFilter as WineFilter) { acc, filter ->
            acc.andCombine(filter ?: NoFilter)
        }
    }

    private fun getGrapeFilter(): WineFilter {
        return filtersBinding.grapeChipGroup
            .collectAs<Grape>()
            .also { searchViewModel.selectedGrapes = it }
            .map { FilterGrape(it) }
            .fold(NoFilter as WineFilter) { acc, filterGrape -> acc.orCombine(filterGrape) }
    }

    private fun getReviewFilter(): WineFilter {
        return filtersBinding.reviewChipGroup.collectAs<Review>()
            .also { searchViewModel.selectedReviews = it }
            .map { FilterReview(it) }
            .fold(NoFilter as WineFilter) { acc, filterReview -> acc.orCombine(filterReview) }
    }

    private fun getFriendFilter(): WineFilter {
        val consumedHistoryType = 0
        val givenToHistoryType = 2
        val givenByHistoryType = 3
        val filterMode = when (searchViewModel.friendFilterMode) {
            0 -> consumedHistoryType
            1 -> givenByHistoryType
            else /* 2 */ -> givenToHistoryType
        }

        return filtersBinding.friendChipGroup
            .collectAs<Friend>()
            .also { searchViewModel.selectedFriends = it }
            .map { FilterFriend(it.id, filterMode) }
            .fold(NoFilter as WineFilter) { acc, filterFriend -> acc.orCombine(filterFriend) }
    }

    private fun getTagFilter(): WineFilter {
        return filtersBinding.tagChipGroup.collectAs<Tag>()
            .also { searchViewModel.selectedTags = it }
            .map { FilterTag(it) }
            .fold(NoFilter as WineFilter) { acc, filterTag -> acc.orCombine(filterTag) }
    }

    private fun getPriceFilter(): WineFilter {
        if (!filtersBinding.togglePrice.isChecked || !filtersBinding.priceSlider.isLaidOut) {
            return NoFilter
        }

        val values = filtersBinding.priceSlider.values
        val min = values[0].toInt()
        val max = values[1].toInt()

        return FilterPrice(min, max)
    }

    private fun getVintageFilter(): WineFilter {
        with(filtersBinding.vintageSlider) {
            if (!isLaidOut) {
                return NoFilter
            }

            val min = valueFrom.toInt()
            val max = valueTo.toInt()
            val lowerBound = values[0].toInt()
            val higherBound = values[1].toInt()
            val isFullRange = lowerBound == min && higherBound == max

            return when {
                isFullRange -> NoFilter
                else -> FilterVintage(lowerBound, higherBound)
            }
        }
    }

    private fun getAlcoholFilter(): WineFilter {
        with(filtersBinding.alcoholSlider) {
            if (!isLaidOut) {
                return NoFilter
            }

            val min = valueFrom.toDouble()
            val max = valueTo.toDouble()
            val lowerBound = values[0].toDouble()
            val higherBound = values[1].toDouble()
            val isFullRange = lowerBound == min && higherBound == max

            return when {
                isFullRange -> NoFilter
                else -> FilterAlcohol(lowerBound, higherBound)
            }
        }
    }

    private fun getSelectedBottlesFilter(): WineFilter {
        return if (filtersBinding.chipSelected.isChecked) FilterSelected() else NoFilter
    }

    private fun getViewGroupFilters(viewGroup: ViewGroup): MutableList<WineFilter?> {
        return viewGroup.children.filter { (it as Checkable).isChecked && it.isEnabled }
            .map { it.getTag(R.string.tag_view_wine_filter) as WineFilter? }
            .toMutableList()
    }

    private fun combineFilters(wineFilters: List<WineFilter?>): WineFilter {
        return wineFilters.fold(NoFilter as WineFilter) { acc, filter ->
            acc.orCombine(filter ?: NoFilter)
        }
    }

    private fun setupCustomBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isSearchMode()) {
                binding.searchButton.performClick()
            } else {
                remove()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun showSortDialog() {
        val criterias = SortCriteria.entries.map { getString(it.value) }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_sort, criterias)
        val dialogBinding = DialogSortBinding.inflate(layoutInflater)

        val dialog = LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
            .setTitle(R.string.sorted_by)
            .setView(dialogBinding.root)
            .show()

        with(dialogBinding.choices) {
            divider = null
            this.adapter = adapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val reversed = dialogBinding.reverseSort.isChecked
                    searchViewModel.submitSortOrder(Sort(SortCriteria.entries[position], reversed))
                    dialog.dismiss()
                }
        }
    }

    private fun isSearchMode() = binding.motionToolbar.progress == 1F

    private fun fetchBackdropHeaderHeight() = binding.backdropHeader.height

    private fun loadRevealShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.show_elevation)

    private fun loadHideShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.hide_elevation)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (_filtersBinding == null) {
            return
        }

        val pos = when (filtersBinding.rbGroupSize.checkedButtonId) {
            R.id.rbSlim -> 0
            R.id.rbSmall -> 1
            R.id.rbNormal -> 2
            R.id.rbMagnum -> 3
            else /* View.NO_ID */ -> View.NO_ID
        }

        with(outState) {
            putFloat(SLIDER_VINTAGE_START, filtersBinding.vintageSlider.values[0])
            putFloat(SLIDER_VINTAGE_END, filtersBinding.vintageSlider.values[1])
            putBoolean(SWITCH_PRICE_ENABLED, filtersBinding.togglePrice.isChecked)
            putFloat(SLIDER_PRICE_START, filtersBinding.priceSlider.values[0])
            putFloat(SLIDER_PRICE_END, filtersBinding.priceSlider.values[1])
            putFloat(SLIDER_ALCOHOL_START, filtersBinding.alcoholSlider.values[0])
            putFloat(SLIDER_ALCOHOL_END, filtersBinding.alcoholSlider.values[1])
            putString(DATE_BEYOND, filtersBinding.beyond.text.toString())
            putString(DATE_UNTIL, filtersBinding.until.text.toString())
            putIntArray(CHIP_COLOR, filtersBinding.colorChipGroup.checkedChipIds.toIntArray())
            putIntArray(CHIP_MISC, filtersBinding.otherChipGroup.checkedChipIds.toIntArray())
            putBoolean(SWITCH_SELECTED_ENABLED, filtersBinding.chipSelected.isChecked)
            putString(STORAGE_LOCATION, filtersBinding.storageLocation.text.toString())
            putInt(RADIO_CAPACITY, pos)
        }
    }

    override fun onPause() {
        super.onPause()

        // Use save state when quitting fragment as well as when configuration change happens
        // But do not save wrong state if stub view hasn't been loaded yet
        if (_filtersBinding?.root?.isLaidOut == true) {
            val savedState = Bundle()
            this.onSaveInstanceState(savedState)
            searchViewModel.onFragmentLeaveSavedState = savedState
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        datePickerBeyond = null
        datePickerUntil = null
        bottlesAdapter = null
        bottomSheetBehavior = null
        _binding = null
        _filtersBinding = null
    }
}
