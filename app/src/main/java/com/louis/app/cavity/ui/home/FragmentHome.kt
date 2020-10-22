package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.ui.ActivityMain
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupParentToolbar()
        setupScrollableTab()
        setListeners()
        observe()
    }

    private fun setupParentToolbar() {
        val activity = activity as ActivityMain
        val navigationView = activity.findViewById<NavigationView>(R.id.navView)

        activity.setSupportActionBar(binding.appBarDefault.toolbar)
        val appBarConfiguration = activity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
        NavigationUI.setupWithNavController(navigationView, findNavController())
    }

    private fun setupScrollableTab() {
        binding.tab.addOnLongClickListener {
            // TODO: show dialog info for county
        }

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            lifecycleScope.launch(Main) {
                with(binding) {
                    tab.addTabs(it)
                    viewPager.adapter = WinesPagerAdapter(this@FragmentHome, it)
                    viewPager.offscreenPageLimit = 5
                    tab.setUpWithViewPager(viewPager)
                }
            }
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.homeToAddWine)
        }
    }

    private fun observe() {
        homeViewModel.isScrollingToTop.observe(viewLifecycleOwner) {
            with(binding) {
                if (it) fab.run { if (!isShown) show() }
                else fab.run { if (isShown) hide() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
