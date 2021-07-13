package com.louis.app.cavity.ui.stepper

import androidx.viewpager2.adapter.FragmentStateAdapter

class StepperPagerAdapter(fragment: Stepper, private val steps: Set<Step>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount() = steps.size

    override fun createFragment(position: Int) = steps.elementAt(position)
}
