package com.louis.app.cavity.ui.stats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentPie.newInstance(StatGlobalType.COUNTY, position)
        1 -> FragmentPie.newInstance(StatGlobalType.COLOR, position)
        2 -> FragmentPie.newInstance(StatGlobalType.VINTAGE, position)
        else -> FragmentPie.newInstance(StatGlobalType.NAMING, position)
    }
}
