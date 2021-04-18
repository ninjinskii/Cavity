package com.louis.app.cavity.ui.manager.naming

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel

class FragmentManageNaming : Fragment(R.layout.fragment_manage_base) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val namingAdapter = NamingRecyclerAdapter(
            onRename = { naming: Naming -> showEditNamingDialog(naming) },
            onDelete = { naming: Naming -> managerViewModel.deleteNaming(naming) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = namingAdapter
        }

        managerViewModel.getNamingsWithWines().observe(viewLifecycleOwner) {
            namingAdapter.submitList(it)
        }
    }

    private fun showEditNamingDialog(naming: Naming) {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.rename_naming,
            hint = R.string.naming
        ) {
            val updatedNaming = naming.copy(naming = it)
            managerViewModel.updateNaming(updatedNaming)
        }

        simpleInputDialog.show(dialogResources)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
