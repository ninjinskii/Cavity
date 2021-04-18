package com.louis.app.cavity.ui.manager

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.manager.county.FragmentManageCounty
import com.louis.app.cavity.ui.manager.friend.FragmentManageFriend
import com.louis.app.cavity.ui.manager.grape.FragmentManageGrape
import com.louis.app.cavity.ui.manager.review.FragmentManageReview

class ManagerPagerAdapter(fragment: FragmentManager) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentManageCounty()
        1 -> FragmentManageGrape()
        2 -> FragmentManageReview()
        else -> FragmentManageFriend()
    }
}
