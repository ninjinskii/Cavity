package com.louis.app.cavity.ui.manager.grape

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageGrapeBinding
import com.louis.app.cavity.ui.manager.ManagerViewModel

class FragmentManageGrape: Fragment(R.layout.fragment_manage_grape) {
    private var _binding: FragmentManageGrapeBinding? = null
    private val binding get() = _binding!!
    // TODO: Check VM scope carefully
    private val managerViewModel: ManagerViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageGrapeBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter =
        }

        managerViewModel.getGrapeWithQuantifiedGrapes().observe(viewLifecycleOwner) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
