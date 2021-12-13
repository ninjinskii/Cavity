package com.louis.app.cavity.ui.addtasting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireScheduleBinding
import com.louis.app.cavity.model.Tasting
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.ui.tasting.notifications.TastingReceiver
import com.louis.app.cavity.ui.tasting.notifications.TastingReceiver.Companion.EXTRA_TASTING_ID
import com.louis.app.cavity.util.setupNavigation
import java.util.*

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
        val tastingBottleAdapter = TastingBottleAdapter(
            onFridgeChecked = { bottleId, shouldFridge ->
                addTastingViewModel.onBottleShouldFridgeChanged(bottleId, shouldFridge)
            },
            onJugChecked = { bottleId, shouldJug ->
                addTastingViewModel.onBottleShouldJugChanged(bottleId, shouldJug)
            }
        )

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

        addTastingViewModel.notificationEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { tasting ->
                scheduleTastingAlarm(tasting)
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
                        findNavController().popBackStack()
                    }
                    .show()
            } else {
                addTastingViewModel.saveTasting()
                findNavController().popBackStack()
            }

        }
    }

    private fun needConfirmDialog(): Boolean {
        return addTastingViewModel.tastingBottles.value?.any { it.showOccupiedWarning } == true
    }

    private fun scheduleTastingAlarm(tasting: Tasting) {
        val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, TastingReceiver::class.java).let { intent ->
            intent.putExtra(EXTRA_TASTING_ID, tasting.id)
            PendingIntent.getBroadcast(
                context,
                tasting.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, if (tasting.isMidday) 9 else 16)
        }

        alarmMgr?.set(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
    }

    // TODO: check if it works, not sure rn
    private fun cancelTastingAlarm(tasting: Tasting) {
        val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(context, TastingReceiver::class.java).let { intent ->
            intent.putExtra(EXTRA_TASTING_ID, tasting.id)
            PendingIntent.getBroadcast(
                context,
                tasting.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        alarmMgr?.cancel(alarmIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
