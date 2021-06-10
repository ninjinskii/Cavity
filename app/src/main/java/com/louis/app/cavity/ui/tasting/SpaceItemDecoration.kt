package com.louis.app.cavity.ui.tasting

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(@Px private val space: Int) : RecyclerView.ItemDecoration() {
    private fun isDecorated(child: View, parent: RecyclerView): Boolean {
        return parent.getChildAdapterPosition(child) != 0
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (isDecorated(view, parent)) outRect.left = space
    }
}
