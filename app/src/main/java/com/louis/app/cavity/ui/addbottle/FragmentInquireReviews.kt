package com.louis.app.cavity.ui.addbottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddReviewBinding
import com.louis.app.cavity.databinding.FragmentInquireReviewBinding
import com.louis.app.cavity.ui.addbottle.adapter.FilledReviewRecyclerAdapter
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.viewmodel.ReviewViewModel
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showKeyboard

class FragmentInquireReviews : Fragment(R.layout.fragment_inquire_review) {
    private lateinit var stepperx: Stepper
    private var _binding: FragmentInquireReviewBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val reviewViewModel: ReviewViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentInquireReviewBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        stepperx = parentFragment as Stepper
        reviewViewModel.start(addBottleViewModel.bottleId)

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val reviewAdapter = FilledReviewRecyclerAdapter {
            // remove review
        }

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
                    .setTitle(R.string.select_grapes)
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
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_grape)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.contestName.text.toString()
                // TODO: get type
                reviewViewModel.insertReview(name, 0)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.contestName.post { dialogBinding.contestName.showKeyboard() }
    }

    private fun revealViews() {
//        with(binding) {
//            when (rbGroupType.checkedButtonId) {
//                R.id.rbMedal -> {
//                    rbGroupMedal.setVisible(true)
//                    rbGroupStars.setVisible(false)
//                    rateLayout.setVisible(false, invisible = true)
//                }
//                R.id.rbRate100 -> {
//                    rateLayout.setVisible(true)
//                    rbGroupMedal.setVisible(false)
//                    rbGroupStars.setVisible(false)
//                }
//                R.id.rbRate20 -> {
//                    rateLayout.setVisible(true)
//                    rbGroupMedal.setVisible(false)
//                    rbGroupStars.setVisible(false)
//                }
//                R.id.rbStar -> {
//                    rbGroupStars.setVisible(true)
//                    rbGroupMedal.setVisible(false)
//                    rateLayout.setVisible(false, invisible = true)
//                }
//            }
//        }
    }

    private fun makeReview() {
//        with(binding) {
//            val constestName = contestName.text.toString().trim()
//            val rate = rate.text.toString().trim()
//            val type = when (rbGroupType.checkedButtonId) {
//                R.id.rbRate20 -> ReviewType.RATE_20 to rate.toInt()
//                R.id.rbRate100 -> ReviewType.RATE_100 to rate.toInt()
//                R.id.rbMedal -> {
//                    val medal: Int = when (rbGroupMedal.checkedButtonId) {
//                        R.id.rbBronze -> MedalColor.BRONZE.ordinal
//                        R.id.rbSilver -> MedalColor.SILVER.ordinal
//                        else -> MedalColor.GOLD.ordinal
//                    }
//
//                    ReviewType.MEDAL to medal
//                }
//                else -> {
//                    val starsNumber: Int = when (rbGroupStars.checkedButtonId) {
//                        R.id.rbStar1 -> Stars.STAR_1.ordinal
//                        R.id.rbStar2 -> Stars.STAR_2.ordinal
//                        else -> Stars.STAR_3.ordinal
//                    }
//
//                    ReviewType.STARS to starsNumber
//                }
//            }
//
//            addBottleViewModel.reviewManager.addReview(constestName, type)
//            contestName.setText("")
//        }
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
