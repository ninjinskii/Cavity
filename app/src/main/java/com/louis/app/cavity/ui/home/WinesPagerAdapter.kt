package com.louis.app.cavity.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WinesPagerAdapter(fragmentManager: FragmentManager, private val countyCount: Int) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return FragmentWines()
    }

    override fun getCount(): Int {
        return countyCount
    }
}