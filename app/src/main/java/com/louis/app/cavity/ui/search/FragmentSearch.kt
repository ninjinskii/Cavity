package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentSearchBinding
import com.louis.app.cavity.databinding.SearchFiltersBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.addtasting.AddTastingViewModel
import com.louis.app.cavity.ui.search.widget.RecyclerViewDisabler
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.util.*
import com.robinhood.ticker.TickerUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

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
        private const val DATE_BEYOND = "com.louis.app.cavity.DATE_BEYOND"
        private const val DATE_UNTIL = "com.louis.app.cavity.DATE_UNTIL"
        private const val CHIP_COLOR = "com.louis.app.cavity.CHIP_COLOR"
        private const val CHIP_MISC = "com.louis.app.cavity.CHIP_MISC"
        private const val SWITCH_SELECTED_ENABLED = "com.louis.app.cavity.SWITCH_SELECTED_ENABLED"

        const val PICK_MODE = "com.louis.app.cavity.ui.search.FragmentSearch.PICK_MODE"
    }

    private lateinit var bottlesAdapter: BottleRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var _filtersBinding: SearchFiltersBinding? = null
    private val filtersBinding get() = _filtersBinding!!

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

        isPickMode = arguments?.getBoolean(PICK_MODE) ?: false

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

        initRecyclerView()
        initTickerView()
        setupMenu()
        setListeners()
        initSearchView()
        setupCustomBackNav()

        lifecycleScope.launch {
            delay(800)
            inflateFiltersStub(savedInstanceState)
        }
    }

    private fun inflateFiltersStub(savedInstanceState: Bundle?) {
        val view = binding.filtersStub.inflate()
        _filtersBinding = SearchFiltersBinding.bind(view)

        filtersBinding.root.doOnLayout {
            setBottomSheetPeekHeight()
        }

        observe()
        initColorChips(savedInstanceState)
        initOtherChips(savedInstanceState)
        initDatePickers(savedInstanceState)
        initSliders(savedInstanceState)
    }

    private fun setBottomSheetPeekHeight() {
        if (_filtersBinding == null) {
            return
        }

        val height = binding.root.height
        val filtersBottom = filtersBinding.reviewScrollView.bottom
        val fill = height - filtersBottom - backdropHeaderHeight

        val peekHeight = max(backdropHeaderHeight, fill)
        bottomSheetBehavior.setPeekHeight(peekHeight, true)
    }

    private fun observe() {
        searchViewModel.getAllCounties().observe(viewLifecycleOwner) { counties ->
            val preselectedCounties = searchViewModel.selectedCounties.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(counties)
                .into(filtersBinding.countyChipGroup)
                .preselect(preselectedCounties)
                .doOnClick { prepareCountyFilters() }
                .emptyText(getString(R.string.empty_county))
                .build()
                .go()
        }

        searchViewModel.getAllGrapes().observe(viewLifecycleOwner) { grapes ->
            val preselectedGrapes = searchViewModel.selectedGrapes.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(grapes)
                .into(filtersBinding.grapeChipGroup)
                .preselect(preselectedGrapes)
                .doOnClick { prepareGrapeFilters() }
                .emptyText(getString(R.string.empty_grape_manager))
                .build()
                .go()

        }

        searchViewModel.getAllReviews().observe(viewLifecycleOwner) { reviews ->
            val preselectedReviews = searchViewModel.selectedReviews.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(reviews)
                .into(filtersBinding.reviewChipGroup)
                .preselect(preselectedReviews)
                .doOnClick { prepareReviewFilters() }
                .emptyText(getString(R.string.empty_review_manager))
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
        val savedState = savedInstanceState?.getIntArray(CHIP_COLOR)

        filtersBinding.colorChipGroup.apply {
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ ->
                    searchViewModel.setColorFilters(checkedChipIds)
                }

                savedState?.let { checkedIds ->
                    it.isChecked = it.id in checkedIds
                }
            }
        }
    }

    private fun initOtherChips(savedInstanceState: Bundle?) {
        val savedStateSelected = savedInstanceState?.getBoolean(SWITCH_SELECTED_ENABLED)
        val savedStateMisc = savedInstanceState?.getIntArray(CHIP_MISC)

        filtersBinding.chipSelected.apply {
            setVisible(isPickMode)
            isChecked = savedStateSelected ?: false
            setOnCheckedChangeListener { _, isChecked ->
                searchViewModel.setSelectedFilter(isChecked)
            }
        }

        filtersBinding.otherChipGroup.apply {
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ ->
                    searchViewModel.setOtherFilters(checkedChipIds)
                }

                savedStateMisc?.let { checkedIds ->
                    it.isChecked = it.id in checkedIds
                }
            }
        }
    }

    private fun initDatePickers(savedInstanceState: Bundle?) {
        val beyondTitle = getString(R.string.buying_date_beyond)
        val untilTitle = getString(R.string.buying_date_until)

        DatePicker(parentFragmentManager, filtersBinding.beyondLayout, beyondTitle).apply {
            onEndIconClickListener = { searchViewModel.setBeyondFilter(null) }
            onDateChangedListener = { searchViewModel.setBeyondFilter(it) }
        }


        DatePicker(parentFragmentManager, filtersBinding.untilLayout, untilTitle).apply {
            onEndIconClickListener = { searchViewModel.setUntilFilter(null) }
            onDateChangedListener = { searchViewModel.setUntilFilter(it) }
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
            values = listOf(start ?: valueFrom, end ?: valueTo)

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.setVintageFilter(
                        slider.values[0].toInt(),
                        slider.values[1].toInt()
                    )
                }

                override fun onStartTrackingTouch(slider: RangeSlider) = Unit
            })
        }

        filtersBinding.togglePrice.apply {
            val savedState = savedInstanceState?.getBoolean(SWITCH_PRICE_ENABLED)

            isChecked = savedState ?: false
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
                        val minPrice = if (isChecked) values[0].toInt() else -1
                        searchViewModel.setPriceFilter(minPrice, values[1].toInt())
                    }
                }
            }
        }

        filtersBinding.priceSlider.apply {
            val start = savedInstanceState?.getFloat(SLIDER_PRICE_START)
            val end = savedInstanceState?.getFloat(SLIDER_PRICE_END)

            values = listOf(start ?: valueFrom, end ?: valueTo)
            isEnabled = filtersBinding.togglePrice.isChecked

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.setPriceFilter(
                        slider.values[0].toInt(),
                        slider.values[1].toInt()
                    )
                }

                override fun onStartTrackingTouch(slider: RangeSlider) = Unit
            })
        }
    }

    private fun initRecyclerView() {
        bottlesAdapter = BottleRecyclerAdapter(
            transitionHelper,
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

        searchViewModel.results.observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            binding.matchingWines.text =
                resources.getQuantityString(R.plurals.matching_wines, it.size, it.size)
            bottlesAdapter.submitList(it.toMutableList())
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

    private fun prepareCountyFilters() {
        filtersBinding.countyChipGroup.apply {
            val counties = collectAs<County>()
            searchViewModel.setCountiesFilters(counties)
        }
    }

    private fun prepareGrapeFilters() {
        filtersBinding.grapeChipGroup.apply {
            val grapes = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Grape
            }

            searchViewModel.setGrapeFilters(grapes)
        }
    }

    private fun prepareReviewFilters() {
        filtersBinding.reviewChipGroup.apply {
            val reviews = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Review
            }

            searchViewModel.setReviewFilters(reviews)
        }
    }

    // Kwown issue: bottom sheet might and the toggle button might misbehave
