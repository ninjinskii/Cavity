package com.louis.app.cavity.ui.account.fileimport

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentImportFilesBinding
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentImportFiles : Fragment(R.layout.fragment_import_files) {
    private lateinit var pickFiles: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentImportFilesBinding? = null
    private val binding get() = _binding!!
    private val fileImportViewModel: FileImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickFiles =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { filesUris ->
                onFileSelected(filesUris)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportFilesBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        applyInsets()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                fileImportViewModel.event.collect { event ->
                    when (event) {
                        is FileImportEvent.FileImported ->
                            binding.coordinator.showSnackbar(
                                getString(R.string.file_imported, event.binded, event.total)
                            )
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.btnImportFiles.setOnClickListener {
            pickFiles.launch(arrayOf("image/*", "application/pdf"))
        }
    }

    private fun onFileSelected(uris: List<Uri>) {
        val act = activity as ActivityMain

        uris.forEach {
            act.requestMediaPersistentPermission(it, silent = true)
        }

        fileImportViewModel.bindFiles(uris, requireContext().contentResolver)
    }
}
