package com.louis.app.cavity.ui.stepper

import androidx.viewpager2.adapter.FragmentStateAdapter

class StepperPagerAdapter(private val fragment: Stepper, private val steps: List<() -> Step>) :
    FragmentStateAdapter(fragment) {

    fun getFragmentAtPosition(position: Int): Step? {
        return fragment.childFragmentManager.findFragmentByTag("f$position") as Step?

        // Give a try with reflection
//            ?:    return this::class.superclasses.find { it == FragmentStateAdapter::class }
//                ?.java?.getDeclaredField("mFragments")
//                ?.let { field ->
//                    field.isAccessible = true
//                    val mFragments = field.get(this) as LongSparseArray<Step>
//                    return@let mFragments[getItemId(position)]
//                }
    }

    override fun getItemCount() = steps.size

    override fun createFragment(position: Int) = steps[position].invoke()
}
