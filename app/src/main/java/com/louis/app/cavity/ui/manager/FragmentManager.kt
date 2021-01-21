package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManagerBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentManager : Fragment(R.layout.fragment_manager) {
    private var _binding: FragmentManagerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManagerBinding.bind(view)

        setupNavigation(binding.toolbar)

        setupWithViewPager()
    }

    private fun setupWithViewPager() {
        binding.viewPager.adapter = ManagerPagerAdapter(this@FragmentManager)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = getString(R.string.counties)
                1 -> tab.text = getString(R.string.grapes)
                2 -> tab.text = getString(R.string.reviews)
                3 -> tab.text = getString(R.string.friends)
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
