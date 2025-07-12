package com.louis.app.cavity.ui.widget

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetPickFriendBinding
import com.louis.app.cavity.ui.addbottle.adapter.PickFriendRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.util.prepareWindowInsets

class FriendPickerBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_pick_friend) {
    private var _binding: BottomSheetPickFriendBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetPickFriendBinding.bind(view)

        binding.friendList.prepareWindowInsets { v, _, _, _, _, bottom ->
            v.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val adapter = PickFriendRecyclerAdapter(handleMultipleChoices = true, null)

        with(binding.friendList) {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }

        /*addBottleViewModel.otherInfoManager.pickableFriends.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
