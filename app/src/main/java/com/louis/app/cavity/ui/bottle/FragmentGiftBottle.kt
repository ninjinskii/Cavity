package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentGiftBottleBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.collectAsSingle
import com.louis.app.cavity.util.prepareWindowInsets

class FragmentGiftBottle : Fragment(R.layout.fragment_gift_bottle) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var datePicker: DatePicker? = null
    private var _binding: FragmentGiftBottleBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val consumeGiftBottleViewModel: ConsumeGiftBottleViewModel by viewModels()
    private val args: FragmentGiftBottleArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setSharedAxisTransition(MaterialSharedAxis.Y, navigatingForward = false)
            setFadeThrough(navigatingForward = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGiftBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        applyInsets()
        initDatePicker()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        binding.root.prepareWindowInsets { view, windowInsets, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            windowInsets
        }

        binding.nestedScrollView.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initDatePicker() {
        val title = getString(R.string.consume_date)

        datePicker = DatePicker(
            childFragmentManager,
            binding.giftDateLayout,
            title,
            clearable = true,
            System.currentTimeMillis()
        ).apply {
            onEndIconClickListener =
                { consumeGiftBottleViewModel.date = System.currentTimeMillis() }
            onDateChangedListener = { consumeGiftBottleViewModel.date = it }
        }
    }

    private fun observe() {
        consumeGiftBottleViewModel.getAllFriends().observe(viewLifecycleOwner) {
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(it)
                .into(binding.friendsChipGroup)
                .useAvatar(true)
                .emptyText(getString(R.string.placeholder_friend))
                .build()
                .go()
        }
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            if (!binding.giftDateLayout.validate()) {
                return@setOnClickListener
            }

            binding.friendsChipGroup.apply {
                val friend = collectAsSingle<Friend>()

                if (friend == null) {
                    snackbarProvider.onShowSnackbarRequested(R.string.no_friend)
                    return@setOnClickListener
                }

                consumeGiftBottleViewModel.consumeBottle(
                    args.bottleId,
                    "",
                    listOf(friend),
                    consumeGiftBottleViewModel.date,
                    isAGift = true
                )
            }

            findNavController().navigateUp()
            snackbarProvider.onShowSnackbarRequested(R.string.bottle_gifted)
        }

        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonAddFriend.setOnClickListener {
            showAddFriendDialog()
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

    override fun onDestroy() {
        super.onDestroy()
        datePicker?.dispose()
        _binding = null
    }
}
