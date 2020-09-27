package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentSearchBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.ui.home.WineRecyclerAdapter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSearch : Fragment(R.layout.fragment_search), CountyLoader {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var bottlesAdapter: WineRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var menu: Menu
    private val rvDisabler = RecyclerViewDisabler()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val backdropHeaderHeight by lazy { binding.backdropHeader.height }
    private val toggleShowBeforeHeight by lazy { binding.toggleShowBefore.height }
    private var isDatePickerDisplayed = false
    private var isHeaderShadowDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
        }

        setHasOptionsMenu(true)

        initCountyChips()
        initColorChips()
        initOtherChips()
        initRecyclerView()
        initDatePicker()
        setListeners()
        setBottomSheetPeekHeight()
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
                onCheckedChangeListener = { _, _ -> updateCheckedIds() }
            )
        }
    }

    private fun initColorChips() {
        binding.colorChipGroup.apply {
            clearCheck()
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ -> updateCheckedIds() }
            }
        }
    }

    private fun initOtherChips() {
        binding.otherChipGroup.apply {
            clearCheck()
            children.forEach {
                (it as Chip).setOnCheckedChangeListener { _, _ -> updateCheckedIds() }
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

        bottlesAdapter = WineRecyclerAdapter({}, {}, colors ?: emptyList())

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

    private fun initDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(R.string.buying_date)

        datePicker = builder.build()

        datePicker.apply {
            addOnDismissListener {
                binding.date.clearFocus()
                isDatePickerDisplayed = false
            }

            addOnPositiveButtonClickListener {
                binding.date.setText(headerText)
            }
        }
    }

    // Material does not trigger listeners on mutli-select, we have to listen on every chip and maintain a state
    private fun updateCheckedIds() {
        binding.countyChipGroup.run {
            val colorFilterCheckedChipIds = binding.colorChipGroup.checkedChipIds
            val otherFilterCheckedChipIds = binding.otherChipGroup.checkedChipIds
            val counties = checkedChipIds.map {
                findViewById<Chip>(it).getTag(R.string.tag_chip_id) as County
            }

            searchViewModel.filter(counties, colorFilterCheckedChipIds , otherFilterCheckedChipIds)
        }
    }

    private fun setListeners() {
        binding.date.apply {
            inputType = InputType.TYPE_NULL

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (!isDatePickerDisplayed) {
                        isDatePickerDisplayed = true

                        datePicker.show(
                            childFragmentManager,
                            resources.getString(R.string.tag_date_picker)
                        )
                    }
                }
            }
        }
    }

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
        lifecycleScope.launch(Main) {
            delay(300)
            val display = activity?.window?.decorView?.height
            val location = IntArray(2)

            display?.let {
                binding.toggleShowBefore.getLocationInWindow(location)

                val peekHeight =
                    if (it - location[1] - toggleShowBeforeHeight < backdropHeaderHeight)
                        backdropHeaderHeight
                    else
                        it - location[1] - binding.toggleShowBefore.height

                bottomSheetBehavior.setPeekHeight(
                    peekHeight,
                    true
                )
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

    override fun onResume() {
        (activity as ActivityMain).setToolbarShadow(false)
        super.onResume()
    }

    override fun onPause() {
        (activity as ActivityMain).setToolbarShadow(true)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
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
}