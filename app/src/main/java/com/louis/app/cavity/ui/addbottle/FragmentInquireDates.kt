package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireDatesBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.viewmodel.DateManager
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.ui.widget.Rule
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.clearInputMethodLeak
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import java.util.*

class FragmentInquireDates : Step(R.layout.fragment_inquire_dates) {
    private lateinit var dateManager: DateManager
    private var datePicker: DatePicker? = null
    private var _binding: FragmentInquireDatesBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireDatesBinding.bind(view)

        dateManager = addBottleViewModel.dateManager

        applyInsets()
        initNumberPickers()
        initQuantityRule()
        initCurrencyDropdown()
        initBuyLocationDropdown()
        setListeners()
        observe()
    }

    private fun applyInsets() {
        binding.nestedScrollView.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initNumberPickers() {
        val year = Calendar.getInstance().get(Calendar.YEAR)

        binding.vintage.apply {
            minValue = year - 30
            maxValue = year
            value = year - 5
        }

        binding.apogee.apply {
            minValue = year - 20
            maxValue = year + 30
            value = year + 5
        }
    }

    private fun initQuantityRule() {
        binding.countLayout.addRules(Rule(R.string.count_limit) { it.toInt() <= MAX_BOTTLE_COUNT })
    }

    private fun initCurrencyDropdown() {
        val items = resources.getStringArray(R.array.currencies)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        binding.currency.apply {
            setAdapter(adapter)
            val default = settingsViewModel.getDefaultCurrency()
            setText(default, false)
        }
    }

    private fun initBuyLocationDropdown() {
        val adapter = ArrayAdapter<String>(requireContext(), R.layout.item_naming)

        binding.buyLocation.setAdapter(adapter)

        addBottleViewModel.buyLocations.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)
        }
    }

    private fun setListeners() {
        val title = getString(R.string.buying_date)
        datePicker = DatePicker(
            childFragmentManager,
            binding.buyDateLayout,
            title,
            defaultDate = System.currentTimeMillis()
        ).apply {
            onEndIconClickListener = { dateManager.setBuyDate(System.currentTimeMillis()) }
            onDateChangedListener = { dateManager.setBuyDate(it) }
        }

        binding.apogeeEnabled.setOnCheckedChangeListener { _, isChecked ->
            val opacity = if (isChecked) 1f else 0.3f

            with(binding) {
                apogee.isEnabled = isChecked
                apogee.alpha = opacity
                apogeeText.alpha = opacity
            }
        }
    }

    private fun observe() {
        addBottleViewModel.editedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        val formattedPrice = editedBottle.price.let { if (it != -1F) it.toString() else "" }

        with(binding) {
            vintage.value = editedBottle.vintage
            apogee.value =
                editedBottle.apogee ?: editedBottle.vintage.also { apogeeEnabled.isChecked = false }
            price.setText(formattedPrice)
            currency.setText(editedBottle.currency, false)
            buyLocation.setText(editedBottle.buyLocation)
            buyDate.setText(DateFormatter.formatDate(editedBottle.buyDate))
            count.setVisible(false)
        }
    }

    private fun savePartialBottle() {
        with(binding) {
            val apogee = if (apogeeEnabled.isChecked) apogee.value else null
            val count = count.text.toString().trim().toInt()
            val price = price.text.toString().trim()
            val formattedPrice = if (price.isEmpty()) -1F else price.toFloat()
            val currency = currency.text.toString()
            val location = buyLocation.text.toString().trim()

            dateManager.submitDates(
                vintage.value,
                apogee,
                count,
                formattedPrice,
                currency,
                location,
            )
        }
    }

    override fun requestNextPage(): Boolean {
        val isFormValid = binding.countLayout.validate() &&
                binding.priceLayout.validate() &&
                binding.buyDateLayout.validate()

        if (isFormValid) {
            savePartialBottle()
            return true
        }

        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearInputMethodLeak()
        datePicker?.dispose()
        _binding = null
    }

    companion object {
        private const val MAX_BOTTLE_COUNT = 20
    }
}
