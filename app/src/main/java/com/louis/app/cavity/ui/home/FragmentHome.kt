package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHomeBinding

class FragmentHome : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupScrollableTab()
        setListeners()
        observe()
        setHasOptionsMenu(true)

        return binding.root
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.switchView -> TODO("Change item view type in RecyclerView")
        }

        return super.onOptionsItemSelected(item)
    }
}
