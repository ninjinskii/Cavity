package com.louis.app.cavity.ui.manager.county

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageCountyBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.relation.CountyWithWines
import com.louis.app.cavity.ui.manager.FragmentManagerDirections
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.ui.manager.recycler.CountyItemTouchHelperCallback
import com.louis.app.cavity.ui.manager.recycler.CountyRecyclerAdapter
import com.louis.app.cavity.util.L

class FragmentManageCounty : Fragment(R.layout.fragment_manage_county),
        CountyRecyclerAdapter.DragListener {
    private var _binding: FragmentManageCountyBinding? = null
    private val binding get() = _binding!!

    // TODO: Check VM scope carefully
    private val managerViewModel: ManagerViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
    )
    private val countyAdapter = CountyRecyclerAdapter(this) { showOptionsDialog(it) }
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageCountyBinding.bind(view)

        // Ensuring we are scoping our VM to the good fragment
        L.v("${requireParentFragment()}")

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = countyAdapter
        }

        managerViewModel.getCountiesWithWines().observe(viewLifecycleOwner) {
            countyAdapter.setCounties(it)
        }

        val callback = CountyItemTouchHelperCallback(countyAdapter)

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun showOptionsDialog(countyWithWines: CountyWithWines) {
        managerViewModel.deleteGrape(Grape(1, ""))
//        val (county, wines) = countyWithWines
//        val action = FragmentManagerDirections.managerToCountyOptions(
//                county.countyId,
//                county.name,
//                county.prefOrder,
//                wines.size
//        )
//
//        findNavController().navigate(action)
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