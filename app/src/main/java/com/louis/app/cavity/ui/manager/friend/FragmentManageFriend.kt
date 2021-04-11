package com.louis.app.cavity.ui.manager.friend

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
import com.louis.app.cavity.ui.manager.ManagerViewModel

class FragmentManageFriend : Fragment(R.layout.fragment_manage_base) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManageBaseBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManageBaseBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val friendAdapter = FriendRecyclerAdapter(
            onRename = { friend: Friend -> showEditFriendDialog(friend) },
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
