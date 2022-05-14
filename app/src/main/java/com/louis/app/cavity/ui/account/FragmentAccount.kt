package com.louis.app.cavity.ui.account

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAccountBinding
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentAccount : Fragment(R.layout.fragment_account) {
    private lateinit var askPermission: ActivityResultLauncher<String>
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private val importExportViewModel: ImportExportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                handlePermisionResults(it)
            }

        val currentBackStackEntry = findNavController().currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle

        savedStateHandle.getLiveData<Boolean>(FragmentLogin.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry) {
                if (!it) {
                    val startDestination = findNavController().graph.startDestination
                    val action = FragmentAccountDirections.accountToHome()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()

                    findNavController().navigate(action, navOptions)
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
        loginViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.email.text = it
            } else {
                val action = FragmentAccountDirections.accountToLogin()
                findNavController().navigate(action)
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
                        binding.coordinator.showSnackbar(R.string.base_error)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        binding.progressBar.setVisible(false)
                        binding.coordinator.showSnackbar(R.string.export_done)
                    }
                    else -> Unit
                }
            } else {
                binding.progressBar.setVisible(false)
            }
        }
    }

    private fun setListeners() {
        binding.exportBtn.setOnClickListener {
            if (hasPermissions()) {
                // accountViewModel.export()
                val action = FragmentAccountDirections.accountToImportExport(
                    isImport = false,
                    title = getString(R.string.export)
                )

                findNavController().navigate(action)
            } else {
                askPermission.launch(REQUIRED_PERMISSION)
            }
        }

        binding.importBtn.setOnClickListener {
            if (hasPermissions()) {
                val action = FragmentAccountDirections.accountToImportExport(
                    isImport = true,
                    title = getString(R.string.import_)
                )

                findNavController().navigate(action)
            } else {
                askPermission.launch(REQUIRED_PERMISSION)
            }
        }
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

    private fun hasPermissions() = ContextCompat.checkSelfPermission(
        requireContext(),
        REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    private fun handlePermisionResults(permission: Boolean) {
        if (permission) {
            importExportViewModel.export()
        } else {
            binding.coordinator.showSnackbar(R.string.permissions_denied_external)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
