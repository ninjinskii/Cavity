package com.louis.app.cavity.ui.account

import android.Manifest
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAccountBinding
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.account.worker.PruneWorker
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.util.DateFormatter
import com.louis.app.cavity.util.PermissionChecker
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentAccount : Fragment(R.layout.fragment_account) {
    private lateinit var readPermissionChecker: PermissionChecker
    private lateinit var writePermissionChecker: PermissionChecker
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val importExportViewModel: ImportExportViewModel by viewModels()

    private lateinit var transitionHelper: TransitionHelper

    private var wannaImport = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postponeEnterTransition()
        transitionHelper = TransitionHelper(this).apply {
            setFadeThroughOnEnterAndExit()
        }

        readPermissionChecker =
            object : PermissionChecker(this, arrayOf(READ_PERMISSION)) {
                override fun onPermissionsAccepted() {
                    navigateToImportFiles()
                }

                override fun onPermissionsDenied() {
                    binding.coordinator.showSnackbar(R.string.permissions_denied_external)
                }
            }

        writePermissionChecker = object : PermissionChecker(this, arrayOf(WRITE_PERMISSION)) {
            override fun onPermissionsAccepted() {
                navigateToImportExport(wannaImport)
            }

            override fun onPermissionsDenied() {
                binding.coordinator.showSnackbar(R.string.permissions_denied_external)
            }
        }

        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle

        savedStateHandle.getLiveData<Boolean>(FragmentLogin.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) {
                if (!it) {
                    val startDestination = findNavController().graph.startDestinationId
                    val action = FragmentAccountDirections.accountToHome()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()

                    findNavController().navigate(action, navOptions)
                } else {
                    startPostponedEnterTransition()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountBinding.bind(view)

        setupNavigation(binding.toolbar)

        observe()
        setListeners()
        setupToolbar()
    }

    private fun observe() {
        loginViewModel.account.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.email.text = it.email

                val date = DateFormatter.formatDate(it.lastUpdateTime, "dd MMMM yyyy, HH:mm")
                binding.lastBackup.text = getString(R.string.last_action, date)
                setupLastBackupStatus()
                startPostponedEnterTransition()
            } else {
                val action = FragmentAccountDirections.accountToLogin()
                findNavController().navigate(action)
            }
        }

        loginViewModel.deletedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                importExportViewModel.cleanAccountDatabase()
            }
        }

        importExportViewModel.workProgress.observe(viewLifecycleOwner) {
            if (it != null) {
                when (it.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> {
                        binding.progressBar.setVisible(true)
                    }

                    WorkInfo.State.FAILED -> {
                        binding.progressBar.setVisible(false)
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        if (it.tags.contains(PruneWorker.WORK_TAG)) {
                            binding.progressBar.setVisible(false)
                            loginViewModel.logout()
                        }
                    }

                    else -> Unit
                }
            } else {
                binding.progressBar.setVisible(false)
            }
        }

        importExportViewModel.autoBackupWorkProgress.observe(viewLifecycleOwner) {
            setupLastBackupStatus()
            loginViewModel.updateAccountLastUpdateLocally()
        }
    }

    private fun setListeners() {
        binding.exportBtn.setOnClickListener {
            wannaImport = false
            writePermissionChecker.askPermissionsIfNecessary()
        }

        binding.importBtn.setOnClickListener {
            wannaImport = true
            writePermissionChecker.askPermissionsIfNecessary()
        }

        binding.imageBtn.setOnClickListener {
            readPermissionChecker.askPermissionsIfNecessary()
        }

        binding.deleteBtn.setOnClickListener {
            val resources = SimpleInputDialog.DialogContent(
                title = R.string.delete_account,
                hint = R.string.password,
                icon = R.drawable.ic_password,
            ) {
                loginViewModel.deleteAccount(it)
            }

            SimpleInputDialog(
                requireContext(),
                layoutInflater,
                viewLifecycleOwner,
                passwordInput = true
            ).show(resources)
        }

        binding.toggleAutoBackup.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            isChecked = settingsViewModel.getAutoBackup()

            setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setAutoBackup(isChecked)

                if (isChecked) {
                    importExportViewModel.enableAutoBackups()
                } else {
                    importExportViewModel.disableAutoBackups()
                }
            }
        }
    }

    private fun setupLastBackupStatus() {
        val status = loginViewModel.getLastAutoBackupStatus()
        val message = if (status != 0) status else R.string.backup_status_disabled

        binding.backupStatusMore.text = getString(message)
        binding.backupStateIcon.imageTintList = getColorForStatus(status)
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.logout) {
                loginViewModel.logout()
                return@setOnMenuItemClickListener true
            }

            false
        }
    }

    private fun navigateToImportExport(isImport: Boolean) {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, true)

        val title = if (isImport) R.string.import_ else R.string.export
        val action = FragmentAccountDirections.accountToImportExport(
            isImport = isImport,
            title = getString(title)
        )

        findNavController().navigate(action)
    }

    private fun navigateToImportFiles() {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, true)

        val action = FragmentAccountDirections.accountToImportFiles()
        findNavController().navigate(action)
    }

    private fun getColorForStatus(@StringRes status: Int): ColorStateList {
        val colorRes = when (status) {
            R.string.backup_status_active -> R.color.cavity_green
            R.string.backup_status_pause -> R.color.cavity_yellow
            R.string.backup_status_suspicious -> R.color.cavity_yellow
            else /* 0 */ -> R.color.cavity_grey
        }

        return ColorStateList.valueOf(requireContext().getColor(colorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
