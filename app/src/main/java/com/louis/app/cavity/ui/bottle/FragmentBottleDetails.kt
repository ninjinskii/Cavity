package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.ui.bottle.adapter.ShowFilledReviewsRecyclerAdapter

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!
    private val bottleDetailsViewModel: BottleDetailsViewModel by viewModels()
    private val args: FragmentBottleDetailsArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBottleDetailsBinding.bind(view)

        initRecyclerView()
        observe()
        setListeners()
    }

    private fun initRecyclerView() {
        val reviewAdapter = ShowFilledReviewsRecyclerAdapter()

        binding.reviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = reviewAdapter
        }

        bottleDetailsViewModel.getFReviewForBottle(args.bottleId).observe(viewLifecycleOwner) {
            reviewAdapter.submitList(it)
        }
    }

    private fun observe() {
        bottleDetailsViewModel.getBottleById(args.bottleId).observe(viewLifecycleOwner) {
            with(binding) {
                stock.text = getString(R.string.stock_number, it.count)
                apogee.setData(it.apogee.toString())
//                price.text =
//                    getString(R.string.price_and_currency, it.price.toString(), it.currency)
//                buyLocation.text = it.buyLocation
//                buyDate.text = DateFormatter.formatDate(it.buyDate)
//                otherInfo.text = it.otherInfo
            }
        }

        bottleDetailsViewModel.getQGrapesForBottle(args.bottleId).observe(viewLifecycleOwner) {
            binding.grapeBar.apply {
                addAllGrapes(it)
                triggerAnimation()
            }
        }
    }

    private fun setListeners() {
        binding.fabEditBottle.setOnClickListener {
            val action = FragmentBottleDetailsDirections.bottleDetailsToEditBottle(
                args.wineId,
                args.bottleId
            )

            findNavController().navigate(action)
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonConsume.setOnClickListener { }

        binding.buttonProvide.setOnClickListener { }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
