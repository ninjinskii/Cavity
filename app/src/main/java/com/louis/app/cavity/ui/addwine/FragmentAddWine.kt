package com.louis.app.cavity.ui.addwine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.util.*

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentAddWineBinding? = null
    private val binding get() = _binding!!
    private val addWineViewModel: AddWineViewModel by viewModels()
    private val args: FragmentAddWineArgs by navArgs()

    companion object {
        const val PICK_IMAGE_RESULT_CODE = 1
        const val TAKEN_PHOTO_URI = "com.louis.app.cavity.ui.TAKEN_PHOTO_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            addWineViewModel.start(args.editedWineId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddWineBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)
        snackbarProvider = activity as SnackbarProvider

        addWineViewModel.setCountyId(args.countyId)

        inflateChips()
        initDropdown()
        setListeners()
        observe()
    }

    private fun inflateChips() {
        val allCounties = mutableSetOf<County>()
        val alreadyInflated = mutableSetOf<County>()

        addWineViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

//            allCounties.addAll(it)
//            val toInflate = allCounties - alreadyInflated
//            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(it)
                .into(binding.countyChipGroup)
                .preselect(args.countyId)
                .doOnClick { v -> setCounty(v) }
                .build()
                .go()
        }
    }

    private fun initDropdown() {
        val adapter = ArrayAdapter<String>(requireContext(), R.layout.item_naming)

        binding.naming.setAdapter(adapter)

        addWineViewModel.namings.observe(viewLifecycleOwner) {
            adapter.clear()
            adapter.addAll(it)
        }
    }

    private fun setListeners() {
        binding.submitAddWine.setOnClickListener {
            with(binding) {
                root.hideKeyboard()

                val valid = namingLayout.validate() and nameLayout.validate()

                if (valid) {
                    val name = name.text.toString().trim()
                    val naming = naming.text.toString().trim()
                    val cuvee = cuvee.text.toString().trim()
                    val isOrganic = organicWine.isChecked.toInt()
                    val color = colorChipGroup.checkedChipId
                    val checkedCountyChipId = countyChipGroup.checkedChipId

                    val county = countyChipGroup
                        .findViewById<Chip>(checkedCountyChipId)
                        ?.getTag(R.string.tag_chip_id) as County?

                    addWineViewModel.saveWine(name, naming, cuvee, isOrganic, color, county)
                }
            }
        }

        binding.buttonAddCounty.setOnClickListener {
            showCountyDialog()
        }

        binding.buttonAddCountyIfEmpty.setOnClickListener {
            showCountyDialog()
        }

        binding.buttonBrowsePhoto.setOnClickListener {
            val fileChooseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
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
            val action = FragmentAddWineDirections.addWineToCamera()
            findNavController().navigate(action)
        }

        binding.buttonRemoveWineImage.setOnClickListener {
            toggleImageViews(false)
            addWineViewModel.setImage("")
        }
    }

    private fun showCountyDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_county,
            hint = R.string.county
        ) {
            addWineViewModel.insertCounty(it.trim())
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
    }

    private fun observe() {
        addWineViewModel.updatedWine.observe(viewLifecycleOwner) {
            with(binding) {
                naming.setText(it.naming)
                name.setText(it.name)
                cuvee.setText(it.cuvee)
                (colorChipGroup.getChildAt(it.color) as Chip).isChecked = true
                organicWine.isChecked = it.isOrganic.toBoolean()
            }
        }

        addWineViewModel.image.observe(viewLifecycleOwner) {
            loadImage(it)
        }

        addWineViewModel.wineUpdatedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes, useAnchorView = true)
                findNavController().navigateUp()
            }
        }

        addWineViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes, useAnchorView = false)
            }
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(TAKEN_PHOTO_URI)
            ?.observe(viewLifecycleOwner) { addWineViewModel.setImage(it) }
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
            binding.wineMiniImage.setVisible(true)
        } else {
            snackbarProvider.onShowSnackbarRequested(R.string.base_error, useAnchorView = false)
        }
    }

    private fun loadImage(uri: String?) {
        if (!uri.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(Uri.parse(uri))
                .centerCrop()
                .into(binding.wineMiniImage)

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

    private fun setCounty(view: View) {
        val county = view.getTag(R.string.tag_chip_id) as County?
        addWineViewModel.setCountyId(county?.id)
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
