package com.louis.app.cavity.ui.manager.review

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddReviewBinding
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.Review
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.showKeyboard
import com.louis.app.cavity.util.showSnackbar

class FragmentManageReview: Fragment(R.layout.fragment_manage_base) {
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!

    lateinit var simpleInputDialog: SimpleInputDialog

    // TODO: Check VM scope carefully
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater)

        initRecyclerView()
        setListener()
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

    private fun setListener() {
        binding.fab.setOnClickListener { showAddReviewDialog() }
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_review)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.contestName.text.toString()
                val type = getReviewType(dialogBinding.rbGroupType.checkedButtonId)

                managerViewModel.addReview(name, type)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.contestName.post { dialogBinding.contestName.showKeyboard() }
        dialogBinding.rbMedal.performClick()
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

    private fun getReviewType(@IdRes button: Int) = when (button) {
        R.id.rbMedal -> 0
        R.id.rbRate20 -> 1
        R.id.rbRate100 -> 2
        else -> 3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
