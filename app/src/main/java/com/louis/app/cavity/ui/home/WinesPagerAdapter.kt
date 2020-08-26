package com.louis.app.cavity.ui.home

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.louis.app.cavity.model.County

class WinesPagerAdapter(fragmentManager: FragmentManager, private val counties: List<County>) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = FragmentWines(counties[position])

    override fun getCount() = counties.size
}