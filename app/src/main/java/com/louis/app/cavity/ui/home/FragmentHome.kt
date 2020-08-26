package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding
import com.louis.app.cavity.util.setVisible
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentHome : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var condensedMode = false

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
                tab.setUpWithViewPager(viewPager)
            }
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.show_addWine)
        }
    }

    private fun observe() {
        homeViewModel.shouldShowFab.observe(viewLifecycleOwner) {
            if (it) binding.fab.run { if (!isShown) show() }
            else binding.fab.run { if (isShown) hide() }
        }
    }

    private fun setCondensedMode() {
        with(binding) {
            lifecycleScope.launch(Main) {
                tab.elevation = 0F

                tab.animate()
                    .scaleX(-100F)
                    .alpha(0F)
                    .setDuration(500)
                    .start()

                delay(500)
                tab.setVisible(false)
            }
        }
    }

    private fun setDefaultMode() {
        with(binding) {
            lifecycleScope.launch(Main) {
                tab.setVisible(true)
                tab.animate()
                    .scaleX(100F)
                    .alpha(1F)
                    .setDuration(500)
                    .start()

                delay(500)
                tab.elevation = 50F
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.switchView -> {
                condensedMode = !condensedMode
                if (condensedMode) setCondensedMode() else setDefaultMode()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
