package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManagerBinding
import com.louis.app.cavity.util.setupNavigation

class FragmentManager: Fragment(R.layout.fragment_manager) {
    private var _binding: FragmentManagerBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManagerBinding.bind(view)

        setupNavigation(binding.toolbar)

        setupWithViewPager()
    }

    private fun setupWithViewPager() {
        binding.viewPager.apply {
            adapter = ManagerPagerAdapter(this@FragmentManager)
            isUserInputEnabled = false
        }

        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}