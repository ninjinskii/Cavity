package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentGiftBottleBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SnackbarProvider
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FragmentGiftBottle : Fragment(R.layout.fragment_gift_bottle) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentGiftBottleBinding? = null
    private val binding get() = _binding!!
    private val consumeGiftBottleViewModel: ConsumeGiftBottleViewModel by viewModels()
    private val args: FragmentUseBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGiftBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        initDatePicker()
        initChips()
        setListeners()
    }

    private fun initDatePicker() {
        val title = getString(R.string.consume_date)

        DatePicker(
            childFragmentManager,
            binding.giftDateLayout,
            title,
            System.currentTimeMillis()
        ).apply {
            onEndIconClickListener = { consumeGiftBottleViewModel.date = System.currentTimeMillis() }
            onDateChangedListener = { consumeGiftBottleViewModel.date = it }
        }
    }

    private fun initChips() {
        lifecycleScope.launch(IO) {
            ChipLoader(lifecycleScope, layoutInflater).apply {
                loadChips(
                    binding.friendsChipGroup,
                    consumeGiftBottleViewModel.getAllFriendsNotLive(),
                    preselect = emptyList(),
                    selectionRequired = false,
                )
            }
        }
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            if (!binding.giftDateLayout.validate()) {
                return@setOnClickListener
            }

            binding.friendsChipGroup.apply {
                val friends = checkedChipIds.map {
                    (findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Friend).id
                }

                consumeGiftBottleViewModel.consumeBottle(args.bottleId, "", friends)
            }

            snackbarProvider.onShowSnackbarRequested(
                R.string.bottle_gifted,
                useAnchorView = false
            )
            findNavController().navigateUp()
        }

        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
