package com.louis.app.cavity.ui.manager.county

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.louis.app.cavity.databinding.BottomSheetCountyOptionsBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.L

class CountyOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCountyOptionsBinding? = null
    private val binding get() = _binding!!
    private val args: CountyOptionsBottomSheetArgs by navArgs()
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCountyOptionsBinding.inflate(inflater)

        // Ensuring we are scoping our VM to the good fragment
        //L.v("${requireParentFragment().requireParentFragment()}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            countyName.text = args.countyName
            bottleCount.text = args.bottlesCount.toString()

            editCounty.setOnClickListener {

            }

            deleteCounty.setOnClickListener {
                managerViewModel.deleteCounty(args.countyId)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}