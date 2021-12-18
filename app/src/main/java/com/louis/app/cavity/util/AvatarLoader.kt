package com.louis.app.cavity.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.louis.app.cavity.R

object AvatarLoader {
    fun requestAvatar(context: Context, uri: String, onLoaded: (Drawable?) -> Unit) {
        Glide
            .with(context)
            .load(Uri.parse(uri))
            .centerCrop()
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ) = true

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onLoaded(resource)
                    return true
                }
            }).submit(
                context.resources.getDimensionPixelSize(R.dimen.small_icon),
                context.resources.getDimensionPixelSize(R.dimen.small_icon)
            )
    }
}
