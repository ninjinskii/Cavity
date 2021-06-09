package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingsBinding
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.setupNavigation

class FragmentTastings : Fragment(R.layout.fragment_tastings) {
    private var _binding: FragmentTastingsBinding? = null
    private val binding get() = _binding!!
    private val tastingViewModel: TastingViewModel by viewModels()
    private val friendViewPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(R.layout.item_friend_chip, 8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTastingsBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val tastingAdapter = TastingRecyclerAdapter(friendViewPool)

        binding.tastingList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tastingAdapter
            setHasFixedSize(true)
        }

        tastingViewModel.futureTastings.observe(viewLifecycleOwner) {
            L.v("$it")
            tastingAdapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
