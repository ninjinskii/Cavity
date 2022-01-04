package com.louis.app.cavity.ui.addwine

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.util.*

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var snackbarProvider: SnackbarProvider
    private lateinit var pickImage: ActivityResultLauncher<Array<String>>
    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentAddWineBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val addWineViewModel: AddWineViewModel by viewModels()
    private val args: FragmentAddWineArgs by navArgs()

    companion object {
        const val TAKEN_PHOTO_URI = "com.louis.app.cavity.ui.TAKEN_PHOTO_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transitionHelper = TransitionHelper(this).apply {
            setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = false)
            setFadeThrough(navigatingForward = true)
            setContainerTransformTransition(options = null, enter = true) // Appbar
        }

        pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri ->
            onImageSelected(imageUri)
        }

        if (savedInstanceState == null) {
            addWineViewModel.start(args.editedWineId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

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
        addWineViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

            val newCountyAdded = binding.countyChipGroup.childCount == it.size - 1

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(it)
                .into(binding.countyChipGroup)
                .preselect(if (newCountyAdded) it.last().id else args.countyId)
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
            try {
                pickImage.launch(arrayOf("image/*"))
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
            addItemViewModel.insertCounty(it.trim())
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
    }

    private fun observe() {
        addWineViewModel.updatedWine.observe(viewLifecycleOwner) {
            with(binding) {
                naming.setText(it.naming)
                name.setText(it.name)
                cuvee.setText(it.cuvee)
                (colorChipGroup.getChildAt(it.color.ordinal) as Chip).isChecked = true
                organicWine.isChecked = it.isOrganic.toBoolean()
            }
        }

        addWineViewModel.image.observe(viewLifecycleOwner) {
            loadImage(it)
        }

        addWineViewModel.wineUpdatedEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                findNavController().navigateUp()
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }

        addWineViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                snackbarProvider.onShowSnackbarRequested(stringRes)
            }
        }

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>(TAKEN_PHOTO_URI)
            ?.observe(viewLifecycleOwner) { addWineViewModel.setImage(it) }
    }

    private fun onImageSelected(imageUri: Uri?) {
        if (imageUri == null) {
            binding.coordinator.showSnackbar(R.string.base_error)
            return
        }

        (activity as ActivityMain).requestMediaPersistentPermission(imageUri)

        addWineViewModel.setImage(imageUri.toString())
        binding.wineMiniImage.setVisible(true)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
