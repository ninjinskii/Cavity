package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
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
    private val rvDisabler = RecyclerViewDisabler()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val backdropHeaderHeight by lazy { fetchBackdropHeaderHeight() }
    private val upperBoundHeight by lazy { fetchUpperBoundHeight() }
    private var isHeaderShadowDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
        }

        initCountyChips()
        initColorChips()
        initOtherChips()
        initRecyclerView()
        initSlider()
        setupMenu()
        setListener()
        initSearchView()
        setBottomSheetPeekHeight()
        restoreState()
    }

    private fun initCountyChips() {
        lifecycleScope.launch(IO) {
            val counties = searchViewModel.getAllCountiesNotLive().toSet()
            CountyLoader().loadCounties(
                lifecycleScope,
                layoutInflater,
                binding.countyChipGroup,
                counties,
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
                it.getColor(R.color.colorAccent)
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
            valueFrom = year - 40F
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
                        toggleBackdrop.postDelayed(500) { toggleBackdrop.performClick() }
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

                    if (bottomSheetBehavior.isCollapsed())
                        binding.toggleBackdrop.performClick()
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

                binding.searchButton.triggerAnimation()
            }
        }

        binding.toggleBackdrop.setOnClickListener { toggleBackdrop() }
    }

    private fun setListener() {
        binding.buttonMoreFilters.setOnClickListener {
            findNavController().navigate(R.id.searchToMoreFilters)
        }
    }

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
        binding.buttonMoreFilters.doOnLayout { upperBound ->
            val display = activity?.window?.decorView?.height
            val location = IntArray(2)

            display?.let {
                upperBound.getLocationInWindow(location)

                L.v(location[1].toString())
                val peekHeight =
                    if (it - location[1] - upperBoundHeight < backdropHeaderHeight)
                        backdropHeaderHeight
                    else
                        it - location[1] - upperBoundHeight

                bottomSheetBehavior.peekHeight = peekHeight
            }
        }
    }

    private fun setHeaderShadow(setVisible: Boolean) {
        val header = binding.backdropHeader

        if (setVisible && !isHeaderShadowDisplayed) {
            header.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.show_elevation)
            isHeaderShadowDisplayed = true
        } else if (!setVisible && isHeaderShadowDisplayed) {
            header.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.hide_elevation)
            isHeaderShadowDisplayed = false
        }
    }

    private fun toggleBackdrop() {
        if (bottomSheetBehavior.isExpanded()) {
            bottomSheetBehavior.toggleState()

            with(binding) {
                scrim.alpha = 0.76F
                recyclerView.addOnItemTouchListener(rvDisabler)
                toggleBackdrop.triggerAnimation()
            }
        } else if (bottomSheetBehavior.isCollapsed()) {
            bottomSheetBehavior.toggleState()

            with(binding) {
                scrim.alpha = 0F
                recyclerView.removeOnItemTouchListener(rvDisabler)
                toggleBackdrop.triggerAnimation()
            }
        }
    }

    private fun initSearchView() {
        binding.searchView.doAfterTextChanged { newText ->
            if (newText != null && newText.isNotEmpty()) {
                searchViewModel.setTextFilter(newText.toString())
                binding.currentQuery.text = newText
            } else {
                binding.currentQuery.text = ""
            }
        }
    }

    private fun restoreState() {
        with(searchViewModel.state) {
            counties?.let { selectedCounties ->
                binding.countyChipGroup.children.forEach {
                    if (((it.getTag(R.string.tag_chip_id)) as County).countyId in selectedCounties)
                        (it as Chip).isChecked = true
                }
            }

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
        binding.buttonMoreFilters.height + resources.getDimension(R.dimen.small_margin).toInt()

    override fun onResume() {
        //(activity as ActivityMain).hideMainToolbar()
        super.onResume()
    }

    override fun onPause() {
        //(activity as ActivityMain).showMainToolbar()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
