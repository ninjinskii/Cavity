package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddReviewBinding
import com.louis.app.cavity.databinding.FragmentManagerBinding
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.util.*

class FragmentManager : Fragment(R.layout.fragment_manager) {
    private lateinit var simpleInputDialog: SimpleInputDialog
    private var _binding: FragmentManagerBinding? = null
    private val binding get() = _binding!!
    private val managerViewModel: ManagerViewModel by viewModels()
    private val addItemViewModel: AddItemViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = false)
            setFadeThrough(navigatingForward = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        _binding = FragmentManagerBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater, viewLifecycleOwner)

        setupNavigation(binding.toolbar)

        setupWithViewPager()
        observe()
        setListener()
    }

    private fun setupWithViewPager() {
        binding.viewPager.adapter = ManagerPagerAdapter(this@FragmentManager)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.counties)
                1 -> tab.text = getString(R.string.grapes)
                2 -> tab.text = getString(R.string.expert_advice)
                3 -> tab.text = getString(R.string.friends)
            }
        }.attach()
    }

    private fun observe() {
        managerViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
            }
        }
    }

    private fun setListener() {
        binding.fab.setOnClickListener {
            when (binding.viewPager.currentItem) {
                0 -> showAddCountyDialog()
                1 -> showAddGrapeDialog()
                2 -> showAddReviewDialog()
                3 -> showAddFriendDialog()
            }
        }
    }

    private fun getReviewType(@IdRes button: Int) = when (button) {
        R.id.rbMedal -> 0
        R.id.rbRate20 -> 1
        R.id.rbRate100 -> 2
        else -> 3
    }

    fun showAddCountyDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_county,
            hint = R.string.county
        ) {
            addItemViewModel.insertCounty(it)
        }

        simpleInputDialog.show(dialogResources)
    }

    fun showAddGrapeDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_grape,
            hint = R.string.grape_name,
            icon = R.drawable.ic_grape
        ) {
            addItemViewModel.insertGrape(it)
        }

        simpleInputDialog.show(dialogResources)
    }

    fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)

        LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
            .setTitle(R.string.add_review)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.contestName.text.toString().trim()
                val type = getReviewType(dialogBinding.rbGroupType.checkedButtonId)

                addItemViewModel.insertReview(name, type)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.contestName.post { dialogBinding.contestName.showKeyboard() }
        dialogBinding.rbMedal.performClick()
    }

    fun showAddFriendDialog() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
