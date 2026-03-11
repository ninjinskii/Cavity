package com.louis.app.cavity.ui.home

import androidx.recyclerview.widget.RecyclerView

interface FragmentWinesParent: SharedElementStore {
    fun getRecycledViewPool(): RecyclerView.RecycledViewPool
}
