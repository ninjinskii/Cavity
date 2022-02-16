package com.louis.app.cavity.ui.bottle

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Checkable
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.transition.MaterialSharedAxis
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentBottleDetailsBinding
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.bottle.adapter.BottleChipRecyclerAdapter
import com.louis.app.cavity.ui.bottle.adapter.JumpSmoothScroller
import com.louis.app.cavity.ui.bottle.adapter.ShowFilledReviewsRecyclerAdapter
import com.louis.app.cavity.ui.tasting.SpaceItemDecoration
import com.louis.app.cavity.util.*

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private lateinit var transitionHelper: TransitionHelper
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!
    private val bottleDetailsViewModel: BottleDetailsViewModel by viewModels()
    private val args: FragmentBottleDetailsArgs by navArgs()

    private var hasRevealGrapeBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transitionHelper = TransitionHelper(this).apply {
            val previousDestination = findNavController().previousBackStackEntry?.destination?.id
            val enterOptions = if (previousDestination == R.id.search_dest) {
                TransitionHelper.ContainerTransformOptions(
                    Color.TRANSPARENT,
                    requireContext().themeColor(R.attr.colorSurface),
                    startElevation = resources.getDimension(R.dimen.container_drop_elevation),
                    endElevation = resources.getDimension(R.dimen.app_bar_elevation)
                ).also {
                    val returnOptions = TransitionHelper.ContainerTransformOptions(
                        Color.TRANSPARENT,
                        requireContext().getColor(R.color.surface_elevation_4dp),
                        startElevation = resources.getDimension(R.dimen.app_bar_elevation),
                        endElevation = resources.getDimension(R.dimen.container_drop_elevation)
                    )
                    setContainerTransformTransition(returnOptions, enter = false)
                }
            } else {
                null
            }

            setContainerTransformTransition(enterOptions, enter = true)
            setFadeThrough(navigatingForward = false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transition = getString(R.string.transition_bottle_details, args.wineId)
        ViewCompat.setTransitionName(view, transition)

        transitionHelper.setFadeThroughOnEnterAndExit()
        postponeEnterTransition()

        _binding = FragmentBottleDetailsBinding.bind(view)

        setupToolbarShape()
        initRecyclerViews()
        observe()
        setListeners()

        val currentId = bottleDetailsViewModel.getBottleId()

        if ((currentId == -1L && args.bottleId != -1L) || currentId == null) {
            bottleDetailsViewModel.setBottleId(args.bottleId)
        }

        binding.root.doOnLayout {
            binding.scrollView.scrollY = 0
            setupScrollViewWatcher()
        }
    }

    private fun setupScrollViewWatcher() {
        hasRevealGrapeBar = checkViewIsOnScreen(binding.grapeBar)

        if (hasRevealGrapeBar) {
            binding.grapeBar.triggerAnimation()
            return
        }

        binding.scrollView.setOnScrollChangeListener { v, _, _, _, _ ->
            v as NestedScrollView

            if (v.isViewVisible(binding.grapeBar) && !hasRevealGrapeBar) {
                binding.grapeBar.triggerAnimation()
                hasRevealGrapeBar = true

                binding.scrollView.setOnScrollChangeListener(null as View.OnScrollChangeListener?)
            }
        }
    }

    private fun setupToolbarShape() {
        val shaper = MaterialShapeDrawable.createWithElevationOverlay(context)

        binding.shaper?.apply {
            background = shaper

            doOnLayout {
                shaper.shapeAppearanceModel = ShapeAppearanceModel.Builder()
                    .setTopEdge(RoundedEdgeTreatment(it.height.toFloat())).build()
            }
        }

        binding.motionLayout?.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, progress: Float) {
                shaper.interpolation = 1 - progress

                if (!hasRevealGrapeBar && checkViewIsOnScreen(binding.grapeBar)) {
                    hasRevealGrapeBar = true
                    binding.grapeBar.triggerAnimation()

                    binding.scrollView.setOnScrollChangeListener(
                        null as View.OnScrollChangeListener?
                    )
                }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                if (currentId == R.id.start) shaper.interpolation = 1f
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
        })
    }

    private fun initRecyclerViews() {
        val bottleAdapter = BottleChipRecyclerAdapter(
            requireContext(),
            onBottleClick = { bottleDetailsViewModel.setBottleId(it) }
        )

        binding.bottlesList.apply {
            adapter = bottleAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)

            val space = resources.getDimensionPixelSize(R.dimen.small_margin)
            addItemDecoration(SpaceItemDecoration(space))
        }

        bottleDetailsViewModel.getBottlesForWine(args.wineId).observe(viewLifecycleOwner) {
            val checkedBottleId = bottleDetailsViewModel.getBottleId()
            val id = bottleAdapter.submitListWithPreselection(it, checkedBottleId ?: -1L)
            smoothScrollToCheckedChip(id, it)
            bottleDetailsViewModel.setBottleId(id)
        }

        val colorUtil = ColorUtil(requireContext())
        val reviewAdapter = ShowFilledReviewsRecyclerAdapter(colorUtil)

        binding.reviewList.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = reviewAdapter
        }

        bottleDetailsViewModel.reviews.observe(viewLifecycleOwner) {
            binding.divider3.setVisible(it.isNotEmpty())
            binding.reviewList.setVisible(it.isNotEmpty())
            reviewAdapter.submitList(it)
        }
    }

    private fun observe() {
        var firstRun = true
        var lastBottleId = -1L

        bottleDetailsViewModel.bottle.observe(viewLifecycleOwner) {
            if (it != null) {
                updateUI(it, lastBottleId)
                lastBottleId = it.id
            }
        }

        bottleDetailsViewModel.grapes.observe(viewLifecycleOwner) {
            binding.divider2.setVisible(it.isNotEmpty())
            binding.grapeBar.apply {
                setVisible(it.isNotEmpty())
                setSlices(it, anim = false)

                if (!firstRun) {
                    triggerAnimation()
                }

                firstRun = false
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
        binding.buttonEdit.setOnClickListener {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)

            val id = bottleDetailsViewModel.getBottleId()

            id?.let { bottleId ->
                val action = FragmentBottleDetailsDirections.bottleDetailsToEditBottle(
                    args.wineId,
                    bottleId
                )

                findNavController().navigate(action)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonConsume.setOnClickListener {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Y, navigatingForward = true)

            (it as Checkable).isChecked = false
            val id = bottleDetailsViewModel.getBottleId()

            id?.let { bottleId ->
                val action = FragmentBottleDetailsDirections.bottleDetailsToConsumeBottle(bottleId)
                findNavController().navigate(action)
            }
        }

        binding.buttonGiftTo.setOnClickListener {
            transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Y, navigatingForward = true)

            (it as Checkable).isChecked = false
            val id = bottleDetailsViewModel.getBottleId()

            id?.let { bottleId ->
                val action = FragmentBottleDetailsDirections.bottleDetailsToGiftBottle(bottleId)
                findNavController().navigate(action)
            }
        }

        binding.buttonPdf.setOnClickListener {
            bottleDetailsViewModel.preparePdf()
        }

        binding.buttonHistory.setOnClickListener {
            transitionHelper.setFadeThrough(navigatingForward = true)

            val id = bottleDetailsViewModel.getBottleId()

            id?.let { bottleId ->
                val action = FragmentBottleDetailsDirections.bottleDetailsToHistory(bottleId)
                findNavController().navigate(action)
            }
        }

        binding.favorite.setOnClickListener {
            bottleDetailsViewModel.toggleFavorite()
        }

        binding.buttonUltraDelete.setOnClickListener {
            LifecycleMaterialDialogBuilder(requireContext(), viewLifecycleOwner)
                .setMessage(R.string.confirm_bottle_delete)
                .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                }
                .setPositiveButton(resources.getString(R.string.submit)) { _, _ ->
                    bottleDetailsViewModel.deleteBottle()
                    findNavController().popBackStack()
                }
                .show()
        }
    }

    private fun smoothScrollToCheckedChip(checkedChipBottleId: Long, bottles: List<Bottle>?) {
        if (bottles == null || checkedChipBottleId == -1L) {
            return
        }

        val postion = bottles.indexOfFirst { it.id == checkedChipBottleId }

        if (postion == -1) {
            return
        }

        val scroller = JumpSmoothScroller(requireContext(), 10).apply {
            targetPosition = postion
        }

        binding.bottlesList.layoutManager?.startSmoothScroll(scroller)
    }

    private fun checkViewIsOnScreen(view: View): Boolean {
        val scrollViewStart = binding.scrollView.top
        val availableHeight = binding.root.height

        if (scrollViewStart > availableHeight) return false

        return scrollViewStart + view.top < availableHeight
    }

    private fun showImage(image: Uri) {
        try {
            Glide.with(this)
                .load(image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ) = false.also { startPostponedEnterTransition() }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ) = false.also { startPostponedEnterTransition() }
                })

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

    private fun updateUI(bottle: Bottle, lastBottleId: Long) {
        with(binding) {
            val formattedPrice = bottle.price.let { if (it != -1F) it.toString() else "" }
            val consumed = bottle.consumed.toBoolean()
            val shouldJumpDrawableState = bottle.id != lastBottleId

            buttonGroupInteract.setVisible(!consumed)
            warningBanner.setVisible(consumed || bottle.tastingId != null)
            bottlesList.setVisible(!consumed)
            vintageIfConsumed.setVisible(consumed)

            val stringRes = if (consumed) R.string.consumed else R.string.bottle_used_in_tasting
            val buttonStringRes = if (consumed) R.string.cancel else R.string.retire

            bannerText.text = getString(stringRes)
            buttonRevertConsumption.text = getString(buttonStringRes)
            vintageIfConsumed.text = bottle.vintage.toString()

            buttonRevertConsumption.setOnClickListener {
                if (consumed) {
                    bottleDetailsViewModel.revertBottleConsumption()
                } else {
                    bottleDetailsViewModel.removeBottleFromTasting()
                }
            }

            apogee.setData(bottle.apogee.toString())
            buyLocation.setData(bottle.buyLocation)
            buyDate.setData(DateFormatter.formatDate(bottle.buyDate))
            capacity.setData(getString(bottle.bottleSize.stringRes))
            otherInfo.setData(bottle.otherInfo)
            buttonPdfIcon.isEnabled = bottle.hasPdf()
            favorite.isChecked = bottle.isFavorite.toBoolean()

            if (formattedPrice.isNotEmpty()) {
                price.setData(
                    getString(
                        R.string.price_and_currency,
                        formattedPrice,
                        bottle.currency
                    )
                )
            }

            if (shouldJumpDrawableState) {
                favorite.jumpDrawablesToCurrentState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
