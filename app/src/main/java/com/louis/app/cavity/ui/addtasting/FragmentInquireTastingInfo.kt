package com.louis.app.cavity.ui.addtasting

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireTastingInfoBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.DatePicker
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.util.PermissionChecker
import com.louis.app.cavity.util.collectAs
import com.louis.app.cavity.util.extractMargin
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setupNavigation

class FragmentInquireTastingInfo : Step(R.layout.fragment_inquire_tasting_info) {
    private lateinit var snackbarProvider: SnackbarProvider
    private lateinit var permissionChecker: PermissionChecker
    private var _binding: FragmentInquireTastingInfoBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val addTastingViewModel: AddTastingViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var datePicker: DatePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionChecker = object : PermissionChecker(this, arrayOf(NOTIFICATION_PERMISSION)) {
                override fun onPermissionsAccepted() {
                    submit()
                }

                override fun onPermissionsDenied() {
                    snackbarProvider.onShowSnackbarRequested(R.string.permissions_denied_external)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireTastingInfoBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        snackbarProvider = activity as SnackbarProvider
        super.setPeekSiblingsSteps(false)

        applyInsets()
        initFriendChips()
        initDatePicker()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        binding.constraint.prepareWindowInsets { view, windowInsets, left, _, right, _ ->
            view.updatePadding(left = left, right = right)
            windowInsets
        }

        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, _, right, _ ->
            view.updatePadding(left = left, right = right)
            WindowInsetsCompat.CONSUMED
        }

        val initialMargin = binding.buttonSubmit.extractMargin()

        binding.buttonSubmit.prepareWindowInsets { view, _, _, _, _, bottom ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(bottom = initialMargin.bottom + bottom)

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initFriendChips() {
        addTastingViewModel.friends.observe(viewLifecycleOwner) {
            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(it)
                .into(binding.friendsChipGroup)
                .useAvatar(true)
                .selectable(true)
                .emptyText(getString(R.string.placeholder_friend))
                .build()
                .go()
        }
    }

    private fun initDatePicker() {
        datePicker = DatePicker(
            childFragmentManager,
            associatedTextLayout = binding.dateLayout,
            title = getString(R.string.tasting_date),
            clearable = false,
            defaultDate = System.currentTimeMillis()
        ).apply {
            onDateChangedListener = { addTastingViewModel.tastingDate = it }
        }
    }

    private fun observe() {
        addTastingViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }
    }

    private fun setListeners() {
        binding.buttonAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        binding.buttonSubmit.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission()
            } else {
                submit()
            }
        }
    }

    @RequiresApi(33)
    private fun requestNotificationPermission() {
        permissionChecker.askPermissionsIfNecessary()
    }

    private fun showAddFriendDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_friend,
            hint = R.string.add_friend_label,
            icon = R.drawable.ic_person,
        ) {
            addItemViewModel.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)
            .show(dialogResources)
    }

    private fun submit() {
        val valid = binding.dateLayout.validate() and binding.opportunityLayout.validate()

        if (valid) {
            with(binding) {
                val opportunity = opportunity.text.toString().trim()
                val isMidday = rbMidday.isChecked
                val friends = friendsChipGroup.collectAs<Friend>()

                val ok = addTastingViewModel.submitTasting(opportunity, isMidday, friends)
                if (ok) stepperFragment?.goToNextPage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        private const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }
}
