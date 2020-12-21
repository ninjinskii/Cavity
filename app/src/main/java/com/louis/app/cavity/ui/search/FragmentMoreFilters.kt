package com.louis.app.cavity.ui.search

import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentMoreFiltersBinding
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.setupNavigation

class FragmentMoreFilters : Fragment(R.layout.fragment_more_filters) {
    private lateinit var beyondDatePicker: MaterialDatePicker<Long>
    private lateinit var untilDatePicker: MaterialDatePicker<Long>
    private var _binding: FragmentMoreFiltersBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by activityViewModels()
    private var isDatePickerDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMoreFiltersBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        initDatePickers()
        initSliders()
        initTextSwitcher()
        observe()
        restoreState()
        setListeners()
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

        binding.stockSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
            override fun onStopTrackingTouch(slider: RangeSlider) {
                searchViewModel.setStockFilter(slider.values[0].toInt(), slider.values[1].toInt())
            }

            override fun onStartTrackingTouch(slider: RangeSlider) {
            }
        })
    }

    private fun initTextSwitcher() {
        binding.resultsSwitcher.apply {
            inAnimation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
            outAnimation = AnimationUtils.loadAnimation(activity, R.anim.slide_out_bottom)

            setFactory {
                TextView(ContextThemeWrapper(activity, R.style.CavityTheme), null, 0).apply {
                    setTextAppearance(R.style.TextAppearance_MaterialComponents_Headline5)
                }
            }
        }
    }

    private fun observe() {
        var previousValue = 0

        searchViewModel.results.observe(viewLifecycleOwner) {
            if (previousValue != it.size) {
                binding.resultsSwitcher.setText(it.size.toString())
                previousValue = it.size
            }
        }
    }

    private fun restoreState() {
        with(searchViewModel.state) {
            price?.let {
                with(binding) {
                    togglePrice.isChecked = true
                    priceSlider.isEnabled = true
                    priceSlider.values = listOf(it.first.toFloat(), it.second.toFloat())
                }
            }

            date?.let {
                val beyondString = DateFormatter.formatDate(it.first)
                val untilString = DateFormatter.formatDate(it.second)

                with(binding) {
                    beyond.setText(beyondString)
                    until.setText(untilString)
                }
            }

            stock?.let {
                binding.stockSlider.values = listOf(it.first.toFloat(), it.second.toFloat())
            }
        }
    }

    private fun setListeners() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
