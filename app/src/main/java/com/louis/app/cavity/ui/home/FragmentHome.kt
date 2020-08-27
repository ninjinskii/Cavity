package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        initToolbar()
        setupScrollableTab()
        setListeners()
        observe()
    }

    private fun initToolbar() {
        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(activity.findViewById(R.id.toolbar))
        setHasOptionsMenu(true)
    }

    private fun setupScrollableTab() {
        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            with(binding) {
                tab.addTabs(it.map { county -> county.name })
                activity?.let { activity ->
                    viewPager.adapter = WinesPagerAdapter(activity.supportFragmentManager, it)
                }
                viewPager.offscreenPageLimit = 0
                viewPager.setCurrentItem(0, true)
                tab.smoothScrollToPosition(0)
                tab.setUpWithViewPager(viewPager)
            }
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.homeToAddWine)
        }
    }

    private fun observe() {
        homeViewModel.shouldShowFab.observe(viewLifecycleOwner) {
            if (it) binding.fab.run { if (!isShown) show() }
            else binding.fab.run { if (isShown) hide() }
        }
    }
}
