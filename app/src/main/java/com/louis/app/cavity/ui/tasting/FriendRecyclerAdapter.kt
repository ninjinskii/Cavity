package com.louis.app.cavity.ui.tasting

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.ItemFriendChipBinding
import com.louis.app.cavity.model.Friend

class FriendRecyclerAdapter : ListAdapter<Friend, FriendRecyclerAdapter.FriendViewHolder>
    (FriendItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding =
            ItemFriendChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int) = R.layout.item_friend_chip

    class FriendItemDiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend) = oldItem == newItem
    }

    inner class FriendViewHolder(private val binding: ItemFriendChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.root.text = friend.name

            Glide
                .with(itemView.context)
                .load(Uri.parse(friend.imgPath))
                .centerCrop()
                .circleCrop()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean = true

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.root.chipIcon = resource
                        return true
                    }
                }).submit(
                    itemView.resources.getDimensionPixelSize(R.dimen.small_icon),
                    itemView.resources.getDimensionPixelSize(R.dimen.small_icon)
                )
        }
    }
}
