package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageCountyBinding
import com.louis.app.cavity.util.L
import kotlinx.android.synthetic.main.item_bottle.*

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

        managerViewModel.getCountiesWithWines().observe(viewLifecycleOwner) {
            countyAdapter.submitList(it)
        }

        val dragDirs = ItemTouchHelper.UP or
                ItemTouchHelper.DOWN or
                ItemTouchHelper.START or
                ItemTouchHelper.END

        val swipeDirs = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        val callback = object : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val fromId = countyAdapter.getItemId(from)
                val to = target.adapterPosition
                val toId = countyAdapter.getItemId(to)



                L.v("from: $from, fromId: $fromId, to: $to, toId: $toId")

                managerViewModel.swapCounties(fromId, to, toId, from)
                //countyAdapter.submitList(countyAdapter.currentList.toMutableList().)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        }

        ItemTouchHelper(callback).run { attachToRecyclerView(binding.recyclerView) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}