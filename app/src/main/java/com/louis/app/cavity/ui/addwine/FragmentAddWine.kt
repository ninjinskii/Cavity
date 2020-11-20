package com.louis.app.cavity.ui.addwine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddCountyGrapeBinding
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.widget.Rule
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentAddWineBinding? = null
    private val binding get() = _binding!!
    private val addWineViewModel: AddWineViewModel by viewModels()
    private val args: FragmentAddWineArgs by navArgs()

    companion object {
        const val PICK_IMAGE_RESULT_CODE = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddWineBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)
        snackbarProvider = activity as SnackbarProvider

        addWineViewModel.start(args.editedWineId)

        inflateChips()
        setListeners()
        observe()
    }

    private fun inflateChips() {
        val allCounties = mutableSetOf<County>()
        val alreadyInflated = mutableSetOf<County>()

        addWineViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

            allCounties.addAll(it)
            val toInflate = allCounties - alreadyInflated
            alreadyInflated.addAll(toInflate)

            CountyLoader().loadCounties(
                lifecycleScope,
                layoutInflater,
                binding.countyChipGroup,
                toInflate,
                preselect = listOf(args.countyId)
            )
        }
    }

    private fun setListeners() {
        binding.submitAddWine.setOnClickListener {
            with(binding) {
                root.hideKeyboard()

                val valid = nameLayout.validate() and namingLayout.validate()

                if (valid) {
                    val name = name.text.toString().trim()
                    val naming = naming.text.toString().trim()
                    val cuvee = cuvee.text.toString().trim()
                    val isOrganic = organicWine.isChecked.toInt()
                    val color = colorChipGroup.checkedChipId
                    val checkedChipId = countyChipGroup.checkedChipId

                    if (countyChipGroup.checkedChipId == View.NO_ID) {
                        coordinator.showSnackbar(R.string.no_county)
                        nestedScrollView.smoothScrollTo(0, 0)
                    }

                    val county = countyChipGroup
                        .findViewById<Chip>(checkedChipId)
                        .getTag(R.string.tag_chip_id) as County

                    addWineViewModel.saveWine(
                        name,
                        naming,
                        cuvee,
                        isOrganic,
                        color,
                        county
                    )
                }
            }
        }

        binding.buttonAddCounty.setOnClickListener {
            showDialog()
        }

        binding.buttonAddCountyIfEmpty.setOnClickListener {
            showDialog()
        }

        binding.buttonBrowsePhoto.setOnClickListener {
            val fileChooseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            fileChooseIntent.apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }

            try {
                startActivityForResult(fileChooseIntent, PICK_IMAGE_RESULT_CODE)
            } catch (e: ActivityNotFoundException) {
                binding.coordinator.showSnackbar(R.string.no_file_explorer)
            }
        }

        binding.buttonTakePhoto.setOnClickListener {
            // Start camera activity
        }

        binding.buttonRemoveWineImage.setOnClickListener {
            toggleImageViews(false)
            addWineViewModel.setImage("")
        }
    }

    private fun showDialog() {
        val dialogBinding = DialogAddCountyGrapeBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_county)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                addWineViewModel.addCounty(dialogBinding.countyName.text.toString().trim())
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.countyName.post { dialogBinding.countyName.showKeyboard() }
    }

    private fun observe() {
        addWineViewModel.updatedWine.observe(viewLifecycleOwner) {
            with(binding) {
                naming.setText(it.naming)
                name.setText(it.name)
                cuvee.setText(it.cuvee)
                (colorChipGroup.getChildAt(it.color) as Chip).isChecked = true
                organicWine.isChecked = it.isOrganic.toBoolean()
                loadImage(it.imgPath)
            }
        }

        addWineViewModel.wineUpdatedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
                findNavController().navigateUp()
            }
        }

        addWineViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
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

    private fun onImageSelected(data: Intent?) {
        if (data != null) {
            val imagePath = data.data.toString()
            requestMediaPersistentPermission(data)
            addWineViewModel.setImage(imagePath)
            loadImage(imagePath)
            binding.wineMiniImage.setVisible(true)
        } else {
            snackbarProvider.onShowSnackbarRequested(R.string.base_error)
        }
    }

    private fun loadImage(uri: String?) {
        if (!uri.isNullOrEmpty()) {
            context?.let {
                Glide.with(it)
                    .load(Uri.parse(uri))
                    .centerCrop()
                    .into(binding.wineMiniImage)
            }

            toggleImageViews(true)
        } else {
            toggleImageViews(false)
        }
    }

    private fun toggleImageViews(hasImage: Boolean) {
        with(binding) {
            buttonRemoveWineImage.setVisible(hasImage)
            wineMiniImage.setVisible(hasImage)
            buttonBrowsePhoto.setVisible(!hasImage)
            buttonTakePhoto.setVisible(!hasImage)
            textButtonTakePhoto.setVisible(!hasImage)
            textButtonBrowsePhoto.setVisible(!hasImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_RESULT_CODE) onImageSelected(data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