// if for some reason the keyboard doesn't show up when calling showKeyboard()
    private fun setupMenu() {
        binding.motionToolbar.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, p0: Int, p1: Int) {
                if (motionLayout?.progress ?: 0F > 0.5F) {
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

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, id: Int) {
                if (id == R.id.end) {
                    binding.searchView.showKeyboard()
                    if (binding.toggleBackdrop.isChecked) {
                        bottomSheetBehavior.peekHeight = backdropHeaderHeight
                    }
                } else {
                    setBottomSheetPeekHeight()
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
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
            if (bottomSheetBehavior.isCollapsed()) {
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
        bottomSheetBehavior.run {
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

    private fun initSearchView() {
        binding.searchView.apply {
            doAfterTextChanged { newText ->
                if (newText != null && newText.isNotEmpty()) {
                    binding.currentQuery.text =
                        context?.getString(R.string.query_feedback, newText).orEmpty()
                } else {
                    binding.currentQuery.text = ""
                }

                searchViewModel.setTextFilter(newText.toString())
            }

            setOnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    binding.searchButton.performClick()
                }

                true
            }
        }
    }

    private fun setupCustomBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isSearchMode()) {
                binding.searchButton.performClick()
            } else {
                remove()
                requireActivity().onBackPressed()
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

        with(outState) {
            putFloat(SLIDER_VINTAGE_START, filtersBinding.vintageSlider.values[0])
            putFloat(SLIDER_VINTAGE_END, filtersBinding.vintageSlider.values[1])
            putBoolean(SWITCH_PRICE_ENABLED, filtersBinding.togglePrice.isChecked)
            putFloat(SLIDER_PRICE_START, filtersBinding.priceSlider.values[0])
            putFloat(SLIDER_PRICE_END, filtersBinding.priceSlider.values[1])
            putString(DATE_BEYOND, filtersBinding.beyond.text.toString())
            putString(DATE_UNTIL, filtersBinding.until.text.toString())
            putIntArray(CHIP_COLOR, filtersBinding.colorChipGroup.checkedChipIds.toIntArray())
            putIntArray(CHIP_MISC, filtersBinding.otherChipGroup.checkedChipIds.toIntArray())
            putBoolean(SWITCH_SELECTED_ENABLED, filtersBinding.chipSelected.isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _filtersBinding = null
    }
}
