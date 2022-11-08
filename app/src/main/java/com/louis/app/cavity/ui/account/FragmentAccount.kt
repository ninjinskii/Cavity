package com.louis.app.cavity.ui.account

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAccountBinding
import com.louis.app.cavity.util.PermissionChecker
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentAccount : Fragment(R.layout.fragment_account) {
    private lateinit var readPermissionChecker: PermissionChecker
    private lateinit var writePermissionChecker: PermissionChecker
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
