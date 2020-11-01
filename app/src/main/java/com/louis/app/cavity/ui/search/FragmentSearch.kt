package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
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
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentSearch : Fragment(R.layout.fragment_search), CountyLoader {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottlesAdapter: BottleRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var menu: Menu
    private val rvDisabler = RecyclerViewDisabler()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val backdropHeaderHeight by lazy { binding.backdropHeader.height }
    private val bottomSheetUpperBound by lazy {
        binding.buttonMoreFilters.height + resources.getDimension(R.dimen.small_margin).toInt()
    }
    private var isHeaderShadowDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED // Expanded
            isHideable = false
        }

        setHasOptionsMenu(true) // TODO: remove

        initCountyChips()
        initColorChips()
        initOtherChips()
        initRecyclerView()
        initSlider()
        setupMenu()
        setListener()
        setBottomSheetPeekHeight()
        restoreState()
    }

    private fun initCountyChips() {
        lifecycleScope.launch(IO) {
            val counties = searchViewModel.getAllCountiesNotLive().toSet()
            loadCounties(
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
        binding.searchButton.setOnClickListener {
            binding.searchButton.triggerAnimation()
            binding.motionToolbar.transitionToEnd()
        }
    }

    private fun setListener() {
        binding.buttonMoreFilters.setOnClickListener {
            findNavController().navigate(R.id.searchToMoreFilters)
        }
    }

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
        lifecycleScope.launch(Main) {
            delay(300)
            val display = activity?.window?.decorView?.height
            val location = IntArray(2)

            display?.let {
                binding.buttonMoreFilters.getLocationInWindow(location)

                val peekHeight =
                    if (it - location[1] - bottomSheetUpperBound < backdropHeaderHeight)
                        backdropHeaderHeight
                    else
                        it - location[1] - bottomSheetUpperBound

                bottomSheetBehavior.setPeekHeight(peekHeight, true)
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
        val item = menu.getItem(1)

        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            item.setIcon(R.drawable.anim_close_filter)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.scrim.alpha = 0.76F
            binding.recyclerView.addOnItemTouchListener(rvDisabler)
        } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            item.setIcon(R.drawable.anim_filter_close)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            binding.scrim.alpha = 0F
            binding.recyclerView.removeOnItemTouchListener(rvDisabler)
        }

        (item.icon as AnimatedVectorDrawable).start()
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchViewModel.setTextFilter(it) }

                return true
            }
        })
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

    override fun onResume() {
        //(activity as ActivityMain).setToolbarShadow(false)
        (activity as ActivityMain).hideMainToolbar()
        super.onResume()
    }

    override fun onPause() {
        //(activity as ActivityMain).setToolbarShadow(true)
        (activity as ActivityMain).showMainToolbar()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu.findItem(R.id.searchBar).actionView as SearchView)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toggleBackdrop -> {
                toggleBackdrop()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
