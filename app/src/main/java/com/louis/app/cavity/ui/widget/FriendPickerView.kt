package com.louis.app.cavity.ui.widget

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.findFragmentManager
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
    private var bottomSheet: FriendPickerBottomSheet? = null

    init {
        layoutTransition = LayoutTransition()
        loadSelectedFriendsChips()
        setOnClickListener { showPickFriendDialog() }
    }

    fun setConfig(config: FriendPickerConfig) {
        this.config = config
    }

    fun setFriends(friends: List<Friend>) {
        config?.friends = friends
        bottomSheet?.refreshPickableFriends(config)
    }

    fun setSelectedFriends(friends: List<Friend>) {
        config?.selectedFriends = friends.toSet()
        bottomSheet?.refreshPickableFriends(config)
        loadSelectedFriendsChips()
    }

    fun showPickFriendDialog() {
        /*val layoutInflater = LayoutInflater.from(context)
        var dialogBinding: BottomSheetPickFriendBinding? =
            BottomSheetPickFriendBinding.inflate(layoutInflater)

        dialogBinding?.run {
            friendList.adapter = adapter
            friendList.layoutManager = LinearLayoutManager(context)
            sortText.setOnClickListener { onSortMethodChanged?.invoke() }
            search.doAfterTextChanged { onFilterQueryChanged?.invoke(it.toString()) }
        }

        refreshPickableFriends()

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.gifted_by_friend)
            .setView(dialogBinding?.root)
            .setPositiveButton(R.string.submit) { _, _ -> }
            .setOnDismissListener {
                dialogBinding = null
                onFilterQueryChanged?.invoke("")
            }
            .show()*/

        val fragmentManager = findFragmentManager(this)

        bottomSheet = FriendPickerBottomSheet()
        fragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
                if (fragment == bottomSheet) {
                    bottomSheet = null
                    fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }
        }, false)

        ensureBottomSheet().show(fragmentManager, config)

//            with(config) {
//                it.show(
//                    parentFragmentManager = fragmentManager,
//                    friends = this?.friends ?: emptyList(),
//                    selectedFriends = this?.selectedFriends?.toList() ?: emptyList(),
//                    onFriendSelected = { pickable -> onFriendSelectionChanged?.invoke(pickable) },
//                    onFilterQueryChanged = { query -> onFilterQueryChanged?.invoke(query) },
//                    onSortMethodChanged = { onSortMethodChanged?.invoke() }
//                )
//            }
    }

    private fun ensureBottomSheet() =
        bottomSheet ?: FriendPickerBottomSheet().also { bottomSheet = it }

    private fun loadSelectedFriendsChips() {
        val lifecycle = findViewTreeLifecycleOwner()?.lifecycleScope
        val layoutInflater = LayoutInflater.from(context)

        if (config?.selectedFriends?.isEmpty() == true) {
            removeAllViews()
            generateEmptyStateButton()
            return
        }

        if (lifecycle != null) {
            ChipLoader.Builder()
                .with(lifecycle)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(config?.selectedFriends?.toList() ?: emptyList())
                .into(this)
                .selectable(false)
                .useAvatar(true)
                .doOnClick {
                    config?.onFriendChipClicked?.invoke(it.getTag(R.string.tag_chip_id) as Friend)
                }
                .closable { chipable ->
                    (chipable as? Friend)?.let {
                        config?.onFriendSelectionChanged?.invoke(PickableFriend(it, false))
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

    data class FriendPickerConfig(
        var friends: List<Friend>,
        var selectedFriends: Set<Friend>,
        val onFriendSelectionChanged: (PickableFriend) -> Unit,
        val onFriendChipClicked: (Friend) -> Unit,
        val onFilterQueryChanged: (String) -> Unit,
        val onSortMethodChanged: () -> Unit
    )
}
