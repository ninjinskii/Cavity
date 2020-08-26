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
import com.louis.app.cavity.util.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var binding: FragmentAddWineBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var wineImagePath: String? = null
    private var bottlePdfPath: String? = null

    companion object {
        const val PICK_IMAGE_RESULT_CODE = 1
        const val PICK_PDF_RESULT_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddWineBinding.bind(view)

        loadCounties()
        setListeners()
    }

    private fun loadCounties() {
        val allCounties = mutableSetOf<County>()
        val alreadyInflated = mutableSetOf<County>()

        homeViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

            allCounties.addAll(it)
            val toInflate = allCounties - alreadyInflated

            lifecycleScope.launch(Default) {
                for ((index, county) in toInflate.withIndex()) {
                    val chip: Chip =
                        layoutInflater.inflate(
                            R.layout.chip_choice,
                            binding.countyChipGroup,
                            false
                        ) as Chip
                    chip.apply {
                        setTag(R.string.tag_chip_id, county)
                        text = county.name
                    }

                    withContext(Main) {
                        binding.countyChipGroup.addView(chip)
                        if (index == 1) chip.isChecked = true
                    }
                }

                alreadyInflated.addAll(toInflate)
            }
        }
    }

    private fun setListeners() {
        binding.submitAddWine.setOnClickListener {
            with(binding) {
                if (countyChipGroup.checkedChipId == NO_ID) {
                    coordinator.showSnackbar(R.string.no_county)
                } else {
                    val name = name.text.toString()
                    val naming = naming.text.toString()
                    val isOrganic = organicWine.isChecked.toInt()
                    val color = colorChipGroup.checkedChipId

                    Wine(
                        0,
                        name,
                        naming,
                        getWineColor(color),
                        0,
                        isOrganic,
                        wineImagePath ?: ""
                    ).also {
                        homeViewModel.addWine(it)
                    }
                }
            }
        }

        binding.buttonAddCounty.setOnClickListener {
            showDialog(it)
        }

        binding.buttonAddCountyIfEmpty.setOnClickListener {
            showDialog(it)
        }

        binding.buttonAddPhoto.setOnClickListener {
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

        binding.buttonRemoveWineImage.setOnClickListener {
            with(binding) {
                wineMiniImage.setVisible(false)
                buttonRemoveWineImage.setVisible(false)
                buttonAddPhoto.setVisible(true)
                wineImagePath = null
            }
        }
    }

    private fun showDialog(view: View) {
        val dialogBinding = DialogAddCountyBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this.requireContext())
            .setTitle(resources.getString(R.string.add_county))
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
            }
            .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                homeViewModel.addCounty(dialogBinding.countyName.text.toString())
            }
            .setView(dialogBinding.root)
            .show()

        view.postDelayed({ context?.showKeyboard(dialogBinding.countyName) }, 100)
    }

    // enum ?
    private fun getWineColor(chipId: Int): Int {
        L.v(chipId.toString())
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

        with(binding) {
            wineMiniImage.setVisible(true)
            context?.let {
                Glide.with(it)
                    .load(Uri.parse(wineImagePath))
                    .centerCrop()
                    .into(wineMiniImage)
            }

            buttonRemoveWineImage.setVisible(true)
            buttonAddPhoto.setVisible(false)
        }
    }

    // TODO: Move
    private fun onPdfSelected(data: Intent?) {
        requestMediaPersistentPermission(data)
        bottlePdfPath = data?.data.toString()
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
