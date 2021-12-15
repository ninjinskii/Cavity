package com.louis.app.cavity.ui.addtasting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.SystemClock
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
                scheduleTastingAlarm(tasting)
                findNavController().popBackStack()
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

    private fun getTastingAlarmIntent(tasting: Tasting): PendingIntent {
        val flags =
            if (SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

        return Intent(context, TastingReceiver::class.java).let { intent ->
            intent.putExtra(EXTRA_TASTING_ID, tasting.id)
            PendingIntent.getBroadcast(
                context,
                tasting.id.hashCode(),
                intent,
                flags
            )
        }
    }

    private fun scheduleTastingAlarm(tasting: Tasting) {
        val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = getTastingAlarmIntent(tasting)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() + 1000 * 5
            set(Calendar.HOUR_OF_DAY, if (tasting.isMidday) 9 else 16)
        }

        alarmMgr?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 20 * 1000,
            alarmIntent
        )
    }

    // TODO: check if it works, not sure rn
    private fun cancelTastingAlarm(tasting: Tasting) {
        val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = getTastingAlarmIntent(tasting)
        alarmMgr?.cancel(alarmIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
