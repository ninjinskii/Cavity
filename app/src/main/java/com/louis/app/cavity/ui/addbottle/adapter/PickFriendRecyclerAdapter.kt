package com.louis.app.cavity.ui.addbottle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemPickFriendBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.util.setVisible

class PickFriendRecyclerAdapter(
    private val onSingleItemSelected: ((item: PickableFriend) -> Unit)?
) :
    ListAdapter<PickableFriend, PickFriendViewHolder>(PickFriendItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickFriendViewHolder {
        val binding =
            ItemPickFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PickFriendViewHolder(binding, onSingleItemSelected)
    }

    override fun onBindViewHolder(holder: PickFriendViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount() = currentList.size

    class PickFriendItemDiffCallback : DiffUtil.ItemCallback<PickableFriend>() {
        override fun areItemsTheSame(oldItem: PickableFriend, newItem: PickableFriend) =
            oldItem.friend.id == newItem.friend.id

        override fun areContentsTheSame(oldItem: PickableFriend, newItem: PickableFriend) =
            oldItem == newItem
    }
}

class PickFriendViewHolder(
    private val binding: ItemPickFriendBinding,
    private val onSingleItemSelected: ((item: PickableFriend) -> Unit)?
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(pickableFriend: PickableFriend) = with(binding) {
        Glide
            .with(itemView.context)
            .load(pickableFriend.friend.imgPath)
            .placeholder(R.drawable.ic_person)
            .centerCrop()
            .into(image)

        text.text = pickableFriend.friend.name
        checkbox.setVisible(pickableFriend.checked)

        root.setOnClickListener {
            pickableFriend.checked = !pickableFriend.checked
            checkbox.setVisible(pickableFriend.checked)
            onSingleItemSelected?.invoke(pickableFriend)
        }
    }
}

data class PickableFriend(val friend: Friend, var checked: Boolean)

