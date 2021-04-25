package com.louis.app.cavity.ui.stats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class StatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4

    override fun createFragment(position: Int) = when (position) {
        0 -> FragmentPie.newInstance(StatGlobalType.COLOR)
        1 -> FragmentPie.newInstance(StatGlobalType.COLOR)
        2 -> FragmentPie.newInstance(StatGlobalType.COLOR)
        else -> FragmentPie.newInstance(StatGlobalType.COLOR)
    }
}
