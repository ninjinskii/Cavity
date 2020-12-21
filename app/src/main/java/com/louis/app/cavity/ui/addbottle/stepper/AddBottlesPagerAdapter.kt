package com.louis.app.cavity.ui.addbottle.stepper

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.addbottle.FragmentInquireDates
import com.louis.app.cavity.ui.addbottle.FragmentInquireGrapes
import com.louis.app.cavity.ui.addbottle.FragmentInquireOtherInfo
import com.louis.app.cavity.ui.addbottle.FragmentInquireReviews

class AddBottlesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> FragmentInquireDates()
        1 -> FragmentInquireGrapes()
        2 -> FragmentInquireReviews()
        else -> FragmentInquireOtherInfo()
    }
}
