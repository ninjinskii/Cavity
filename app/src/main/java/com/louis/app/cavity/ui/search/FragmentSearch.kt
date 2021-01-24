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
import androidx.fragment.app.activityViewModels
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
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.ui.search.widget.RecyclerViewDisabler
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

class FragmentSearch : Fragment(R.layout.fragment_search) {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottlesAdapter: BottleRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val recyclerViewDisabler = RecyclerViewDisabler()
    private val backdropHeaderHeight by lazy { fetchBackdropHeaderHeight() }
    private val upperBoundHeight by lazy { fetchUpperBoundHeight() }
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
        }

        binding.fakeToolbar.setNavigationOnClickListener {
            searchViewModel.reset()
            findNavController().navigateUp()
        }

        setBottomSheetPeekHeight()
        initCountyChips()
        initColorChips()
        initOtherChips()
        initRecyclerView()
        initSlider()
        setupMenu()
        setListeners()
        initSearchView()
        setupCustomBackNav()
        restoreState()
    }

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
        binding.untilLayout.doOnLayout { upperBound ->
            val display = activity?.window?.decorView?.height
            val location = IntArray(2)

            display?.let {
                upperBound.getLocationInWindow(location)

                val peekHeight =
                    if (it - location[1] - upperBoundHeight < backdropHeaderHeight)
                        backdropHeaderHeight
                    else
                        it - location[1] - upperBoundHeight

                bottomSheetBehavior.peekHeight = peekHeight
            }

            removeStubChip()
        }
    }

    private fun removeStubChip() {
        binding.countyChipGroup.removeAllViews()
    }

    private fun initCountyChips() {
        lifecycleScope.launch(IO) {
            val counties = searchViewModel.getAllCountiesNotLive().toSet()
            val preselect = searchViewModel.state.counties.orEmpty()

            CountyLoader().loadCounties(
                lifecycleScope,
                layoutInflater,
                binding.countyChipGroup,
                counties,
                preselect,
                selectionRequired = false,
                onCheckedChangeListener = { _, _ -> prepareCountyFilters() }
            )
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
        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.cavity_gold)
            )
        }

        bottlesAdapter = BottleRecyclerAdapter({}, colors ?: return)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = bottlesAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Add shadow when RV is not on top
                    if (!recyclerView.canScrollVertically(-1))
                        setHeaderShadow(false)
                    else
                        setHeaderShadow(true)
                }
            })
        }

        searchViewModel.results.observe(viewLifecycleOwner) {
            binding.matchingWines.text =
                resources.getQuantityString(R.plurals.matching_wines, it.size, it.size)
            bottlesAdapter.submitList(it)
        }
    }

    private fun initSlider() {
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
    }

    private fun prepareCountyFilters() {
        binding.countyChipGroup.apply {
            val counties = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as County
            }

            searchViewModel.setCountiesFilters(counties)
        }
    }

    private fun setupMenu() {
        binding.motionToolbar.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, p1: Int, p2: Int) {
                // When this callback is trigerred, the progress is already lower than 1, forcing us to check for a lower magic value.
                if (motionLayout?.progress ?: 0F > 0.5F) {
                    with(binding) {
                        currentQuery.setVisible(true)
                        searchView.hideKeyboard()
                    }
                } else {
                    binding.currentQuery.setVisible(false)
                }
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, id: Int) {
                if (isSearchMode()) {
                    binding.searchView.showKeyboard()
                }
            }

            override fun onTransitionTrigger(
                p0: MotionLayout?,
                p1: Int,
                p2: Boolean,
                p3: Float
            ) {
            }
        })

        binding.searchButton.setOnClickListener {
            if (!isToolbarAnimRunning()) {
                if (isSearchMode()) binding.motionToolbar.transitionToStart()
                else binding.motionToolbar.transitionToEnd()
            }
        }

        binding.toggleBackdrop.setOnClickListener { toggleBackdrop() }
    }

    private fun setListeners() {
//        binding.buttonMoreFilters.setOnClickListener {
//            findNavController().navigate(R.id.searchToMoreFilters)
//        }

        binding.currentQuery.setOnClickListener {
            binding.searchButton.performClick()
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
        with(bottomSheetBehavior) {
            if(isExpanded()) {
                toggleState()
                binding.scrim.alpha = 0.76f
                binding.recyclerView.addOnItemTouchListener(recyclerViewDisabler)
            } else if (isCollapsed()) {
                toggleState()
                binding.scrim.alpha = 0f
                binding.recyclerView.removeOnItemTouchListener(recyclerViewDisabler)
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
                searchViewModel.reset()
                remove()
                requireActivity().onBackPressed()
            }
        }
    }

    private fun restoreState() {
        // See initCountyChip for selected couties restoration
        with(searchViewModel.state) {
            colors?.let { selectedChipIds ->
                selectedChipIds.forEach { binding.root.findViewById<Chip>(it).isChecked = true }
            }

            others?.let { selectedChipIds ->
                selectedChipIds.forEach { binding.root.findViewById<Chip>(it).isChecked = true }
            }

            vintage?.let {
                binding.vintageSlider.values = listOf(it.first.toFloat(), it.second.toFloat())
            }
        }
    }

    private fun isSearchMode() = binding.motionToolbar.progress == 1F

    private fun isToolbarAnimRunning() = binding.motionToolbar.progress !in listOf(0F, 1F)

    private fun fetchBackdropHeaderHeight() = binding.backdropHeader.height

    private fun fetchUpperBoundHeight() =
        binding.untilLayout.height + resources.getDimension(R.dimen.small_margin).toInt()

    private fun loadRevealShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.show_elevation)

    private fun loadHideShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.hide_elevation)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
