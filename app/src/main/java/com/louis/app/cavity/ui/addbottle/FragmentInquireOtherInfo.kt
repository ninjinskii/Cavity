package com.louis.app.cavity.ui.addbottle

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Checkable
import android.widget.CompoundButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireOtherInfoBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.BottleSize
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.viewmodel.OtherInfoManager
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.ui.widget.friendpicker.FriendPickerBottomSheet
import com.louis.app.cavity.ui.widget.friendpicker.FriendPickerView
import com.louis.app.cavity.ui.widget.friendpicker.FriendPickerViewModel
import com.louis.app.cavity.util.*

class FragmentInquireOtherInfo : Step(R.layout.fragment_inquire_other_info) {
    private lateinit var otherInfoManager: OtherInfoManager
    private lateinit var pickPdf: ActivityResultLauncher<Array<String>>
    private var _binding: FragmentInquireOtherInfoBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private val friendPickerViewModel: FriendPickerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var lockBottomSheet = true
    private var onGiftedByCheckedChange = { binding: FragmentInquireOtherInfoBinding ->
        { _: CompoundButton, isChecked: Boolean ->
            with(binding) {
                buttonAddFriend.setVisible(isChecked)
                friendPicker.setVisible(isChecked)

                if (isChecked) {
                    showPickFriendDialog()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickPdf = registerForActivityResult(ActivityResultContracts.OpenDocument()) { pdfUri ->
            onPdfSelected(pdfUri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireOtherInfoBinding.bind(view)

        otherInfoManager = addBottleViewModel.otherInfoManager

        binding.apply {
            val storageLocationEnabled = settingsViewModel.getEnableBottleStorageLocation()
            storageLocationLayout.setVisible(storageLocationEnabled)
            autoAnimate.layoutTransition.setAnimateParentHierarchy(false)
            rbNormal.isChecked = true
        }

        applyInsets()
        setListeners()
        observe()
    }

    private fun applyInsets() {
        binding.nestedScrollView.prepareWindowInsets { view, _, _, _, _, bottom ->
            view.updatePadding(bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setListeners() {
        binding.buttonAddPdf.setOnClickListener {
            if (!otherInfoManager.hasPdf) {
                try {
                    pickPdf.launch(arrayOf("application/pdf"))
                } catch (_: ActivityNotFoundException) {
                    binding.coordinator.showSnackbar(R.string.no_file_explorer)
                }
            } else {
                onPdfRemoved()
            }
        }

        binding.buttonAddFriend.setOnClickListener { showAddFriendDialog() }

        binding.giftedBy.setOnCheckedChangeListener(onGiftedByCheckedChange(binding))

        binding.friendPicker.setConfig(
            FriendPickerView.FriendPickerConfig(
                onRootViewClick = { showPickFriendDialog() },
                onFriendCloseIconClicked = { friendPickerViewModel.updateFriendStatus(it) },
                onFriendChipClicked = { showPickFriendDialog() }
            )
        )
    }

    private fun observe() {
        addBottleViewModel.editedBottle.observe(viewLifecycleOwner) {
            if (it != null) {
                updateFields(it)
            }
        }

        addBottleViewModel.editedBottleHistoryEntry.observe(viewLifecycleOwner) { entry ->
            with(binding) {
                entry?.let {
                    val isAGift = entry.friends.isNotEmpty()
                    silentGivenBySetChecked(isAGift)
                    buttonAddFriend.setVisible(isAGift)
                    friendPicker.setVisible(isAGift)
                }
            }

            lockBottomSheet = false
        }

        friendPickerViewModel.getAllFriends().observe(viewLifecycleOwner) {
            binding.friendPicker.setFriends(it)
            lockBottomSheet = false
        }

        friendPickerViewModel.selectedFriends.observe(viewLifecycleOwner) {
            binding.friendPicker.setSelectedFriends(it)
        }
    }

    private fun silentGivenBySetChecked(checked: Boolean) {
        with(binding) {
            giftedBy.setOnCheckedChangeListener(null)
            giftedBy.isChecked = checked
            giftedBy.setOnCheckedChangeListener(onGiftedByCheckedChange(binding))
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        with(binding) {
            storageLocation.setText(editedBottle.storageLocation)
            alcohol.setText(editedBottle.alcohol?.toString() ?: "")
            otherInfo.setText(editedBottle.otherInfo)
            addToFavorite.isChecked = editedBottle.isFavorite.toBoolean()
            otherInfoManager.setPdfPath(editedBottle.pdfPath)

            val checkedButtonPos = when (editedBottle.bottleSize) {
                BottleSize.SLIM -> 0
                BottleSize.SMALL -> 1
                BottleSize.NORMAL -> 2
                BottleSize.MAGNUM -> 3
            }

            (binding.rbGroupSize.getChildAt(checkedButtonPos) as Checkable).isChecked = true

            if (otherInfoManager.hasPdf) {
                buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                buttonAddPdf.text = resources.getString(R.string.remove_pdf)
            }
        }
    }

    private fun onPdfRemoved() {
        otherInfoManager.setPdfPath("")
        binding.buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pdf)
        binding.buttonAddPdf.text = resources.getString(R.string.add_pdf)
    }

    private fun onPdfSelected(pdfUri: Uri?) {
        if (pdfUri == null) {
            binding.coordinator.showSnackbar(R.string.base_error)
            return
        }

        (activity as ActivityMain).requestMediaPersistentPermission(pdfUri)

        otherInfoManager.setPdfPath(pdfUri.toString())
        binding.buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
        binding.buttonAddPdf.text = getString(R.string.remove_pdf)
    }

    private fun showAddFriendDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_friend,
            hint = R.string.add_friend_label,
            icon = R.drawable.ic_person,
        ) {
            addItemViewModel.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)
            .show(dialogResources)
    }

    private fun showPickFriendDialog() {
        if (lockBottomSheet) {
            return
        }

        FriendPickerBottomSheet().show(
            parentFragmentManager,
            getString(R.string.tag_friend_picker_modal_sheet)
        )
    }

    override fun requestNextPage(): Boolean {
        val friends =
            if (binding.giftedBy.isChecked) friendPickerViewModel.getSelectedFriendsIds()
            else emptyList()

        with(binding) {
            otherInfoManager.submitOtherInfo(
                storageLocation.text.toString().trim(),
                alcohol.text.toString().toDoubleOrNull(),
                otherInfo.text.toString(),
                rbGroupSize.checkedButtonId,
                addToFavorite.isChecked,
                friends
            )

            addBottleViewModel.submitBottleForm()
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.buttonAddFriend.setOnClickListener(null)
        binding.giftedBy.setOnCheckedChangeListener(null)
        _binding = null
    }
}
