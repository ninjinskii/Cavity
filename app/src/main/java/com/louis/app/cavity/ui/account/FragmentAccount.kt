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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAccountBinding
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentAccount : Fragment(R.layout.fragment_account) {
    private lateinit var askPermission: ActivityResultLauncher<String>
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var transitionHelper: TransitionHelper

    private var wannaImport = false
    private var wannaImportFiles = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postponeEnterTransition()
        transitionHelper = TransitionHelper(this).apply {
            setFadeThroughOnEnterAndExit()
        }

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
        loginViewModel.user.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.email.text = it
                startPostponedEnterTransition()
            } else {
                val action = FragmentAccountDirections.accountToLogin()
                findNavController().navigate(action)
            }
        }
    }

    private fun setListeners() {
        binding.exportBtn.setOnClickListener {
            navigateToImportExport(false)
        }

        binding.importBtn.setOnClickListener {
            navigateToImportExport(true)
        }

        binding.imageBtn.setOnClickListener {
            navigateToImportFiles()
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

    private fun hasPermissions(perm: String) = ContextCompat.checkSelfPermission(
        requireContext(),
        perm
    ) == PackageManager.PERMISSION_GRANTED

    private fun handlePermisionResults(permission: Boolean) {
        if (permission) {
            if (wannaImportFiles) {
                navigateToImportFiles()
            } else {
                navigateToImportExport(wannaImport)
            }
        } else {
            binding.coordinator.showSnackbar(R.string.permissions_denied_external)
        }
    }

    private fun navigateToImportExport(isImport: Boolean) {
        if (hasPermissions(WRITE_PERMISSION)) {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, true)

            val title = if (isImport) R.string.import_ else R.string.export
            val action = FragmentAccountDirections.accountToImportExport(
                isImport = isImport,
                title = getString(title)
            )

            findNavController().navigate(action)
        } else {
            wannaImport = isImport
            askPermission.launch(WRITE_PERMISSION)
        }
    }

    private fun navigateToImportFiles() {
        if (hasPermissions(READ_PERMISSION)) {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, true)

            val action = FragmentAccountDirections.accountToImportFiles()
            wannaImportFiles = false
            findNavController().navigate(action)
        } else {
            wannaImportFiles = true
            askPermission.launch(READ_PERMISSION)
        }
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
