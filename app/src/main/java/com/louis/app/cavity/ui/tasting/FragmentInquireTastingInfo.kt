package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireTastingInfoBinding

class FragmentInquireTastingInfo : Fragment(R.layout.fragment_inquire_tasting_info) {
    private var _binding: FragmentInquireTastingInfoBinding? = null
    private val binding get() = _binding!!
    private val tastingViewModel: TastingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireTastingInfoBinding.bind(view)

        initNumberPickers()
    }

    private fun initNumberPickers() {
        with(binding) {
            cellarTemp.maxValue = resources.getInteger(R.integer.max_cellar_temp)
            cellarTemp.minValue = resources.getInteger(R.integer.min_cellar_temp)
            fridgeTemp.minValue = resources.getInteger(R.integer.min_fridge_temp)
            fridgeTemp.minValue = resources.getInteger(R.integer.min_fridge_temp)
            freezerTemp.minValue = resources.getInteger(R.integer.min_freezer_temp)
            freezerTemp.minValue = resources.getInteger(R.integer.min_freezer_temp)
        }

        tastingViewModel.lastTasting.observe(viewLifecycleOwner) {
            with(binding) {
                cellarTemp.value = it.cellarTemp
                fridgeTemp.value = it.fridgeTemp
                freezerTemp.value = it.freezerTemp
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
