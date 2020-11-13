package com.louis.app.cavity.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.model.County

class WinesPagerAdapter(activity: FragmentActivity, private val counties: List<County>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = counties.size

    override fun createFragment(position: Int) =
        FragmentWines.newInstance(counties[position].countyId)

    override fun getItemId(position: Int) = counties[position].countyId

    override fun containsItem(itemId: Long) = counties.map { it.countyId }.contains(itemId)
}
