package com.louis.app.cavity.ui.bottle

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Checkable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.DialogAddBottleBinding
import com.louis.app.cavity.databinding.DialogUseBottleBinding
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.bottle.adapter.ShowFilledReviewsRecyclerAdapter
import com.louis.app.cavity.ui.search.widget.AnimatedImageButton
import com.louis.app.cavity.util.*

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
            if (it.isEmpty()) {
                binding.reviewCardView.setVisible(false)
            } else {
                reviewAdapter.submitList(it)
            }
        }
    }

    private fun observe() {
        bottleDetailsViewModel.getBottleById(args.bottleId).observe(viewLifecycleOwner) {
            updateUI(it)
        }

        bottleDetailsViewModel.getQGrapesForBottle(args.bottleId).observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.grapesCardView.setVisible(false)
            } else {
                binding.grapeBar.apply {
                    addAllGrapes(it)
                    triggerAnimation()
                }
            }
        }

        bottleDetailsViewModel.getWineById(args.wineId).observe(viewLifecycleOwner) {
            binding.bottleName.text = it.name
            showImage(Uri.parse(it.imgPath))
        }

        bottleDetailsViewModel.pdfEvent.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { uri ->
                showPdf(uri)
            }
        }

        bottleDetailsViewModel.userFeedback.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { stringRes ->
                binding.coordinator.showSnackbar(stringRes)
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

        binding.buttonConsume.setOnClickListener {
            (it as Checkable).isChecked = false
            val dialogBinding = DialogUseBottleBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.use_long))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                    val count = dialogBinding.bottleCount.text.toString().toInt()
                    bottleDetailsViewModel.removeBottles(args.bottleId, count)
                    // TODO: history
                }
                .setView(dialogBinding.root)
                .show()
        }

        binding.buttonProvide.setOnClickListener {
            (it as Checkable).isChecked = false
            val dialogBinding = DialogAddBottleBinding.inflate(layoutInflater)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.entry))
                .setMessage(resources.getString(R.string.how_many_to_add))
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                    val count = dialogBinding.bottleCount.text.toString().toInt()
                    bottleDetailsViewModel.addBottles(args.bottleId, count)
                }
                .setView(dialogBinding.root)
                .show()
        }

        binding.buttonShowPdf.setOnClickListener {
            bottleDetailsViewModel.preparePdf(args.bottleId)
        }

        binding.favorite.setOnClickListener {
            bottleDetailsViewModel.toggleFavorite(args.bottleId)
        }
    }

    private fun showImage(image: Uri) {
        try {
            Glide.with(this)
                .load(image)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.bottlePicture)
        } catch (e: SecurityException) {
            // Do nothing
        }
    }

    private fun showPdf(pdf: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.apply {
            setDataAndType(pdf, "application/pdf")
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        try {
            startActivity(intent)
        } catch (a: ActivityNotFoundException) {
            binding.coordinator.showSnackbar(R.string.no_pdf_app)
        } catch (e: SecurityException) {
            binding.coordinator.showSnackbar(R.string.base_error)
        }
    }

    private fun updateUI(bottle: Bottle) {
        with(binding) {
            stock.text = getString(R.string.stock_number, bottle.count)
            apogee.setData(bottle.apogee.toString())
            price.setData(
                getString(
                    R.string.price_and_currency,
                    bottle.price.toString(),
                    bottle.currency
                )
            )
            buyLocation.setData(bottle.buyLocation)
            buyDate.setData(DateFormatter.formatDate(bottle.buyDate))
            otherInfo.setData(bottle.otherInfo)
            bottleVintage.text = bottle.vintage.toString()

            if (!bottle.hasPdf()) {
                noPdf.setVisible(true)
                buttonShowPdf.setVisible(false)
            }

            if (bottle.isFavorite.toBoolean()) {
                // TODO: Fix favorite wrong icon when bottle is fav
                favorite.triggerAnimation()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
