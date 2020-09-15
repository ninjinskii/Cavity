package com.louis.app.cavity.ui.bottle.steps

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireOtherInfoBinding
import com.louis.app.cavity.ui.bottle.AddBottleViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentInquireOtherInfo : Fragment(R.layout.fragment_inquire_other_info) {
    private lateinit var binding : FragmentInquireOtherInfoBinding
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private var bottlePdfPath: String? = null

    companion object {
        const val PICK_PDF_RESULT_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInquireOtherInfoBinding.bind(view)

        setListeners()
    }

    private fun setListeners() {
        binding.buttonAddPdf.setOnClickListener {
            if (bottlePdfPath == null) {
                val fileChooseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                fileChooseIntent.apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }

                try {
                    startActivityForResult(fileChooseIntent, PICK_PDF_RESULT_CODE)
                } catch (e: ActivityNotFoundException) {
                    binding.coordinator.showSnackbar(R.string.no_file_explorer)
                }
            } else {
                onPdfRemoved()
            }
        }
    }

    private fun onBottleAdditionalInfoSubmited() {

    }

    private fun requestMediaPersistentPermission(fileBrowserIntent: Intent?) {
        if (fileBrowserIntent != null) {
            val flags = (fileBrowserIntent.flags
                    and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))

            fileBrowserIntent.data?.let {
                activity?.contentResolver?.takePersistableUriPermission(it, flags)
            }
        } else {
            binding.coordinator.showSnackbar(R.string.base_error)
        }
    }

    private fun onPdfRemoved() {
        bottlePdfPath = null
        binding.buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pdf)
        binding.buttonAddPdf.text = resources.getString(R.string.add_pdf)
    }

    private fun onPdfSelected(data: Intent?) {
        requestMediaPersistentPermission(data)
        bottlePdfPath = data?.data.toString()
        binding.buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
        binding.buttonAddPdf.text = resources.getString(R.string.remove_pdf)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PDF_RESULT_CODE) onPdfSelected(data)
        }
    }
}