package com.louis.app.cavity.ui.addbottle

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentInquireOtherInfoBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.model.Chipable
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.addbottle.stepper.Stepper
import com.louis.app.cavity.ui.addbottle.viewmodel.OtherInfoManager
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.showSnackbar
import com.louis.app.cavity.util.toBoolean

class FragmentInquireOtherInfo : Fragment(R.layout.fragment_inquire_other_info) {
    private lateinit var stepperx: Stepper
    private lateinit var otherInfoManager: OtherInfoManager
    private var _binding: FragmentInquireOtherInfoBinding? = null
    private val binding get() = _binding!!
    private val addBottleViewModel: AddBottleViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    companion object {
        const val PICK_PDF_RESULT_CODE = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInquireOtherInfoBinding.bind(view)

        stepperx = parentFragment as Stepper
        otherInfoManager = addBottleViewModel.otherInfoManager

        setListeners()
        observe()
        initFriendsChips()
    }

    private fun setListeners() {
        binding.buttonAddPdf.setOnClickListener {
            if (!otherInfoManager.hasPdf) {
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
                }
            }

            stepper.next.setOnClickListener { stepperx.requestNextPage() }
            stepper.previous.setOnClickListener { stepperx.requestPreviousPage() }

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

            allFriends.addAll(it)
            val toInflate = allFriends - alreadyInflated
            alreadyInflated.addAll(toInflate)

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .load(toInflate.toMutableList())
                .into(binding.friendChipGroup)
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
        otherInfoManager.setPdfPath("")
        binding.buttonAddPdf.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_pdf)
        binding.buttonAddPdf.text = resources.getString(R.string.add_pdf)
    }

    private fun onPdfSelected(intent: Intent?) {
        requestMediaPersistentPermission(intent)

        if (intent != null) {
            otherInfoManager.setPdfPath(intent.data.toString())
            binding.buttonAddPdf.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
            binding.buttonAddPdf.text = resources.getString(R.string.remove_pdf)
        } else {
            binding.coordinator.showSnackbar(R.string.base_error)
        }
    }

    private fun showAddFriendDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_friend,
            hint = R.string.add_friend_label,
            icon = R.drawable.ic_person,
        ) {
            otherInfoManager.insertFriend(it)
        }

        SimpleInputDialog(requireContext(), layoutInflater).show(dialogResources)
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
