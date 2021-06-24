package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireTastingInfoBinding
import com.louis.app.cavity.model.Friend
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
        with(binding) {
            cellarTemp.maxValue = resources.getInteger(R.integer.max_cellar_temp)
            cellarTemp.minValue = resources.getInteger(R.integer.min_cellar_temp)
            fridgeTemp.maxValue = resources.getInteger(R.integer.max_fridge_temp)
            fridgeTemp.minValue = resources.getInteger(R.integer.min_fridge_temp)
            freezerTemp.maxValue = resources.getInteger(R.integer.max_freezer_temp)
            freezerTemp.minValue = resources.getInteger(R.integer.min_freezer_temp)
        }

        tastingViewModel.lastTasting.observe(viewLifecycleOwner) {
            with(binding) {
                cellarTemp.value = it.cellarTemp
                fridgeTemp.value = it.fridgeTemp
                freezerTemp.value = it.freezerTemp
            }
        }
    }

    private fun initFriendChips() {
        val allFriends = mutableSetOf<Friend>()
        val alreadyInflated = mutableSetOf<Friend>()

        tastingViewModel.friends.observe(viewLifecycleOwner) {
            allFriends.addAll(it)
            val toInflate = allFriends - alreadyInflated
            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(toInflate.toList())
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
        tastingViewModel
            .submit(binding.opportunityLayout.validate() && binding.dateLayout.validate())
        if (binding.opportunityLayout.validate() && binding.dateLayout.validate()) {
            // save to view model, navigate
        } else {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
