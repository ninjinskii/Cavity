package com.louis.app.cavity.ui.bottle.steps

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
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.ui.bottle.stepper.FragmentStepper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar

class FragmentInquireExpertAdvice : Fragment(R.layout.fragment_inquire_expert_advice) {
    private lateinit var binding: FragmentInquireExpertAdviceBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireExpertAdviceBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isHideable = false

        registerStepperWatcher()
        initRecyclerView()
        observe()
        setListeners()
    }

    private fun registerStepperWatcher() {
        val stepperFragment =
            parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = true
            override fun onPageRequestAccepted() {
            }
        })
    }

    private fun initRecyclerView() {
        val adviceAdapter = ExpertAdviceRecyclerAdapter {
            addBottleViewModel.removeExpertAdvice(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = adviceAdapter
        }

        addBottleViewModel.getAllExpertAdvices().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                peekBottomSheet()
            }

            binding.dynamicListHint.text =
                resources.getQuantityString(R.plurals.expert_advices, it.size, it.size)
            adviceAdapter.submitList(it)
        }
    }

    private fun observe() {
        addBottleViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }
    }

    private fun setListeners() {
        binding.buttonAddExpertAdvice.setOnClickListener {
            val constestName = binding.contestName.text.toString().trim()
            val rate = binding.rate.text.toString().trim()

            try {
                val advice = makeExpertAdvice(constestName, rate)
                addBottleViewModel.addExpertAdvice(advice)
            } catch (e: IllegalStateException) {
                binding.coordinator.showSnackbar(R.string.base_error)
            }

            binding.contestName.setText("")
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

    private fun makeExpertAdvice(contestName: String, rate: String): ExpertAdvice {
        val bottleId = addBottleViewModel.bottleId
            ?: throw IllegalStateException("bottleId is null")

        with(binding) {
            return when (val checked = rbGroupType.checkedButtonId) {
                R.id.rbMedal -> {
                    val value = when (checked) {
                        R.id.rbBronze -> 0
                        R.id.rbSilver -> 1
                        else -> 2
                    }
                    ExpertAdvice(0, contestName, 1, 0, 0, 0, value, bottleId)
                }
                R.id.rbRate100 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    0,
                    1,
                    rate.toInt(),
                    bottleId
                )
                R.id.rbRate20 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    1,
                    0,
                    rate.toInt(),
                    bottleId
                )
                else -> {
                    val value = when (rbGroupType.checkedButtonId) {
                        R.id.rbStar1 -> 0
                        R.id.rbStar2 -> 1
                        else -> 2
                    }
                    ExpertAdvice(
                        0,
                        contestName,
                        0,
                        1,
                        0,
                        0,
                        value,
                        bottleId
                    )
                }
            }
        }
    }
}
