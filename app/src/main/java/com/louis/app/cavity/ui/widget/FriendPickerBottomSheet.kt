package com.louis.app.cavity.ui.widget

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetPickFriendBinding
import com.louis.app.cavity.ui.addbottle.adapter.PickFriendRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
import com.louis.app.cavity.util.dpToPx
import com.louis.app.cavity.util.prepareWindowInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FriendPickerBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_pick_friend) {
    private var config: FriendPickerView.FriendPickerConfig? = null
    private var _binding: BottomSheetPickFriendBinding? = null
    private val binding get() = _binding!!
    private val adapter = PickFriendRecyclerAdapter(handleMultipleChoices = true) {
        config?.onFriendSelectionChanged?.invoke(it)
    }

    private var scrollTo: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetPickFriendBinding.bind(view)

        binding.friendList.prepareWindowInsets { v, insets, _, _, _, bottom ->
            v.updatePadding(bottom = bottom)
            insets
        }

        setListeners()
        initRecyclerView()

        lifecycleScope.launch {
            delay(300)
            executeScrollToRequest()
        }
    }

    fun show(
        parentFragmentManager: FragmentManager,
        friendPickerConfig: FriendPickerView.FriendPickerConfig?
    ) {
        config = friendPickerConfig
        refreshPickableFriends(config)
        show(parentFragmentManager, "friend-picker-bottom-sheet")
    }

    fun refreshPickableFriends(config: FriendPickerView.FriendPickerConfig?) {
        config?.let {
            val pickable = it.friends.map { friend ->
                PickableFriend(friend, friend in it.selectedFriends)
            }
            adapter.submitList(pickable)
        }
    }

    fun requestScrollToPosition(position: Int) {
        scrollTo = position
    }

    private fun setListeners() {
        with(binding) {
            sortText.setOnClickListener { config?.onSortMethodChanged?.invoke() }
            search.doAfterTextChanged { config?.onFilterQueryChanged?.invoke(it.toString()) }
        }
    }


    private fun initRecyclerView() {
        with(binding.friendList) {
            this@with.adapter = this@FriendPickerBottomSheet.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun removeBottomSheetBottomInsetHandling(dialog: BottomSheetDialog) {
        dialog.setOnShowListener {
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.prepareWindowInsets { view, windowInsets, left, top, right, _ ->
                    view.updatePadding(left = left, right = right, top = top)
                    windowInsets
                }
        }
    }

    private fun configureBottomSheetBehavior(dialog: BottomSheetDialog) {
        with(dialog.behavior) {
            skipCollapsed = true
            isDraggable = true
            isHideable = true
            peekHeight = requireContext().dpToPx(500f).toInt()
        }
    }

    private fun executeScrollToRequest() {
        val scroller = LinearSmoothScroller(requireContext())
        scroller.targetPosition = scrollTo ?: 0

        try {
            binding.friendList.layoutManager?.startSmoothScroll(scroller)
        } finally {
            scrollTo = null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).also {
            removeBottomSheetBottomInsetHandling(it)
            configureBottomSheetBehavior(it)
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
