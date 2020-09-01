package com.louis.app.cavity.ui.bottle

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AddBottlesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentInquireDatesAndGrape()
        1 -> FragmentInquireBuyingInfo()
        2 -> FragmentInquireExpertAdvice()
        else -> FragmentInquireOtherInfo()
    }
}