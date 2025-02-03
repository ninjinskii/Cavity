package com.louis.app.cavity.ui.stats

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.R

class StatsPagerAdapter(fragment: Fragment, lifecyclOwner: LifecycleOwner) :
    FragmentStateAdapter(fragment.childFragmentManager, lifecyclOwner.lifecycle) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentPie.newInstance(position, R.string.pie_title_county)
        1 -> FragmentPie.newInstance(position, R.string.pie_title_color)
        2 -> FragmentPie.newInstance(position, R.string.pie_title_vintage)
        else -> FragmentPie.newInstance(position, R.string.pie_title_naming)
    }
}
