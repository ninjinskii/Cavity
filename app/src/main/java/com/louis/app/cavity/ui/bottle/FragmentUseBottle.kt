package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentUseBottleBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.util.DateFormatter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FragmentUseBottle : Fragment(R.layout.fragment_use_bottle) {
    private lateinit var useDatePicker: MaterialDatePicker<Long>
    private var _binding: FragmentUseBottleBinding? = null
    private val binding get() = _binding!!
    private val useBottleViewModel: UseBottleViewModel by viewModels()
    private val args: FragmentUseBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUseBottleBinding.bind(view)

        initDatePicker()
        initChips()
        setListeners()
    }

    private fun initDatePicker() {
        useDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.buying_date_beyond)
            .build()

        binding.useDateLayout.setEndIconOnClickListener {
            binding.useDate.setText("")
            useBottleViewModel.date = System.currentTimeMillis()
        }

        useDatePicker.apply {
            addOnDismissListener {
                binding.useDate.clearFocus()
            }

            addOnPositiveButtonClickListener {
                binding.useDate.setText(DateFormatter.formatDate(selection ?: 0))
                selection?.let {
                    useBottleViewModel.date = it
                }
            }
        }
    }

    private fun initChips() {
        lifecycleScope.launch(IO) {
            ChipLoader(lifecycleScope, layoutInflater).apply {
                loadChips(
                    binding.friendsChipGroup,
                    useBottleViewModel.getAllFriendsNotLive(),
                    preselect = emptyList(),
                    selectionRequired = false,
                )
            }
        }
    }

    private fun setListeners() {
        binding.useDate.apply {
            inputType = InputType.TYPE_NULL

            setOnClickListener {
                useDatePicker.show(
                    childFragmentManager,
                    resources.getString(R.string.tag_date_picker)
                )
            }
        }

        binding.buttonSubmit.setOnClickListener {
            binding.friendsChipGroup.apply {
                val friends = checkedChipIds.map {
                    (findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Friend).id
                }

                useBottleViewModel.useBottle(args.bottleId, friends)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
