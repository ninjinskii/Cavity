package com.louis.app.cavity.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentHistoryBinding
import com.louis.app.cavity.util.setupNavigation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FragmentHistory: Fragment(R.layout.fragment_history) {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupNavigation(binding.toolbar)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val historyAdapter = HistoryRecyclerAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historyAdapter
            setHasFixedSize(true)
        }

        historyViewModel.entries.observe(viewLifecycleOwner) {
            historyAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
