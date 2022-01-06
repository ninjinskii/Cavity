package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireTastingInfoBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.util.collectAs
import com.louis.app.cavity.util.setupNavigation

class FragmentInquireTastingInfo : Step(R.layout.fragment_inquire_tasting_info) {
    private var _binding: FragmentInquireTastingInfoBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val addTastingViewModel: AddTastingViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var datePicker: DatePicker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireTastingInfoBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        initFriendChips()
        initDatePicker()
        setListeners()
    }

    private fun initFriendChips() {
        addTastingViewModel.friends.observe(viewLifecycleOwner) {
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
        val constraint = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        datePicker = DatePicker(
            childFragmentManager,
            associatedTextLayout = binding.dateLayout,
            title = getString(R.string.tasting_date),
            clearable = false,
            defaultDate = System.currentTimeMillis(),
            constraint
        )
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
            addItemViewModel.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)
            .show(dialogResources)
    }

    private fun submit() {
        val valid = binding.dateLayout.validate() and binding.opportunityLayout.validate()

        if (valid) {
            with(binding) {
                val opportunity = opportunity.text.toString().trim()
                val date = datePicker.getDate() ?: System.currentTimeMillis()
                val isMidday = rbMidday.isChecked
                val friends = friendsChipGroup.collectAs<Friend>()

                addTastingViewModel.submitTasting(opportunity, isMidday, date, friends)
                stepperFragment?.requestNextPage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
