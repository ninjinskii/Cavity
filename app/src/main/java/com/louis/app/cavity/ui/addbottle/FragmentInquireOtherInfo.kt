package com.louis.app.cavity.ui.addbottle

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireOtherInfoBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Chipable
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.addbottle.viewmodel.AddBottleViewModel
import com.louis.app.cavity.ui.addbottle.viewmodel.OtherInfoManager
import com.louis.app.cavity.ui.manager.AddItemViewModel
import com.louis.app.cavity.ui.stepper.Step
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar
import com.louis.app.cavity.util.toBoolean

class FragmentInquireOtherInfo : Step(R.layout.fragment_inquire_other_info) {
    private lateinit var otherInfoManager: OtherInfoManager
    private var _binding: FragmentInquireOtherInfoBinding? = null
    private val binding get() = _binding!!
    private val addItemViewModel: AddItemViewModel by activityViewModels()
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val pickPdf by lazy {
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { pdfUri ->
            onPdfSelected(pdfUri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireOtherInfoBinding.bind(view)

        otherInfoManager = addBottleViewModel.otherInfoManager

        setListeners()
        observe()
        initFriendsChips()
    }

    private fun setListeners() {
        binding.buttonAddPdf.setOnClickListener {
            if (!otherInfoManager.hasPdf) {
                try {
                    pickPdf.launch(arrayOf("application/pdf"))
                } catch (e: ActivityNotFoundException) {
                    binding.coordinator.showSnackbar(R.string.no_file_explorer)
                }
            } else {
                onPdfRemoved()
            }
        }

        with(binding) {
            submitAddBottle.setOnClickListener {
                friendChipGroup.apply {
                    val friend =
                        if (giftedBy.isChecked) (findViewById<Chip>(checkedChipId).getTag(R.string.tag_chip_id) as Chipable).getItemId() else null

                    otherInfoManager.submitOtherInfo(
                        otherInfo.text.toString(),
                        addToFavorite.isChecked,
                        friend
                    )

                    addBottleViewModel.insertBottle()
                }
            }

            stepper.next.setOnClickListener { stepperFragment.requestNextPage() }
            stepper.previous.setOnClickListener { stepperFragment.requestPreviousPage() }

            giftedBy.setOnCheckedChangeListener { _, isChecked ->
                friendScrollView.setVisible(isChecked)
            }

            buttonAddFriendIfEmpty.setOnClickListener { showAddFriendDialog() }
        }

    }

    private fun observe() {
        addBottleViewModel.editedBottle.observe(viewLifecycleOwner) {
            if (it != null) updateFields(it)
        }
    }

    private fun initFriendsChips() {
        val allFriends = mutableSetOf<Friend>()
        val alreadyInflated = mutableSetOf<Friend>()

        otherInfoManager.getAllFriends().observe(viewLifecycleOwner) {
            binding.buttonAddFriendIfEmpty.setVisible(it.isEmpty())

//            allFriends.addAll(it)
//            val toInflate = allFriends - alreadyInflated
//            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_friend_entry)
                .load(it)
                .into(binding.friendChipGroup)
                .useAvatar(true)
                .build()
                .go()
        }
    }

    private fun updateFields(editedBottle: Bottle) {
        with(binding) {
            otherInfo.setText(editedBottle.otherInfo)
            addToFavorite.isChecked = editedBottle.isFavorite.toBoolean()
            otherInfoManager.setPdfPath(editedBottle.pdfPath)

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

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
