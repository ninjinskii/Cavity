package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
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
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.search.widget.RecyclerViewDisabler
import com.louis.app.cavity.util.*
import java.util.*
import kotlin.math.max

class FragmentSearch : Fragment(R.layout.fragment_search) {
    companion object {
        const val PICK_MODE = "com.louis.app.cavity.ui.search.FragmentSearch.PICK_MODE"
    }

    private lateinit var bottlesAdapter: BottleRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    private val recyclerViewDisabler = RecyclerViewDisabler { binding.toggleBackdrop.toggle() }
    private val backdropHeaderHeight by lazy { fetchBackdropHeaderHeight() }
    private val revealShadowAnim by lazy { loadRevealShadowAnim() }
    private val hideShadowAnim by lazy { loadHideShadowAnim() }
    private var isHeaderShadowDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupNavigation(binding.fakeToolbar)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
            saveFlags = BottomSheetBehavior.SAVE_NONE
        }

        binding.fakeToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.root.doOnLayout {
            setBottomSheetPeekHeight()
        }

        observe()
        initColorChips()
        initOtherChips()
        initRecyclerView()
        initDatePickers()
        initSliders()
        setupMenu()
        setListeners()
        initSearchView()
        setupCustomBackNav()
    }

    private fun setBottomSheetPeekHeight() {
        val fill = binding.root.height - binding.reviewScrollView.bottom - backdropHeaderHeight
        val peekHeight = max(backdropHeaderHeight, fill)
        bottomSheetBehavior.setPeekHeight(peekHeight, true)
    }

    private fun observe() {
        searchViewModel.getAllCounties().observe(viewLifecycleOwner) { counties ->
            L.timestamp("map") {
                searchViewModel.selectedCounties.map { it.id }
            }
            val preselectedCounties = searchViewModel.selectedCounties.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(counties)
                .into(binding.countyChipGroup)
                .preselect(preselectedCounties)
                .doOnClick { prepareCountyFilters() }
                .build()
                .go()
        }

        searchViewModel.getAllGrapes().observe(viewLifecycleOwner) { grapes ->
            val preselectedGrapes = searchViewModel.selectedGrapes.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(grapes)
                .into(binding.grapeChipGroup)
                .preselect(preselectedGrapes)
                .doOnClick { prepareGrapeFilters() }
                .build()
                .go()

        }

        searchViewModel.getAllReviews().observe(viewLifecycleOwner) { reviews ->
            val preselectedReviews = searchViewModel.selectedReviews.map { it.id }
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(reviews)
                .into(binding.reviewChipGroup)
                .preselect(preselectedReviews)
                .doOnClick { prepareReviewFilters() }
                .build()
                .go()
        }
    }

    private fun initColorChips() {
        binding.colorChipGroup.apply {
            clearCheck()
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ ->
                    searchViewModel.setColorFilters(checkedChipIds)
                }
            }
        }
    }

    private fun initOtherChips() {
        binding.otherChipGroup.apply {
            clearCheck()
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ ->
                    searchViewModel.setOtherFilters(checkedChipIds)
                }
            }
        }
    }

    private fun initRecyclerView() {
        val isPickMode = arguments?.getBoolean(PICK_MODE) ?: false

        bottlesAdapter = BottleRecyclerAdapter(isPickMode) { wineId, bottleId ->
            val action = FragmentSearchDirections.searchToBottleDetails(wineId, bottleId)
            binding.searchView.hideKeyboard()
            findNavController().navigate(action)
        }

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
            L.v("received event")
            binding.matchingWines.text =
                resources.getQuantityString(R.plurals.matching_wines, it.size, it.size)
            bottlesAdapter.submitList(it.toMutableList())
        }
    }

    private fun initDatePickers() {
        val beyondTitle = getString(R.string.buying_date_beyond)
        val untilTitle = getString(R.string.buying_date_until)

        DatePicker(parentFragmentManager, binding.beyondLayout, beyondTitle).apply {
            onEndIconClickListener = { searchViewModel.setBeyondFilter(null) }
            onDateChangedListener = { searchViewModel.setBeyondFilter(it) }
        }

        DatePicker(parentFragmentManager, binding.untilLayout, untilTitle).apply {
            onEndIconClickListener = { searchViewModel.setUntilFilter(null) }
            onDateChangedListener = { searchViewModel.setUntilFilter(it) }
        }
    }

    private fun initSliders() {
        binding.vintageSlider.apply {
            val year = Calendar.getInstance().get(Calendar.YEAR).toFloat()
            valueFrom = year - 20F
            valueTo = year
            values = listOf(valueFrom, valueTo)

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.setVintageFilter(
                        slider.values[0].toInt(),
                        slider.values[1].toInt()
                    )
                }

                override fun onStartTrackingTouch(slider: RangeSlider) {
                }
            })
        }

        binding.priceSlider.apply {
            isEnabled = false

            addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    searchViewModel.setPriceFilter(
                        slider.values[0].toInt(),
                        slider.values[1].toInt()
                    )
                }

                override fun onStartTrackingTouch(slider: RangeSlider) {
                }
            })
        }
    }

    private fun prepareCountyFilters() {
        binding.countyChipGroup.apply {
            val counties = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as County
            }

            searchViewModel.setCountiesFilters(counties)
        }
    }

    private fun prepareGrapeFilters() {
        binding.grapeChipGroup.apply {
            val grapes = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Grape
            }

            searchViewModel.setGrapeFilters(grapes)
        }
    }

    private fun prepareReviewFilters() {
        binding.reviewChipGroup.apply {
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

        binding.togglePrice.setOnCheckedChangeListener { _, isChecked ->
            binding.priceSlider.apply {
                // Making sure the view has its chance to restore it state before grabbing values
                doOnLayout {
                    isEnabled = isChecked
                    val minPrice = if (isChecked) values[0].toInt() else -1
                    searchViewModel.setPriceFilter(minPrice, values[1].toInt())
                }
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

    override fun onResume() {
        super.onResume()

        setHeaderShadow(binding.bottleList.canScrollVertically(-1))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
