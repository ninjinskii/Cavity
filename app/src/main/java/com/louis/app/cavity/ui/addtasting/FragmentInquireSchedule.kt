package com.louis.app.cavity.ui.addtasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireScheduleBinding
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.ui.tasting.notifications.TastingAlarmScheduler
import com.louis.app.cavity.util.setupNavigation

class FragmentInquireSchedule : Step(R.layout.fragment_inquire_schedule) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentInquireScheduleBinding? = null
    private val binding get() = _binding!!
    private val addTastingViewModel: AddTastingViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireScheduleBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        binding.appBar.toolbar.setNavigationOnClickListener {
            stepperFragment?.requestPreviousPage()
        }

        snackbarProvider = activity as SnackbarProvider

        initRecylerView()
        observe()
        setListener()
    }

    private fun initRecylerView() {
        val tastingBottleAdapter = TastingBottleAdapter()
        val space = requireContext().resources.getDimension(R.dimen.small_margin)

        binding.tastingBottleList.apply {
            adapter = tastingBottleAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
            setHasFixedSize(true)
            addItemDecoration(SpaceGridItemDecoration(space.toInt()))
        }

        addTastingViewModel.tastingBottles.observe(viewLifecycleOwner) {
            tastingBottleAdapter.submitList(it)
        }
    }

    private fun observe() {
        addTastingViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }

        addTastingViewModel.tastingSaved.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { tasting ->
                TastingAlarmScheduler.scheduleTastingAlarm(requireContext(), tasting)
                snackbarProvider.onShowSnackbarRequested(R.string.tasting_created)
                findNavController().popBackStack()
            }
        }

        addTastingViewModel.cancelTastingAlarms.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { tastingsToCancel ->
                tastingsToCancel.forEach { tasting ->
                    TastingAlarmScheduler.cancelTastingAlarm(requireContext(), tasting)
                }
            }
        }
    }

    private fun setListener() {
        binding.buttonSubmit.setOnClickListener {
            if (needConfirmDialog()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.confirm_switch_tasting)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        addTastingViewModel.saveTasting()
                    }
                    .show()
            } else {
                addTastingViewModel.saveTasting()
            }

        }
    }

    private fun needConfirmDialog(): Boolean {
        return addTastingViewModel.tastingBottles.value?.any { it.showOccupiedWarning } == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
