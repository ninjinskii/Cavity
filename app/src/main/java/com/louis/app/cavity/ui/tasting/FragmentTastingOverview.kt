package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingOverviewBinding
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addtasting.SpaceGridItemDecoration
import com.louis.app.cavity.ui.notifications.NotificationBuilder
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.setVisible
import com.louis.app.cavity.util.setupNavigation
import com.louis.app.cavity.util.showSnackbar

class FragmentTastingOverview : Fragment(R.layout.fragment_tasting_overview) {
    private lateinit var snackbarProvider: SnackbarProvider
    private var _binding: FragmentTastingOverviewBinding? = null
    private val binding get() = _binding!!
    private val tastingOverviewViewModel: TastingOverviewViewModel by viewModels()
    private val args: FragmentTastingOverviewArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setContainerTransformTransition(options = null, enter = true)
            setFadeThrough(navigatingForward = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(view, args.tastingId.toString())

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        _binding = FragmentTastingOverviewBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        setupNavigation(binding.appBar.toolbar)
        tastingOverviewViewModel.start(args.tastingId)

        applyInsets()
        initRecyclerView()
        observe()
        setListeners()
    }

    private fun applyInsets() {
        binding.appBar.toolbarLayout.prepareWindowInsets { view, _, left, top, right, _ ->
            view.updatePadding(left = left, right = right, top = top)
            WindowInsetsCompat.CONSUMED
        }

        binding.bottleTastingActionsList.prepareWindowInsets { view, _, left, _, right, bottom ->
            view.updatePadding(left = left, right = right, bottom = bottom)
            WindowInsetsCompat.CONSUMED
        }

        binding.buttonSubmit.prepareWindowInsets { view, _, left, _, right, _ ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(left = left, right = right)

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun initRecyclerView() {
        val space = requireContext().resources.getDimension(R.dimen.small_margin)
        val tastingOverviewAdapter = BottleActionAdapter(
            onActionCheckedChange = { tastingAction, isChecked ->
                if (isChecked) {
                    NotificationBuilder.cancelNotification(
                        requireContext(),
                        tastingAction.id.toInt()
                    )
                } else {
                    tastingOverviewViewModel.requestNotificationsForTastingAction(
                        requireContext(),
                        tastingAction
                    )
                }

                tastingOverviewViewModel.setActionIsChecked(tastingAction, isChecked)
            },
            onCommentChanged = { bottle, comment ->
                tastingOverviewViewModel.updateBottleComment(bottle, comment)
            },
            onCloseIconClicked = { bottle ->
                tastingOverviewViewModel.updateBottleTasting(bottle, tastingId = null)
                binding.coordinator.showSnackbar(
                    stringRes = R.string.bottle_removed_from_tasting,
                    actionStringRes = R.string.cancel,
                    action = {
                        tastingOverviewViewModel.updateBottleTasting(
                            bottle,
                            tastingId = args.tastingId
                        )
                    }
                )
            }
        )

        binding.bottleTastingActionsList.apply {
            adapter = tastingOverviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(SpaceGridItemDecoration(space.toInt()))
        }

        tastingOverviewViewModel.bottles.observe(viewLifecycleOwner) {
            binding.emptyState.setVisible(it.isEmpty())
            tastingOverviewAdapter.submitList(it)
        }
    }

    private fun observe() {
        tastingOverviewViewModel.tastingConfirmed.observe(viewLifecycleOwner) {
            returnTransition = MaterialFadeThrough()
            sharedElementReturnTransition = null

            snackbarProvider.onShowSnackbarRequested(R.string.tasting_confirmed)

            findNavController().popBackStack()
        }
    }

    private fun setListeners() {
        binding.buttonSubmit.setOnClickListener {
            LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
                .setTitle(R.string.confirm_tasting)
                .setMessage(R.string.confirm_tasting_explanation)
                .setPositiveButton(R.string.ok) { _, _ ->
                    tastingOverviewViewModel.confirmTasting()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                }
                .show()
        }

        binding.emptyState.setOnActionClickListener {
            tastingOverviewViewModel.confirmTasting()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
