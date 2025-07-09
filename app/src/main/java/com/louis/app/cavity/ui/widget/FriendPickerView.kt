package com.louis.app.cavity.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend

class FriendPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    private var friends: List<PickableFriend> = emptyList()
    private var selectedFriends: List<Friend> = emptyList()
    private var onFriendsSelected: ((List<Friend>) -> Unit)? = null
    private var onChipFriendClicked: ((Friend) -> Unit)? = null
    private var onRequestShowDialog: (() -> Unit)? = null

    init {
        loadSelectedFriendsChips()
    }

    fun setFriends(friends: List<Friend>) {
        this.friends = friends.map { PickableFriend(it, it in selectedFriends) }
    }

    fun setSelectedFriends(friends: List<Friend>) {
        this.selectedFriends = friends
        this.friends.forEach { it.checked = it.friend in friends }
        loadSelectedFriendsChips()
    }

    fun setOnFriendSelectedListener(listener: (List<Friend>) -> Unit) {
        onFriendsSelected = listener
    }

    fun setOnFriendClickListener(listener: ((Friend) -> Unit)) {
        onChipFriendClicked = listener
    }

    private fun loadSelectedFriendsChips() {
        val lifecycle = findViewTreeLifecycleOwner()?.lifecycleScope
        val layoutInflater = LayoutInflater.from(context)

        if (lifecycle != null) {
            ChipLoader.Builder()
                .with(lifecycle)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(selectedFriends)
                .into(this)
                .selectable(false)
                .useAvatar(true)
                .doOnClick {
                    onChipFriendClicked?.invoke(it.getTag(R.string.tag_chip_id) as Friend)
                }
                .closable { chipable ->
                    setSelectedFriends(
                        selectedFriends.filter { friend -> friend.id != chipable.getItemId() }
                    )
                    onFriendsSelected?.invoke(selectedFriends)
                }
                .emptyText(context.getString(R.string.placeholder_friend))
                .build()
                .go()
        }
    }
}
