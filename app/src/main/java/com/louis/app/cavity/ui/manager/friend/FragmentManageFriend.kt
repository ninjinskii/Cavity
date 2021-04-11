package com.louis.app.cavity.ui.manager.friend

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentManageBaseBinding
import com.louis.app.cavity.model.Friend
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.ui.addwine.FragmentAddWine
import com.louis.app.cavity.ui.manager.ManagerViewModel
import com.louis.app.cavity.util.showSnackbar

class FragmentManageFriend : Fragment(R.layout.fragment_manage_base) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    companion object {
        const val PICK_IMAGE_RESULT_CODE = 1
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

        val fileChooseIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }

        try {
            startActivityForResult(fileChooseIntent, FragmentAddWine.PICK_IMAGE_RESULT_CODE)
        } catch (e: ActivityNotFoundException) {
            binding.coordinator.showSnackbar(R.string.no_file_explorer)
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
            managerViewModel.setImageForCurrentFriend(imagePath)
        } else {
            binding.coordinator.showSnackbar(R.string.base_error)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FragmentAddWine.PICK_IMAGE_RESULT_CODE) onImageSelected(data)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class InsetDivider(
    @Px private val inset: Int,
    @Px private val height: Int,
    @ColorInt private val dividerColor: Int
) : RecyclerView.ItemDecoration() {

    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dividerColor
        style = Paint.Style.STROKE
        strokeWidth = height.toFloat()
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val points = mutableListOf<Float>()
        parent.forEach {
            if (parent.getChildAdapterPosition(it) < state.itemCount - 1) {
                val bottom = it.bottom.toFloat()
                points.add((it.left + inset).toFloat())
                points.add(bottom)
                points.add(it.right.toFloat())
                points.add(bottom)
            }
        }
        c.drawLines(points.toFloatArray(), dividerPaint)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = height / 2
        outRect.bottom = height / 2
    }
}
