package com.louis.app.cavity.ui.addbottle.steps

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireExpertAdviceBinding
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.stepper.Step
import com.louis.app.cavity.util.*

// TODO: use material dialogs instead of text fields
class FragmentInquireExpertAdvice : Fragment(R.layout.fragment_inquire_expert_advice), Step {
    private var _binding: FragmentInquireExpertAdviceBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentInquireExpertAdviceBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = false
        }

        initRecyclerView()
        setListeners()
    }

    private fun initRecyclerView() {
        val adviceAdapter = ExpertAdviceRecyclerAdapter {
            addBottleViewModel.expertAdviceManager.removeExpertAdvice(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = adviceAdapter
        }

        addBottleViewModel.expertAdviceManager.expertAdvices.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) peekBottomSheet()
            else hideBottomSheet()

            binding.dynamicListHint.text =
                resources.getQuantityString(R.plurals.expert_advices, it.size, it.size)

            // Using toMutableList() to change the list reference, otherwise our call to submitList will be ignored
            adviceAdapter.submitList(it.toMutableList())
        }
    }

    private fun setListeners() {
        binding.buttonAddExpertAdvice.setOnClickListener {
            makeExpertAdvice()
        }

        binding.rbGroupType.addOnButtonCheckedListener { _, _, _ -> revealViews() }

        binding.buttonShowBottomSheet.setOnClickListener {
            with(bottomSheetBehavior) {
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                } else if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.buttonShowBottomSheet.setImageResource(R.drawable.ic_down)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.buttonShowBottomSheet.setImageResource(R.drawable.ic_up)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

    }

    private fun revealViews() {
        with(binding) {
            when (rbGroupType.checkedButtonId) {
                R.id.rbMedal -> {
                    rbGroupMedal.setVisible(true)
                    rbGroupStars.setVisible(false)
                    rateLayout.setVisible(false, invisible = true)
                }
                R.id.rbRate100 -> {
                    rateLayout.setVisible(true)
                    rbGroupMedal.setVisible(false)
                    rbGroupStars.setVisible(false)
                }
                R.id.rbRate20 -> {
                    rateLayout.setVisible(true)
                    rbGroupMedal.setVisible(false)
                    rbGroupStars.setVisible(false)
                }
                R.id.rbStar -> {
                    rbGroupStars.setVisible(true)
                    rbGroupMedal.setVisible(false)
                    rateLayout.setVisible(false, invisible = true)
                }
            }
        }
    }

    private fun peekBottomSheet() {
        val tv = TypedValue()

        context?.let {
            if (it.theme.resolveAttribute(
                    android.R.attr.actionBarSize,
                    tv,
                    true
                )
            ) {
                bottomSheetBehavior.setPeekHeight(
                    TypedValue.complexToDimensionPixelSize(
                        tv.data,
                        resources.displayMetrics
                    ), true
                )
            }
        }
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.setPeekHeight(0, true)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun makeExpertAdvice() {
        with(binding) {
            val constestName = contestName.text.toString().trim()
            val rate = rate.text.toString().trim()
            val type = when (rbGroupType.checkedButtonId) {
                R.id.rbRate20 -> AdviceType.RATE_20 to rate.toInt()
                R.id.rbRate100 -> AdviceType.RATE_100 to rate.toInt()
                R.id.rbMedal -> {
                    val medal: Int = when (rbGroupMedal.checkedButtonId) {
                        R.id.rbBronze -> MedalColor.BRONZE.ordinal
                        R.id.rbSilver -> MedalColor.SILVER.ordinal
                        else -> MedalColor.GOLD.ordinal
                    }

                    AdviceType.MEDAL to medal
                }
                else -> {
                    val starsNumber: Int = when (rbGroupStars.checkedButtonId) {
                        R.id.rbStar1 -> Stars.STAR_1.ordinal
                        R.id.rbStar2 -> Stars.STAR_2.ordinal
                        else -> Stars.STAR_3.ordinal
                    }

                    AdviceType.STARS to starsNumber
                }
            }

            addBottleViewModel.expertAdviceManager.addExpertAdvice(constestName, type)
            contestName.setText("")
        }
    }

    override fun validate() = true

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
