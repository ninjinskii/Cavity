package com.louis.app.cavity.ui.manager

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.home.FragmentHome

class ManagerPagerAdapter(fragmentManager: FragmentManager) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        1 -> FragmentHome()
        2 -> FragmentHome()
        3 -> FragmentHome()
        else -> FragmentHome()
    }
}