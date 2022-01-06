package com.louis.app.cavity.ui.manager.review

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentManageReview: Fragment(R.layout.fragment_manage_base) {
    lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val reviewAdapter = ReviewRecyclerAdapter(
            onRename = { review: Review -> showEditReviewDialog(review) },
            onDelete = { review: Review -> showConfirmDeleteDialog(review) }
        )

        binding.recyclerView.apply {
            adapter = reviewAdapter
            layoutManager = LinearLayoutManager(context)
        }

        managerViewModel.getReviewWithFilledReviews().observe(viewLifecycleOwner) {
            reviewAdapter.submitList(it)
        }
    }

    private fun showEditReviewDialog(review: Review) {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.rename_review,
            hint = R.string.contest_name,
            icon = R.drawable.ic_contest
        ) {
            val updatedReview = review.copy(contestName = it)
            managerViewModel.updateReview(updatedReview)
        }

        simpleInputDialog.show(dialogResources)
    }

    private fun showConfirmDeleteDialog(review: Review) {
        LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
            .setMessage(R.string.confirm_review_delete)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                managerViewModel.deleteReview(review)
                binding.coordinator.showSnackbar(R.string.review_deleted)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
