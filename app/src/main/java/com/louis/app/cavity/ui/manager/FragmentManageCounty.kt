package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageCountyBinding
import com.louis.app.cavity.ui.manager.recycler.CountyItemTouchHelperCallback
import com.louis.app.cavity.ui.manager.recycler.CountyRecyclerAdapter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FragmentManageCounty : Fragment(R.layout.fragment_manage_county),
    CountyRecyclerAdapter.DragListener {
    private var _binding: FragmentManageCountyBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels()
    private val countyAdapter = CountyRecyclerAdapter(this)
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageCountyBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = countyAdapter
        }

        lifecycleScope.launch(IO) {
            val counties = managerViewModel.getAllCountiesNotLive()
            countyAdapter.setCounties(counties)
        }

        val callback = CountyItemTouchHelperCallback(countyAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onPause() {
        super.onPause()
        managerViewModel.updateCounties(countyAdapter.getCounties())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}