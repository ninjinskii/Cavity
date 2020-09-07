package com.louis.app.cavity.ui.bottle.steps

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireExpertAdviceBinding
import com.louis.app.cavity.model.ExpertAdvice
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar

class FragmentInquireExpertAdvice : Fragment(R.layout.fragment_inquire_expert_advice) {
    private lateinit var binding: FragmentInquireExpertAdviceBinding
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireExpertAdviceBinding.bind(view)

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val adviceAdapter = ExpertAdviceRecyclerAdapter {
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = adviceAdapter
        }

        addBottleViewModel.getAllExpertAdvices().observe(viewLifecycleOwner) {
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
            val advice = makeExpertAdvice(constestName, rate)

            addBottleViewModel.addExpertAdvice(advice)
        }

        binding.rbGroupType.setOnCheckedChangeListener { _, _ -> revealViews() }
    }

    private fun revealViews() {
        with(binding) {
            when (rbGroupType.checkedRadioButtonId) {
                R.id.rbMedal -> {
                    rbGroupMedal.setVisible(true)
                    rbGroupStars.setVisible(false)
                    rateLayout.setVisible(false)
                }
                R.id.rbRate100 -> {
                    rateLayout.setVisible(true)
                    rbGroupMedal.setVisible(false)
                    rbGroupStars.setVisible(false)
                }
                R.id.rbStar -> {
                    rbGroupStars.setVisible(true)
                    rbGroupMedal.setVisible(false)
                    rateLayout.setVisible(false)
                }
            }
        }
    }

    private fun makeExpertAdvice(contestName: String, rate: String): ExpertAdvice {
        with(binding) {
            return when (rbGroupType.checkedRadioButtonId) {
                R.id.rbMedal -> {
                    val value = when {
                        rbBronze.isChecked -> 0
                        rbSilver.isChecked -> 1
                        else -> 2
                    }
                    ExpertAdvice(0, contestName, 1, 0, 0, 0, value, 0)
                }
                R.id.rbRate100 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    0,
                    1,
                    rate.toInt(),
                    0
                )
                R.id.rbRate20 -> ExpertAdvice(
                    0,
                    contestName,
                    0,
                    0,
                    1,
                    0,
                    rate.toInt(),
                    0
                )
                else -> {
                    val value = when {
                        rbStar1.isChecked -> 0
                        rbStar2.isChecked -> 1
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
                        0
                    )
                }
            }
        }
    }
}
