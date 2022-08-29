package com.louis.app.cavity.ui.account.fileimport

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentImportFilesBinding
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentImportFiles : Fragment(R.layout.fragment_import_files) {
    private lateinit var pickFiles: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentImportFilesBinding? = null
    private val binding get() = _binding!!
    private val fileImportViewModel: FileImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TransitionHelper(this).setSharedAxisTransition(
            MaterialSharedAxis.Z,
            navigatingForward = false
        )

        pickFiles =
            registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { filesUris ->
                onFileSelected(filesUris)
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportFilesBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)

        observe()
        setListeners()
    }

    private fun observe() {
        fileImportViewModel.fileImportedEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { values ->
                binding.coordinator.showSnackbar(
                    getString(R.string.file_imported, values.first, values.second)
                )
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

        fileImportViewModel.bindFiles(uris)
    }
}
