package com.louis.app.cavity.ui.manager.review

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentManageReview: Fragment(R.layout.fragment_manage_base) {
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!

    // TODO: Check VM scope carefully
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        initRecyclerView()
    }

    private fun initRecyclerView() {

    }

    private fun showEditReviewDialog(review: Review) {
        SimpleInputDialog(requireContext(), layoutInflater).showForEdit(
            title = R.string.rename_review,
            hint = R.string.review,
            icon = R.drawable.ic_grade,
            editedString = review.contestName
        ) {
            val updatedReview = review.copy(contestName = it)
            managerViewModel.updateReview(updatedReview)
        }
    }

    private fun showConfirmDeleteDialog(review: Review) {
        MaterialAlertDialogBuilder(requireContext())
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
