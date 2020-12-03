package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireReviewBinding
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel

// TODO: use material dialogs instead of text fields
class FragmentInquireReviews : Fragment(R.layout.fragment_inquire_review) {
    private var _binding: FragmentInquireReviewBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentInquireReviewBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        setListeners()
    }

    private fun initRecyclerView() {
        val reviewAdapter = ReviewRecyclerAdapter {
            addBottleViewModel.reviewManager.removeReview(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = reviewAdapter
        }

        addBottleViewModel.reviewManager.reviews.observe(viewLifecycleOwner) {

            // Using toMutableList() to change the list reference, otherwise our call to submitList will be ignored
            reviewAdapter.submitList(it.toMutableList())
        }
    }

    private fun setListeners() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
