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
import com.louis.app.cavity.databinding.FragmentUseBottleBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SnackbarProvider
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FragmentUseBottle : Fragment(R.layout.fragment_use_bottle) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentUseBottleBinding? = null
    private val binding get() = _binding!!
    private val useBottleViewModel: UseBottleViewModel by viewModels()
    private val args: FragmentUseBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUseBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        initDatePicker()
        initChips()
        setListeners()
    }

    private fun initDatePicker() {
        val title = getString(R.string.consume_date)

        DatePicker(
            childFragmentManager,
            binding.consumeDateLayout,
            title,
            System.currentTimeMillis()
        ).apply {
            onEndIconClickListener = { useBottleViewModel.date = System.currentTimeMillis() }
            onDateChangedListener = { useBottleViewModel.date = it }
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
        binding.buttonSubmit.setOnClickListener {
            binding.friendsChipGroup.apply {
                val friends = checkedChipIds.map {
                    (findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Friend).id
                }

                useBottleViewModel.useBottle(args.bottleId, friends)
            }

            snackbarProvider.onShowSnackbarRequested(
                R.string.bottle_consumed,
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
