package com.louis.app.cavity.ui.addbottle.steps

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireOtherInfoBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.addbottle.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.stepper.FragmentStepper
import com.louis.app.cavity.util.showSnackbar
import com.louis.app.cavity.util.toBoolean

class FragmentInquireOtherInfo : Fragment(R.layout.fragment_inquire_other_info) {
    private var _binding: FragmentInquireOtherInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var stepperFragment: FragmentStepper
    private val addBottleViewModel: AddBottleViewModel by activityViewModels()
    private var bottlePdfPath: String? = null

    companion object {
        const val PICK_PDF_RESULT_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireOtherInfoBinding.bind(view)

        registerStepperWatcher()
        setListeners()
        observe()
    }

    private fun registerStepperWatcher() {
        stepperFragment = parentFragmentManager.findFragmentById(R.id.stepper) as FragmentStepper

        stepperFragment.addListener(object : FragmentStepper.StepperWatcher {
            override fun onRequestChangePage() = true

            override fun onPageRequestAccepted() {
            }

            override fun onFinalStepAccomplished() {
                with(binding) {
                    addBottleViewModel.saveBottle(
                        otherInfo.text.toString(),
                        addToFavorite.isChecked,
                        bottlePdfPath ?: ""
                    )
                }

                findNavController().popBackStack()
            }
        })
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

        binding.submitAddBottle.setOnClickListener {
            stepperFragment.accomplished()
        }
    }

    private fun observe() {
        addBottleViewModel.updatedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        with(binding) {
            otherInfo.setText(editedBottle.otherInfo)
            addToFavorite.isChecked = editedBottle.isFavorite.toBoolean()
            bottlePdfPath = editedBottle.pdfPath
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
