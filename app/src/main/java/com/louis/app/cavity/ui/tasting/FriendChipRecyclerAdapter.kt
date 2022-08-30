package com.louis.app.cavity.ui.tasting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ChipFriendBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.util.AvatarLoader

class FriendChipRecyclerAdapter : ListAdapter<Friend, FriendChipRecyclerAdapter.FriendViewHolder>
    (FriendItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding =
            ChipFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = R.layout.chip_friend

    class FriendItemDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend) = oldItem == newItem
    }

    inner class FriendViewHolder(private val binding: ChipFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.root.text = friend.name

            AvatarLoader.requestAvatar(itemView.context, friend.imgPath) {
                binding.root.chipIconTint = null
                binding.root.chipIcon = it
            }
        }
    }
}
