package com.louis.app.cavity.ui.addtasting

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.ui.addbottle.FragmentInquireGrapes
import com.louis.app.cavity.ui.addbottle.FragmentInquireReviews

class AddTastingPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> FragmentInquireTastingInfo()
        1 -> FragmentInquireGrapes()
        else -> FragmentInquireReviews()
    }
}

