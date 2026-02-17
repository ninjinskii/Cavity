package com.louis.app.cavity.ui.widget.friendpicker

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
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

/**
 * Tips for instantiating this view model. If you want to scope the friend picker to the fragment
 * where is it called from, pass `childFragmentManager` as the first `show()` arguments.
 * When instantiating friend picker viewModel, scope it to the same fragment where the view model
 * lives.
 * When scoping to a parent fragment, you can use `parentFragmentManager` & scope the view model to
 * `{requireParentFragment()}`
 */
class FriendPickerBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_pick_friend) {
    private var _binding: BottomSheetPickFriendBinding? = null
    private val binding get() = _binding!!
    private val friendPickerViewModel: FriendPickerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private lateinit var adapter: PickFriendRecyclerAdapter

    private var scrollTo: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = PickFriendRecyclerAdapter (requireContext()) {
            friendPickerViewModel.updateFriendStatus(it)
        }
    }

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
        initFriendTextSwitcher()

        lifecycleScope.launch {
            delay(300)
            executeScrollToRequest()
        }
    }

    fun requestScrollToPosition(position: Int) {
        scrollTo = position
    }

    private fun observe() {
        friendPickerViewModel.getPickableFriends().observe(viewLifecycleOwner) {
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

    private fun initFriendTextSwitcher() {
        var index = if (friendPickerViewModel.getSortByPreference()) 0 else 1
        val friendSortMethodText: List<Int> =
            listOf(R.string.sort_friend_frequence, R.string.sort_friend_alphabetical)

        binding.sortText.apply {
            setCurrentText(getString(friendSortMethodText[index]))

            inAnimation =
                AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_in_left)

            outAnimation =
                AnimationUtils.loadAnimation(requireContext(), android.R.anim.slide_out_right)

            setOnClickListener {
                index = (index + 1) % friendSortMethodText.size
                setText(getString(friendSortMethodText[index]))
                friendPickerViewModel.toggleSortFriendsByPreference()
            }
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
