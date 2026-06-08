package com.louis.app.cavity.ui.stats

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.R
import com.louis.app.cavity.domain.stats.StatGroupBy

class StatsPagerAdapter(fragment: Fragment, lifecycleOwner: LifecycleOwner) :
    FragmentStateAdapter(fragment.childFragmentManager, lifecycleOwner.lifecycle) {

    private val pages = listOf(
        StatGroupBy.COUNTY to R.string.pie_title_county,
        StatGroupBy.COLOR to R.string.pie_title_color,
        StatGroupBy.VINTAGE to R.string.pie_title_vintage,
        StatGroupBy.NAMING to R.string.pie_title_naming
    )

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        val (statSlot, title) = pages[position]
        return FragmentPie.newInstance(statSlot, title)
    }

    fun getSlotAt(position: Int) = pages[position].first
}
