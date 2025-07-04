package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogChipablePickBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.addbottle.adapter.PickFriendRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend
import com.louis.app.cavity.util.L

class FriendPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    private var friends: List<PickableFriend> = emptyList()
    private var selectedFriends: List<Friend> = emptyList()
    private var onFriendsSelected: ((List<Friend>) -> Unit)? = null

    init {
        loadSelectedFriendsChips()
        setOnClickListener {
            showPickFriendDialog()
        }
    }

    fun setFriends(friends: List<Friend>) {
        this.friends = friends.map { PickableFriend(it, false) }
        // invalidate()
    }

    fun setSelectedFriends(friends: List<Friend>) {
        this.selectedFriends = friends
        // invalidate()
        loadSelectedFriendsChips()
    }

    fun setOnFriendSelectedListener(listener: (List<Friend>) -> Unit) {
        onFriendsSelected = listener
    }

    fun showPickFriendDialog() {
        val layoutInflater = LayoutInflater.from(context)
        val dialogBinding = DialogChipablePickBinding.inflate(layoutInflater)
        val adapter = PickFriendRecyclerAdapter(handleMultipleChoices = true) {
            //onFriendSelected?.invoke(it)
        }

        with(dialogBinding.friendList) {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)

            adapter.submitList(friends)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.gifted_by_friend)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.submit) { _, _ ->
                selectedFriends = adapter.currentList.filter { it.checked }.map { it.friend }
                onFriendsSelected?.invoke(selectedFriends)
                L.v("${adapter.currentList}")
                loadSelectedFriendsChips()
            }
            .show()
    }

    private fun loadSelectedFriendsChips() {
        val lifecycle = findViewTreeLifecycleOwner()?.lifecycleScope
        val layoutInflater = LayoutInflater.from(context)

        if (lifecycle != null) {
            ChipLoader.Builder()
                .with(lifecycle)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(friends.filter { it.checked }.map { it.friend })
                .into(this)
                .selectable(false)
                .useAvatar(true)
                .emptyText(context.getString(R.string.placeholder_friend))
                .build()
                .go()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onFriendsSelected = null
    }
}