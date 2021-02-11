package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddReviewBinding
import com.louis.app.cavity.databinding.FragmentInquireReviewBinding
import com.louis.app.cavity.ui.addbottle.adapter.FilledReviewRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.ReviewViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showKeyboard

class FragmentInquireReviews : Fragment(R.layout.fragment_inquire_review) {
    private lateinit var stepperx: Stepper
    private var _binding: FragmentInquireReviewBinding? = null
    private val binding get() = _binding!!
    private val reviewViewModel: ReviewViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireReviewBinding.bind(view)

        stepperx = parentFragment as Stepper
        reviewViewModel.start(stepperx.getBottleId())

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val reviewAdapter = FilledReviewRecyclerAdapter(
            onValueChangedListener = { fReview, value ->
                reviewViewModel.updateFilledReview(fReview, value)
            },

            onDeleteListener = {
                reviewViewModel.removeFilledReview(it)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = reviewAdapter
        }

        reviewViewModel.getFReviewAndReview().observe(viewLifecycleOwner) {
            toggleRvPlaceholder(it.isEmpty())
            reviewAdapter.submitList(it)
        }
    }

    private fun observe() {
        reviewViewModel.reviewDialogEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { checkableReviews ->
                val copy = checkableReviews.map { it.copy() }.toMutableList()
                val names = checkableReviews.map { it.review.contestName }.toTypedArray()
                val bool = checkableReviews.map { it.isChecked }.toBooleanArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.select_reviews)
                    .setMultiChoiceItems(names, bool) { _, pos, checked ->
                        copy[pos].isChecked = checked
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                    }
                    .setPositiveButton(R.string.submit) { _, _ ->
                        reviewViewModel.submitCheckedReviews(copy)
                    }
                    .show()
            }
        }
    }

    private fun setListeners() {
        with(binding) {
            buttonAddReview.setOnClickListener { showAddReviewDialog() }
            buttonSelectReview.setOnClickListener { reviewViewModel.requestReviewDialog() }
            buttonSelectReviewSecondary.setOnClickListener { reviewViewModel.requestReviewDialog() }
            buttonSkip.setOnClickListener { stepperx.requestNextPage() }
            stepper.next.setOnClickListener { stepperx.requestNextPage() }
            stepper.previous.setOnClickListener { stepperx.requestPreviousPage() }
        }
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_review)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.contestName.text.toString().trim()
                val type = getReviewType(dialogBinding.rbGroupType.checkedButtonId)

                reviewViewModel.insertReview(name, type)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.contestName.post { dialogBinding.contestName.showKeyboard() }
        dialogBinding.rbMedal.performClick()
    }

    private fun getReviewType(@IdRes button: Int) = when (button) {
        R.id.rbMedal -> 0
        R.id.rbRate20 -> 1
        R.id.rbRate100 -> 2
        else -> 3
    }

    private fun toggleRvPlaceholder(toggle: Boolean) {
        with(binding) {
            reviewIconEmpty.setVisible(toggle)
            explanation.setVisible(toggle)
            buttonSelectReviewSecondary.setVisible(toggle)
            buttonSkip.setVisible(toggle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
