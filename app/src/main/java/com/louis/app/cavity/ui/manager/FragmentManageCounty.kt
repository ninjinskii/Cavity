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
import com.louis.app.cavity.util.L
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

class FragmentManageCounty : Fragment(R.layout.fragment_manage_county) {
    private var _binding: FragmentManageCountyBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageCountyBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val countyAdapter = CountyRecyclerAdapter()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = countyAdapter
        }

        lifecycleScope.launch(IO) {
            val counties = managerViewModel.getAllCountiesNotLive()
            countyAdapter.submitList(counties)
        }

        val dragDirs = ItemTouchHelper.UP or
                ItemTouchHelper.DOWN or
                ItemTouchHelper.START or
                ItemTouchHelper.END

        val swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        val callback = object : ItemTouchHelper.SimpleCallback(dragDirs, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                val counties = countyAdapter.currentList.toMutableList()
                Collections.swap(counties, from, to)
                countyAdapter.notifyItemMoved(from, to)
                //countyAdapter.submitList(counties)
//                L.v("from: $from, fromId: $fromId, to: $to, toId: $toId")
//                managerViewModel.swapCounties(fromId, to, toId, from)
                //countyAdapter.submitList(countyAdapter.currentList.toMutableList().)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        }

        ItemTouchHelper(callback).run { attachToRecyclerView(binding.recyclerView) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}