package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewTreeObserver
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
import com.google.android.material.datepicker.MaterialDatePicker
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
    private lateinit var beyondDatePicker: MaterialDatePicker<Long>
    private lateinit var untilDatePicker: MaterialDatePicker<Long>
    private val searchViewModel: SearchViewModel by viewModels()
    private val recyclerViewDisabler =
        RecyclerViewDisabler { binding.toggleBackdrop.performClick() }
    private val backdropHeaderHeight by lazy { fetchBackdropHeaderHeight() }
    private val upperBoundHeight by lazy { fetchUpperBoundHeight() }
    private val revealShadowAnim by lazy { loadRevealShadowAnim() }
    private val hideShadowAnim by lazy { loadHideShadowAnim() }
    private var isHeaderShadowDisplayed = false
    private var isDatePickerDisplayed = false

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

        setBottomSheetPeekHeight()
        initCountyChips()
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

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
//        binding.root.doOnLayout {
//            L.v("backropHeader");
//            val upperBound = binding.warning
//            val display = activity?.window?.decorView?.height
//            val location = IntArray(2)
//
//            display?.let {
//                upperBound.getLocationInWindow(location)
//
//                val peekHeight =
//                    if (it - location[1] - upperBoundHeight < backdropHeaderHeight)
//                        backdropHeaderHeight
//                    else
//                        it - location[1] - upperBoundHeight
//
//                bottomSheetBehavior.peekHeight = peekHeight
//            }
//        }

        binding.root.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val display = activity?.window?.decorView?.height
                    display?.let {
                        val warn = binding.warning.top

                        L.v("warning bottom $warn")
                        L.v("screen height $it")
                        L.v("final: ${it - warn}")
                        bottomSheetBehavior.peekHeight = Math.abs(it - warn)
                    }

                    //--------------------

//                    val upperBound = binding.warning
//                    val display = activity?.window?.decorView?.height
//                    val location = IntArray(2)
//
//                    display?.let {
//                        upperBound.getLocationInWindow(location)
//
//                        val peekHeight =
//                            if (it - location[1] - upperBoundHeight < backdropHeaderHeight)
//                                backdropHeaderHeight
//                            else
//                                it - location[1] - upperBoundHeight
//
//                        bottomSheetBehavior.peekHeight = peekHeight
//                    }
                }
            })
        }
    }

    private fun initCountyChips() {
        lifecycleScope.launch(IO) {
            val counties = searchViewModel.getAllCountiesNotLive().toSet()
            val preselect = searchViewModel.counties

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
        val colors = requireContext().run {
            listOf(
                getColor(R.color.wine_white),
                getColor(R.color.wine_red),
                getColor(R.color.wine_sweet),
                getColor(R.color.wine_rose),
                getColor(R.color.cavity_gold)
            )
        }

        bottlesAdapter = BottleRecyclerAdapter(colors) { wineId, bottleId ->
            val action = FragmentSearchDirections.searchToBottleDetails(wineId, bottleId)
            findNavController().navigate(action)
        }

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

    private fun initDatePickers() {
        beyondDatePicker = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText(R.string.buying_date_beyond)
        }.build()

        untilDatePicker = MaterialDatePicker.Builder.datePicker().apply {
            setTitleText(R.string.buying_date_until)
        }.build()

        binding.beyondLayout.setEndIconOnClickListener {
            binding.beyond.setText("")
            searchViewModel.setBeyondFilter(null)
        }

        binding.untilLayout.setEndIconOnClickListener {
            binding.until.setText("")
            searchViewModel.setUntilFilter(null)
        }

        beyondDatePicker.apply {
            addOnDismissListener {
                binding.beyond.clearFocus()
                isDatePickerDisplayed = false
            }

            addOnPositiveButtonClickListener {
                binding.beyond.setText(DateFormatter.formatDate(selection ?: 0))
                selection?.let {
                    searchViewModel.setBeyondFilter(it)
                }
            }
        }

        untilDatePicker.apply {
            addOnDismissListener {
                binding.until.clearFocus()
                isDatePickerDisplayed = false
            }

            addOnPositiveButtonClickListener {
                binding.until.setText(DateFormatter.formatDate(selection ?: 0))
                selection?.let {
                    searchViewModel.setUntilFilter(it)
                }
            }
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

    private fun setupMenu() {
        binding.motionToolbar.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, p0: Int, p1: Int) {
                if (motionLayout?.progress ?: 0F > 0.5F) {
                    setBottomSheetPeekHeight()

                    with(binding) {
                        currentQuery.setVisible(true)
                        searchView.hideKeyboard()
                    }
                } else {
                    binding.currentQuery.setVisible(false)

                    if (binding.toggleBackdrop.isChecked) {
                        //setBottomSheetPeekHeight()
                    }
                }
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, id: Int) {
                if (id == R.id.end) {
                    binding.searchView.showKeyboard()
                    L.v("header height: $backdropHeaderHeight")
                    bottomSheetBehavior.peekHeight = backdropHeaderHeight
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
                toggleBackdrop()
                binding.recyclerView.removeOnItemTouchListener(recyclerViewDisabler)
                binding.toggleBackdrop.toggle()
            }
        }

        binding.currentQuery.setOnClickListener {
            binding.searchButton.performClick()
        }

        binding.beyond.apply {
            inputType = InputType.TYPE_NULL

            setOnClickListener {
                if (!isDatePickerDisplayed) {
                    isDatePickerDisplayed = true

                    beyondDatePicker.show(
                        childFragmentManager,
                        resources.getString(R.string.tag_date_picker)
                    )
                }
            }
        }

        binding.until.apply {
            inputType = InputType.TYPE_NULL

            setOnClickListener {
                if (!isDatePickerDisplayed) {
                    isDatePickerDisplayed = true

                    untilDatePicker.show(
                        childFragmentManager,
                        resources.getString(R.string.tag_date_picker)
                    )
                }
            }
        }

        binding.togglePrice.setOnCheckedChangeListener { _, isChecked ->
            binding.priceSlider.apply {
                isEnabled = isChecked
                val minPrice = if (isChecked) values[0].toInt() else -1
                searchViewModel.setPriceFilter(minPrice, values[1].toInt())
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
                    binding.recyclerView.addOnItemTouchListener(recyclerViewDisabler)
                }
                isCollapsed() -> {
                    toggleState()
                    binding.scrim.alpha = 0f
                    binding.recyclerView.removeOnItemTouchListener(recyclerViewDisabler)
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

    private fun fetchUpperBoundHeight() = binding.warning.height

    private fun loadRevealShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.show_elevation)

    private fun loadHideShadowAnim() =
        AnimatorInflater.loadStateListAnimator(context, R.animator.hide_elevation)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
