package com.louis.app.cavity.ui.manager

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddReviewBinding
import com.louis.app.cavity.databinding.FragmentManagerBinding
import com.louis.app.cavity.ui.SimpleInputDialog
import com.louis.app.cavity.util.hideKeyboard
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showKeyboard
import com.louis.app.cavity.util.showSnackbar

class FragmentManager : Fragment(R.layout.fragment_manager) {
    private var _binding: FragmentManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var simpleInputDialog: SimpleInputDialog
    private val managerViewModel: ManagerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentManagerBinding.bind(view)

        simpleInputDialog = SimpleInputDialog(requireContext(), layoutInflater)

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
                2 -> tab.text = getString(R.string.reviews)
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
            }
        }
    }

    private fun showAddCountyDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_county,
            hint = R.string.county
        ) {
            managerViewModel.addCounty(it)
        }

        simpleInputDialog.show(dialogResources)
    }

    private fun showAddGrapeDialog() {
        val dialogResources = SimpleInputDialog.DialogContent(
            title = R.string.add_grape,
            hint = R.string.grape_name,
            icon = R.drawable.ic_grape
        ) {
            managerViewModel.addGrape(it)
        }

        simpleInputDialog.show(dialogResources)
    }

    private fun showAddReviewDialog() {
        val dialogBinding = DialogAddReviewBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_review)
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.submit) { _, _ ->
                val name = dialogBinding.contestName.text.toString()
                val type = getReviewType(dialogBinding.rbGroupType.checkedButtonId)

                managerViewModel.addReview(name, type)
            }
            .setView(dialogBinding.root)
            .setOnDismissListener { dialogBinding.root.hideKeyboard() }
            .show()

        dialogBinding.contestName.post { dialogBinding.contestName.showKeyboard() }
        dialogBinding.rbMedal.performClick()
    }

    private fun getReviewType(@IdRes button: Int) = when (button) {
        R.id.rbMedal -> 0
        R.id.rbRate20 -> 1
        R.id.rbRate100 -> 2
        else -> 3
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
