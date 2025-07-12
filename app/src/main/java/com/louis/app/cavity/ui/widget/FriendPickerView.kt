package com.louis.app.cavity.ui.widget

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.BottomSheetPickFriendBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.addbottle.adapter.PickFriendRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.adapter.PickableFriend

class FriendPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    private var friends: List<PickableFriend> = emptyList()
    private var selectedFriends: List<Friend> = emptyList()
    private var onFriendSelectionChanged: ((PickableFriend) -> Unit)? = null
    private var onChipFriendClicked: ((Friend) -> Unit)? = null
    private var onFilterQueryChanged: ((String) -> Unit)? = null
    private var onSortMethodChanged: (() -> Unit)? = null

    private val adapter = PickFriendRecyclerAdapter(handleMultipleChoices = true) {
        onFriendSelectionChanged?.invoke(it)
    }

    init {
        layoutTransition = LayoutTransition()
        loadSelectedFriendsChips()
        setOnClickListener { showPickFriendDialog() }
    }

    fun setFriends(friends: List<Friend>) {
        computeSelectedFriends(friends)
    }

    fun setSelectedFriends(friends: List<Friend>) {
        this.selectedFriends = friends
        computeSelectedFriends(this.friends.map { it.friend })
        loadSelectedFriendsChips()
    }

    fun setOnFriendSelectedListener(listener: (PickableFriend) -> Unit) {
        onFriendSelectionChanged = listener
    }

    fun setOnFriendClickListener(listener: ((Friend) -> Unit)) {
        onChipFriendClicked = listener
    }

    fun setOnFilterQueryChangedListener(listener: ((String) -> Unit)) {
        onFilterQueryChanged = listener
    }

    fun setOnSortMethodChangedListener(listener: (() -> Unit)) {
        onSortMethodChanged = listener
    }

    fun showPickFriendDialog() {
        val layoutInflater = LayoutInflater.from(context)
        var dialogBinding: BottomSheetPickFriendBinding? =
            BottomSheetPickFriendBinding.inflate(layoutInflater)

        dialogBinding?.run {
            friendList.adapter = adapter
            friendList.layoutManager = LinearLayoutManager(context)
            sortText.setOnClickListener { onSortMethodChanged?.invoke() }
            search.doAfterTextChanged { onFilterQueryChanged?.invoke(it.toString()) }
        }

        setFriends(friends.map { it.friend })
//        adapter.submitList(friends)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.gifted_by_friend)
            .setView(dialogBinding?.root)
            .setPositiveButton(R.string.submit) { _, _ -> }
            .setOnDismissListener {
                dialogBinding = null
                onFilterQueryChanged?.invoke("")
            }
            .show()
    }

    private fun computeSelectedFriends(friends: List<Friend>) {
        this.friends = friends.map { PickableFriend(it, it in selectedFriends) }
        adapter.submitList(this.friends)
    }

    private fun loadSelectedFriendsChips() {
        val lifecycle = findViewTreeLifecycleOwner()?.lifecycleScope
        val layoutInflater = LayoutInflater.from(context)

        if (selectedFriends.isEmpty()) {
            removeAllViews()
            generateEmptyStateButton()
            return
        }

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
                    (chipable as? Friend)?.let {
                        onFriendSelectionChanged?.invoke(PickableFriend(it, false))
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
                setOnClickListener { showPickFriendDialog() }
            }
        )
    }
}
