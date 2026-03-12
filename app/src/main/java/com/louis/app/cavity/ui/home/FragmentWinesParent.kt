package com.louis.app.cavity.ui.home

import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView

interface FragmentWinesParent: SharedElementStore, ViewModelStoreOwner {
    fun getRecycledViewPool(): RecyclerView.RecycledViewPool
}
