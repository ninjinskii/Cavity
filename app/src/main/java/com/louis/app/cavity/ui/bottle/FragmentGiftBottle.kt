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
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider

class FragmentGiftBottle : Fragment(R.layout.fragment_gift_bottle) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentGiftBottleBinding? = null
    private val binding get() = _binding!!
    private val consumeGiftBottleViewModel: ConsumeGiftBottleViewModel by viewModels()
    private val args: FragmentGiftBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGiftBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        initDatePicker()
        observe()
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
            onEndIconClickListener =
                { consumeGiftBottleViewModel.date = System.currentTimeMillis() }
            onDateChangedListener = { consumeGiftBottleViewModel.date = it }
        }
    }

    private fun observe() {
        val allFriends = mutableSetOf<Friend>()
        val alreadyInflated = mutableSetOf<Friend>()

        consumeGiftBottleViewModel.getAllFriends().observe(viewLifecycleOwner) {
            allFriends.addAll(it)
            val toInflate = allFriends - alreadyInflated
            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(toInflate.toList())
                .into(binding.friendsChipGroup)
                .build()
                .go()
        }

        consumeGiftBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes, useAnchorView = false)
            }
        }
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            if (!binding.giftDateLayout.validate()) {
                return@setOnClickListener
            }

            binding.friendsChipGroup.apply {
                val friend =
                    (findViewById<Chip>(checkedChipId).getTag(R.string.tag_chip_id) as Friend).id

                consumeGiftBottleViewModel.giftBottle(args.bottleId, "", friend)
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
            consumeGiftBottleViewModel.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
