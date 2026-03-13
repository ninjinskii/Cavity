package com.louis.app.cavity.ui.addwine

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddWineBinding
import com.louis.app.cavity.model.County
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.util.*
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.louis.app.cavity.ui.TransitionFragment
import com.louis.app.cavity.ui.home.FragmentHome.Companion.ADD_WINE_RESULT_KEY
import com.louis.app.cavity.ui.navigation.AddWineRoute
import com.louis.app.cavity.ui.navigation.TransitionSpec
import com.louis.app.cavity.ui.navigation.navigateUp
import com.louis.app.cavity.ui.navigation.fragmentResultListener
import com.louis.app.cavity.ui.navigation.navigate
import com.louis.app.cavity.ui.navigation.putFragmentResult
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class FragmentAddWine : TransitionFragment(R.layout.fragment_add_wine) {
    private lateinit var pickImage: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentAddWineBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val addWineViewModel: AddWineViewModel by viewModels()
    private val args: FragmentAddWineArgs by navArgs()

    companion object {
        const val CAMERA_RESULT_KEY = "com.louis.app.cavity.ui.CAMERA_RESULT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        addWineViewModel.setCountyId(args.countyId)

        if (args.editedWineId != 0L) {
            binding.appBar.toolbar.title = getString(R.string.edit_wine_title)
        }

        L.v("FragmentAddWInes: nav arguments: ${requireArguments().getParcelable<TransitionSpec>("transition-spec")}")
        L.v("FragmentAddWInes: returnTransition: $returnTransition")

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addWineViewModel.event.collect {
                    when (it) {
                        is AddWineEvent.WineChange -> {
                            val result = Result(it.wine.id, it.wine.countyId)
                            putFragmentResult(ADD_WINE_RESULT_KEY, result)
                            navigateUp()
                        }
                    }
                }
            }
        }

        applyInsets()
        listenForCameraResult()
        inflateChips()
        initDropdown()
        setListeners()
        observe()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, top = top, right = right)
            WindowInsetsCompat.CONSUMED
        }

        binding.nestedScrollView.prepareWindowInsets { view, _, left, _, right, bottom ->
            view.updatePadding(left = left, right = right, bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun listenForCameraResult() {
        fragmentResultListener<FragmentCamera.Result>(CAMERA_RESULT_KEY) { result ->
            result?.let {
                addWineViewModel.setImage(it.imageUri)
            }
        }
    }

    private fun inflateChips() {
        addWineViewModel.getAllCounties().observe(viewLifecycleOwner) {
            binding.buttonAddCountyIfEmpty.setVisible(it.isEmpty())

            val newCountyAdded = binding.countyChipGroup.childCount == it.size - 1
            val newCountyId = if (newCountyAdded) it.last().id else 0

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(it)
                .into(binding.countyChipGroup)
                .preselect(if (newCountyAdded) newCountyId else args.countyId)
                .doOnClick { v -> setCounty(v) }
                .build()
                .go()

            if (newCountyAdded) {
                addWineViewModel.setCountyId(newCountyId)
            }
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
            } catch (_: ActivityNotFoundException) {
                binding.coordinator.showSnackbar(R.string.no_file_explorer)
            }
        }

        binding.buttonTakePhoto.setOnClickListener {
            navigate(AddWineRoute.Camera)
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

        SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)
            .show(dialogResources)
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
                .load(uri.toUri())
                .centerCrop()
                .into(binding.wineMiniImage)

            binding.nestedScrollView.run { post { scrollTo(0, height) } }
            toggleImageViews(true)
        } else {
            toggleImageViews(false)
        }
    }

    private fun toggleImageViews(hasImage: Boolean) {
        with(binding) {
            buttonRemoveWineImage.setVisible(hasImage)
            hexagonPreview.setVisible(hasImage)
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

    @Parcelize
    data class Result(val wineId: Long, val countyId: Long) : Parcelable
}
