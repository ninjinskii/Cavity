package com.louis.app.cavity.ui.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.NO_ID
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddCountyBinding
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.ui.CountyLoader
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentAddWine : Fragment(R.layout.fragment_add_wine), CountyLoader {
    private lateinit var binding: FragmentAddWineBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var wineImagePath: String? = null
    private var bottlePdfPath: String? = null
    private var editMode: Boolean = false

    companion object {
        const val PICK_IMAGE_RESULT_CODE = 1
        const val PICK_PDF_RESULT_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddWineBinding.bind(view)
        editMode = homeViewModel.editWine != null

        inflateChips()
        setListeners()
        updateFields()
    }

    private fun inflateChips() {
        val allCounties = mutableSetOf<County>()
        val alreadyInflated = mutableSetOf<County>()

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

            allCounties.addAll(it)
            val toInflate = allCounties - alreadyInflated
            alreadyInflated.addAll(toInflate)

            loadCounties(
                lifecycleScope,
                layoutInflater,
                binding.countyChipGroup,
                toInflate,
                homeViewModel.editWine
            )
        }
    }

    private fun setListeners() {
        binding.submitAddWine.setOnClickListener {
            with(binding) {
                val name = name.text.toString()
                val naming = naming.text.toString()
                val cuvee = cuvee.text.toString()
                val isOrganic = organicWine.isChecked.toInt()
                val color = colorChipGroup.checkedChipId
                val checkedChipId = countyChipGroup.checkedChipId

                if (countyChipGroup.checkedChipId == NO_ID) {
                    coordinator.showSnackbar(R.string.no_county)
                    nestedScrollView.smoothScrollTo(0, 0)
                } else if (name.isBlank() || naming.isBlank()) {
                    coordinator.showSnackbar(R.string.empty_name_or_naming)
                    if (name.isBlank()) nameLayout.error = getString(R.string.required_field)
                    if (naming.isBlank()) namingLayout.error = getString(R.string.required_field)
                } else {
                    nameLayout.error = null
                    namingLayout.error = null

                    val county = countyChipGroup
                        .findViewById<Chip>(checkedChipId)
                        .getTag(R.string.tag_chip_id) as County

                    val wine = Wine(
                        0,
                        name,
                        naming,
                        getWineColor(color),
                        cuvee,
                        county.idCounty,
                        isOrganic,
                        wineImagePath ?: ""
                    )

                    if (!editMode) {
                        homeViewModel.addWine(wine)
                    } else {
                        wine.apply { idWine = homeViewModel.editWine!!.idWine }
                            .also { homeViewModel.updateWine(wine) }
                    }
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
            wineImagePath = null
        }
    }

    private fun showDialog() {
        val dialogBinding = DialogAddCountyBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.add_county))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                homeViewModel.addCounty(dialogBinding.countyName.text.toString())
            }
            .setView(dialogBinding.root)
            .show()

        lifecycleScope.launch(Main) {
            delay(300)
            dialogBinding.countyName.showKeyboard()
        }
    }

    private fun updateFields() {
        val wineToEdit = homeViewModel.editWine

        if (editMode && wineToEdit != null) {
            with(binding) {
                naming.setText(wineToEdit.naming)
                name.setText(wineToEdit.name)
                cuvee.setText(wineToEdit.cuvee)
                (colorChipGroup.getChildAt(wineToEdit.color) as Chip).isChecked = true
                organicWine.isChecked = wineToEdit.isOrganic.toBoolean()
                wineImagePath = wineToEdit.imgPath
                loadImage(wineToEdit.imgPath)
            }
        }
    }

    // enum ?
    private fun getWineColor(chipId: Int): Int {
        return when (chipId) {
            R.id.colorWhite -> 0
            R.id.colorRed -> 1
            R.id.colorSweet -> 2
            else -> 3
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
        requestMediaPersistentPermission(data)
        wineImagePath = data?.data.toString()
        binding.wineMiniImage.setVisible(true)
        loadImage(wineImagePath)
    }

    // TODO: Move
    private fun onPdfSelected(data: Intent?) {
        requestMediaPersistentPermission(data)
        bottlePdfPath = data?.data.toString()
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
            when (requestCode) {
                PICK_IMAGE_RESULT_CODE -> onImageSelected(data)
                PICK_PDF_RESULT_CODE -> onPdfSelected(data) // TODO: Move
            }
        }
    }
}
