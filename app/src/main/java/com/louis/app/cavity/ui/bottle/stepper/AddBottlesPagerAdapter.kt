package com.louis.app.cavity.ui.bottle.stepper

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.bottle.steps.FragmentInquireBuyingInfo
import com.louis.app.cavity.ui.bottle.steps.FragmentInquireDatesAndGrape
import com.louis.app.cavity.ui.bottle.steps.FragmentInquireExpertAdvice
import com.louis.app.cavity.ui.bottle.steps.FragmentInquireOtherInfo

class AddBottlesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentInquireDatesAndGrape()
        1 -> FragmentInquireBuyingInfo()
        2 -> FragmentInquireExpertAdvice()
        else -> FragmentInquireOtherInfo()
    }
}