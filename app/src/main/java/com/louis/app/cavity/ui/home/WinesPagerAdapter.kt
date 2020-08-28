package com.louis.app.cavity.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.model.County

class WinesPagerAdapter(fragment: Fragment, private val counties: List<County>) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = counties.size

    override fun createFragment(position: Int) =
        FragmentWines.newInstance(counties[position].idCounty)

    override fun getItemId(position: Int) = counties[position].idCounty

    override fun containsItem(itemId: Long) = counties.map { it.idCounty }.contains(itemId)
}
