package com.louis.app.cavity.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentImportExportBinding
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation

class FragmentImportExport : Fragment(R.layout.fragment_import_export) {
    private var _binding: FragmentImportExportBinding? = null
    private val binding get() = _binding!!
    private val importExportViewModel: ImportExportViewModel by activityViewModels()
    private val args: FragmentImportExportArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportExportBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)
        importExportViewModel.checkHealth(args.isImport)

        updateUiState()
        observe()
        setListeners()
    }

    private fun updateUiState() {
        val import = args.isImport
        val arrowSrc = if (import) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
        val btn = if (import) R.string.import_ else R.string.export
        val btnSrc = if (import) R.drawable.ic_import else R.drawable.ic_export
        val healthcheck =
            if (import) R.string.healthcheck_import_warn else R.string.healthcheck_export_warn
        val explanation =
            if (import) R.string.backup_erasure_warn_import else R.string.backup_erasure_warn_export

        with(binding) {
            arrow.setImageResource(arrowSrc)
            erasureWarn.text = getString(explanation)
            warn.text = getString(healthcheck)
            submit.text = getString(btn)
            submit.setIconResource(btnSrc)
        }
    }

    private fun observe() {
        importExportViewModel.healthy.observe(viewLifecycleOwner) {
            changeWarningVisibilty(!it)
        }

        importExportViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.setVisible(it, invisible = true)
        }

        importExportViewModel.navigateToLogin.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                val action = FragmentImportExportDirections.importExportToLogin()
                findNavController().navigate(action)
            }
        }
    }

    private fun setListeners() {
        binding.confirmDanger.setOnClickListener {
            changeWarningVisibilty(visible = false)
        }

        binding.submit.setOnClickListener {
            importExportViewModel.run {
                if (args.isImport) import() else export()
            }
        }
    }

    private fun changeWarningVisibilty(visible: Boolean) {
        with(binding) {
            submit.isEnabled = !visible
            healthcheckWarn.setVisible(visible)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
