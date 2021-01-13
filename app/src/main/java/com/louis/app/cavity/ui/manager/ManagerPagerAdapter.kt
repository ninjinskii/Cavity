package com.louis.app.cavity.ui.manager

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.home.FragmentHome

class ManagerPagerAdapter(fragmentManager: FragmentManager) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentManageCounty()
        1 -> FragmentManageGrape()
        2 -> FragmentManageReview()
        else -> FragmentManageFriend()
    }
}