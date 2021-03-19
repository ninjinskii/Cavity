package com.louis.app.cavity.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.louis.app.cavity.model.County

class WinesPagerAdapter(
    fragment: Fragment,
    private val counties: List<County>
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = counties.size

    override fun createFragment(position: Int) =
        FragmentWines.newInstance(counties[position].id)

    override fun getItemId(position: Int) = counties[position].id

    override fun containsItem(itemId: Long) = counties.map { it.id }.contains(itemId)
}
