package com.louis.app.cavity.ui.search

import android.animation.AnimatorInflater
import android.graphics.Point
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentSearchBinding
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.ui.home.WineRecyclerAdapter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FragmentSearch : Fragment(R.layout.fragment_search), CountyLoader {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var bottlesAdapter: WineRecyclerAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var menu: Menu
    private val searchViewModel: SearchViewModel by activityViewModels()
    private var isHeaderShadowShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = false
        }

        setHasOptionsMenu(true)

        initRecyclerView()
        inflateChips()
        setListeners()
        setBottomSheetPeekHeight()
    }

    private fun initRecyclerView() {
        val colors = context?.let {
            listOf(
                it.getColor(R.color.wine_white),
                it.getColor(R.color.wine_red),
                it.getColor(R.color.wine_sweet),
                it.getColor(R.color.wine_rose),
                it.getColor(R.color.colorAccent)
            )
        }

        bottlesAdapter = WineRecyclerAdapter({}, {}, colors ?: emptyList())

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = bottlesAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    // Add shadow when RV is not on top
                    if (!recyclerView.canScrollVertically(-1))
                        setHeaderShadow(false)
                    else
                        setHeaderShadow(true)
                }
            })
        }

        searchViewModel.getWineWithBottles().observe(viewLifecycleOwner) {
            bottlesAdapter.submitList(it)
        }
    }

    private fun inflateChips() {
        lifecycleScope.launch(IO) {
            val counties = searchViewModel.getAllCountiesNotLive().toSet()
            loadCounties(
                lifecycleScope,
                layoutInflater,
                binding.countyChipGroup,
                counties,
                selectionRequired = false
            )
        }
    }

    private fun setListeners() {
    }

    // Needed for split screen
    private fun setBottomSheetPeekHeight() {
        lifecycleScope.launch(Main) {
            delay(300)

            val display = activity?.windowManager?.defaultDisplay
            val size = Point()
            display?.getSize(size)
            val screenHeight = size.y
            val location = IntArray(2)

            // Bottom-most view in back layer
            binding.toggleShowBefore.getLocationOnScreen(location)
            bottomSheetBehavior.setPeekHeight(
                screenHeight - location[1] - binding.toggleShowBefore.height,
                true
            )
        }
    }

    private fun setHeaderShadow(setVisible: Boolean) {
        val header = binding.backdropHeader

        if (setVisible && !isHeaderShadowShown) {
            header.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.show_elevation)
            isHeaderShadowShown = true
        } else if (!setVisible && isHeaderShadowShown) {
            header.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.hide_elevation)
            isHeaderShadowShown = false
        }
    }

    private fun toggleBackdrop() {
        val item = menu.getItem(1)

        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            item.setIcon(R.drawable.anim_close_filter)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            menu.getItem(1).setIcon(R.drawable.anim_filter_close)
        }

        (item.icon as AnimatedVectorDrawable).start()
    }

    override fun onResume() {
        (activity as ActivityMain).setToolbarShadow(false)
        super.onResume()
    }

    override fun onPause() {
        (activity as ActivityMain).setToolbarShadow(true)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.toggleBackdrop -> {
                toggleBackdrop()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
