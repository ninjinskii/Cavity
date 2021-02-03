package com.louis.app.cavity.ui.home

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.model.County

class WinesPagerAdapter(activity: FragmentActivity, private val counties: List<County>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = counties.size

    override fun createFragment(position: Int) =
        FragmentWines.newInstance(counties[position].id)

    override fun getItemId(position: Int) = counties[position].id

    override fun containsItem(itemId: Long) = counties.map { it.id }.contains(itemId)
}
