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
import com.louis.app.cavity.util.L

class FragmentMoreFilters : Fragment(R.layout.fragment_more_filters) {
    private lateinit var datePicker: MaterialDatePicker<Long>
    private var _binding: FragmentMoreFiltersBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by activityViewModels()
    private var isDatePickerDisplayed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMoreFiltersBinding.bind(view)

        initDatePicker()
        initSliders()
        initTextSwitcher()
        observe()
        restoreState()
        setListeners()
    }

    private fun initDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(R.string.buying_date)

        binding.dateLayout.setEndIconOnClickListener {
            binding.date.setText("")
            binding.toggleShowBefore.isEnabled = false
            searchViewModel.setDateFilter(-1, searchBefore = false)
        }

        datePicker = builder.build()

        datePicker.apply {
            addOnDismissListener {
                binding.date.clearFocus()
                isDatePickerDisplayed = false
            }

            addOnPositiveButtonClickListener {
                binding.date.setText(DateFormatter.formatDate(selection ?: 0))
                binding.toggleShowBefore.isEnabled = true
                selection?.let {
                    searchViewModel.setDateFilter(it, binding.toggleShowBefore.isChecked)
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
                    L.v("${it.first}, ${it.second}")
                    priceSlider.values = listOf(it.first.toFloat(), it.second.toFloat())
                }
            }

            date?.let {
                val dateString = DateFormatter.formatDate(it.first)

                with(binding) {
                    date.setText(dateString)
                    toggleShowBefore.isChecked = it.second
                    toggleShowBefore.isEnabled = true
                }
            }

            stock?.let {
                binding.stockSlider.values = listOf(it.first.toFloat(), it.second.toFloat())
            }
        }
    }

    private fun setListeners() {
        binding.date.apply {
            inputType = InputType.TYPE_NULL

            setOnClickListener {
                if (!isDatePickerDisplayed) {
                    isDatePickerDisplayed = true

                    datePicker.show(
                        childFragmentManager,
                        resources.getString(R.string.tag_date_picker)
                    )
                }
            }
        }

        binding.toggleShowBefore.setOnCheckedChangeListener { _, isChecked ->
            datePicker.selection?.let {
                searchViewModel.setDateFilter(it, isChecked)
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
