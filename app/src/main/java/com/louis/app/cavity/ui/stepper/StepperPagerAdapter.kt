package com.louis.app.cavity.ui.stepper

import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class StepperPagerAdapter(fragment: Stepper, private val steps: Set<KClass<out Step>>) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount() = steps.size

    override fun createFragment(position: Int): Step = steps.elementAt(position).createInstance()

}
