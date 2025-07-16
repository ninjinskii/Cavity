package com.louis.app.cavity.ui.widget.friendpicker

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetPickFriendBinding
import com.louis.app.cavity.ui.addbottle.adapter.PickFriendRecyclerAdapter
import com.louis.app.cavity.util.dpToPx
import com.louis.app.cavity.util.prepareWindowInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FriendPickerBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_pick_friend) {
    private var _binding: BottomSheetPickFriendBinding? = null
    private val binding get() = _binding!!
    private val friendPickerViewModel: FriendPickerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val adapter = PickFriendRecyclerAdapter {
        friendPickerViewModel.updateFriendStatus(it)
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
        observe()
        initRecyclerView()

        lifecycleScope.launch {
            delay(300)
            executeScrollToRequest()
        }
    }

    fun requestScrollToPosition(position: Int) {
        scrollTo = position
    }

    private fun observe() {
        friendPickerViewModel.a().observe(viewLifecycleOwner) {
            adapter.submitList(it) {
                (dialog as BottomSheetDialog).behavior.peekHeight =
                    requireContext().dpToPx(500f).toInt()
            }
        }
    }

    private fun setListeners() {
        with(binding) {
            sortText.setOnClickListener { friendPickerViewModel.toggleSortFriendsByPreference() }
            search.doAfterTextChanged { friendPickerViewModel.setFriendFilterQuery(it.toString()) }
        }
    }


    private fun initRecyclerView() {
        with(binding.friendList) {
            this@with.adapter = this@FriendPickerBottomSheet.adapter
            val cols = context.resources.getInteger(R.integer.friend_grid_cols)
            layoutManager = GridLayoutManager(context, cols)
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
            peekHeight = 0
        }
    }

    private fun executeScrollToRequest() {
        val scroller = LinearSmoothScroller(requireContext())
        scroller.targetPosition = scrollTo ?: return

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
