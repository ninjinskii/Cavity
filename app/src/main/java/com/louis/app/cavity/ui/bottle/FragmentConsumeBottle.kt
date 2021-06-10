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
import com.louis.app.cavity.databinding.FragmentConsumeBottleBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider

class FragmentConsumeBottle : Fragment(R.layout.fragment_consume_bottle) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentConsumeBottleBinding? = null
    private val binding get() = _binding!!
    private val consumeGiftBottleViewModel: ConsumeGiftBottleViewModel by viewModels()
    private val args: FragmentConsumeBottleArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConsumeBottleBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        initDatePicker()
        observe()
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
                .toInflate(R.layout.chip_friend_entry)
                .load(toInflate.toList())
                .into(binding.friendsChipGroup)
                .useAvatar(true)
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
            if (!binding.consumeDateLayout.validate()) {
                return@setOnClickListener
            }

            binding.friendsChipGroup.apply {
                val comment = binding.tasteComment.text.toString()
                val friends = checkedChipIds.map {
                    (findViewById<Chip>(it).getTag(R.string.tag_chip_id) as Friend).id
                }

                consumeGiftBottleViewModel.consumeBottle(args.bottleId, comment, friends)
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
