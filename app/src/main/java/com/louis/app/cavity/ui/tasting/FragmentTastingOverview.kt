package com.louis.app.cavity.ui.tasting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentTastingOverviewBinding
import com.louis.app.cavity.ui.tasting.notifications.TastingNotifier
import com.louis.app.cavity.util.setupNavigation

class FragmentTastingOverview : Fragment(R.layout.fragment_tasting_overview) {
    private var _binding: FragmentTastingOverviewBinding? = null
    private val binding get() = _binding!!
    private val tastingOverviewViewModel: TastingOverviewViewModel by viewModels()
    private val args: FragmentTastingOverviewArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTastingOverviewBinding.bind(view)

        setupNavigation(binding.appBar.toolbar)
        tastingOverviewViewModel.start(args.tastingId)

        initRecyclerView()
        observe()
    }

    private fun initRecyclerView() {
        val tastingOverviewAdapter = BottleActionAdapter(
            onActionCheckedChange = { tastingAction, isChecked ->
                tastingOverviewViewModel.setActionIsChecked(tastingAction, isChecked)
            }
        )

        binding.bottleTastingActionsList.apply {
            adapter = tastingOverviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        tastingOverviewViewModel.bottles.observe(viewLifecycleOwner) {
            tastingOverviewAdapter.submitList(it)
        }
    }

    private fun observe() {
        tastingOverviewViewModel.notificationEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { pair ->
                pair.second.forEach { action ->
                    val notification = TastingNotifier.buildNotification(
                        requireContext(),
                        tasting = pair.first,
                        action
                    )

                    TastingNotifier.notify(requireContext(), notification)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
