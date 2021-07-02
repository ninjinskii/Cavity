package com.louis.app.cavity.ui.manager.friend

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.ActivityMain
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentManageFriend : Fragment(R.layout.fragment_manage_base) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val pickImage by lazy {
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri ->
            onImageSelected(imageUri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val friendAdapter = FriendRecyclerAdapter(
            onRename = { friend: Friend -> showEditFriendDialog(friend) },
            onChangeImage = { friend: Friend -> onChangeImage(friend) },
            onDelete = { friend: Friend -> showConfirmDeleteDialog(friend) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = friendAdapter

            val inset =
                resources.getDimensionPixelSize(R.dimen.medium_margin) * 2 +
                    resources.getDimensionPixelSize(R.dimen.large_icon)

            val height = resources.getDimensionPixelSize(R.dimen.divider_height)
            val color = ContextCompat.getColor(requireContext(), R.color.divider_color)

            addItemDecoration(InsetDivider(inset, height, color))
        }

        managerViewModel.getAllFriends().observe(viewLifecycleOwner) {
            friendAdapter.submitList(it)
        }
    }

    private fun showEditFriendDialog(friend: Friend) {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.rename_friend,
            hint = R.string.add_friend_label,
            icon = R.drawable.ic_person
        ) {
            managerViewModel.updateFriend(friend, it)
        }

        simpleInputDialog.show(dialogResources)
    }

    private fun showConfirmDeleteDialog(friend: Friend) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_grape_delete)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                managerViewModel.deleteFriend(friend)
            }
            .show()
    }

    private fun onChangeImage(friend: Friend) {
        managerViewModel.friendPickingImage = friend

        try {
            pickImage.launch(arrayOf("image/*"))
        } catch (e: ActivityNotFoundException) {
            binding.coordinator.showSnackbar(R.string.no_file_explorer)
        }
    }

    private fun onImageSelected(imageUri: Uri?) {
        if (imageUri == null) {
            binding.coordinator.showSnackbar(R.string.base_error)
            return
        }

        (activity as ActivityMain).requestMediaPersistentPermission(imageUri)

        managerViewModel.setImageForCurrentFriend(imageUri.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
