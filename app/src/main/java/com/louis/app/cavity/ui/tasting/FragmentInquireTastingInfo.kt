package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireTastingInfoBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.model.Temperature
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SimpleInputDialog

class FragmentInquireTastingInfo : Fragment(R.layout.fragment_inquire_tasting_info) {
    private var _binding: FragmentInquireTastingInfoBinding? = null
    private val binding get() = _binding!!
    private val tastingViewModel: TastingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireTastingInfoBinding.bind(view)

        initNumberPickers()
        initFriendChips()
        initDatePicker()
        setListeners()
    }

    private fun initNumberPickers() {
        // Number picker doesn't support negative values
        val freezerMaxValue = Temperature.MAX_FREEZER_TEMP.toLocaleTemp()
        val freezerMinValue = Temperature.MIN_FREEZER_TEMP.toLocaleTemp()
        val freezerMinPositive = freezerMinValue - freezerMinValue
        val freezerMaxPositive = freezerMaxValue - freezerMinValue

        with(binding) {
            cellarTemp.maxValue = Temperature.MAX_CELLAR_TEMP.toLocaleTemp()
            cellarTemp.minValue = Temperature.MIN_CELLAR_TEMP.toLocaleTemp()
            fridgeTemp.maxValue = Temperature.MAX_FRIDGE_TEMP.toLocaleTemp()
            fridgeTemp.minValue = Temperature.MIN_FRIDGE_TEMP.toLocaleTemp()
            freezerTemp.minValue = freezerMinPositive
            freezerTemp.maxValue = freezerMaxPositive
            freezerTemp.setFormatter { "${it + freezerMinValue}" }
        }

        tastingViewModel.lastTasting.observe(viewLifecycleOwner) {
            with(binding) {
                cellarTemp.value = (it?.cellarTemp ?: Temperature.DEFAULT_CELLAR_TEMP)
                    .toLocaleTemp()

                fridgeTemp.value = (it?.fridgeTemp ?: Temperature.DEFAULT_FRIDGE_TEMP)
                    .toLocaleTemp()

                freezerTemp.value = (it?.freezerTemp ?: Temperature.DEFAULT_FREEZER_TEMP)
                    .toLocaleTemp() - freezerMinValue
            }
        }
    }

    private fun initFriendChips() {
        val allFriends = mutableSetOf<Friend>()
        val alreadyInflated = mutableSetOf<Friend>()

        tastingViewModel.friends.observe(viewLifecycleOwner) {
//            allFriends.addAll(it)
//            val toInflate = allFriends - alreadyInflated
//            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(it)
                .into(binding.friendsChipGroup)
                .useAvatar(true)
                .selectable(true)
                .build()
                .go()
        }
    }

    private fun initDatePicker() {
        DatePicker(
            childFragmentManager,
            associatedTextLayout = binding.dateLayout,
            title = getString(R.string.tasting_date),
            defaultDate = System.currentTimeMillis()
        ).apply {
            onEndIconClickListener = { tastingViewModel.date = System.currentTimeMillis() }
            onDateChangedListener = { tastingViewModel.date = it }
        }
    }

    private fun setListeners() {
        binding.buttonAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        binding.buttonSubmit.setOnClickListener {
            submit()
        }
    }

    private fun showAddFriendDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_friend,
            hint = R.string.add_friend_label,
            icon = R.drawable.ic_person,
        ) {
            tastingViewModel.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
    }

    private fun submit() {
        val valid = binding.dateLayout.validate() and binding.opportunityLayout.validate()

        if (valid) {
            with(binding) {
                val opportunity = opportunity.text.toString().trim()

                tastingViewModel.submit(
                    opportunity,
                    cellarTemp.value,
                    fridgeTemp.value,
                    freezerTemp.value
                )
            }
        }
    }

    private fun Int.toLocaleTemp() = when (tastingViewModel.temperatureUnit) {
        0 -> Temperature.Celsius(this).value
        else -> Temperature.Fahrenheit(this).value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
