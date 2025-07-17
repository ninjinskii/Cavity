package com.louis.app.cavity.ui.widget.friendpicker

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.louis.app.cavity.R
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend

class FriendPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ChipGroup(context, attrs, defStyleAttr) {

    private var config: FriendPickerConfig? = null
    private var friends: List<Friend> = emptyList()
    private var selectedFriends: Set<Friend> = emptySet()

    init {
        layoutTransition = LayoutTransition().also { it.setAnimateParentHierarchy(false) }
        loadSelectedFriendsChips()
        setOnClickListener { config?.onRootViewClick?.invoke() }
    }

    fun setConfig(config: FriendPickerConfig) {
        this.config = config
    }

    fun setFriends(friends: List<Friend>) {
        this.friends = friends
    }

    fun setSelectedFriends(friends: List<Friend>) {
        this.selectedFriends = friends.toSet()
        loadSelectedFriendsChips()
    }

    private fun loadSelectedFriendsChips() {
        val lifecycle = findViewTreeLifecycleOwner()?.lifecycleScope
        val layoutInflater = LayoutInflater.from(context)

        if (this.selectedFriends.isEmpty()) {
            removeAllViews()
            generateEmptyStateButton()
            return
        }

        if (lifecycle != null) {
            ChipLoader.Builder()
                .with(lifecycle)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(this.selectedFriends.toList())
                .into(this)
                .selectable(false)
                .useAvatar(true)
                .doOnClick {
                    config?.onFriendChipClicked?.invoke(it.getTag(R.string.tag_chip_id) as Friend)
                }
                .closable { chipable ->
                    (chipable as? Friend)?.let {
                        config?.onFriendCloseIconClicked?.invoke(PickableFriend(it, false))
                    }
                }
                .build()
                .go()
        }
    }

    private fun generateEmptyStateButton() {
        this.addView(
            MaterialButton(
                ContextThemeWrapper(
                    context,
                    R.style.Widget_Cavity_Button_OutlinedButton
                )
            ).apply {
                text = context.getString(R.string.click_to_select_friend)
                backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                foregroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
                setTextColor(ContextCompat.getColor(context, R.color.cavity_gold))
                setOnClickListener { config?.onRootViewClick?.invoke() }
            }
        )
    }

    data class FriendPickerConfig(
        val onRootViewClick: () -> Unit,
        val onFriendCloseIconClicked: (PickableFriend) -> Unit,
        val onFriendChipClicked: (Friend) -> Unit
    )
}
