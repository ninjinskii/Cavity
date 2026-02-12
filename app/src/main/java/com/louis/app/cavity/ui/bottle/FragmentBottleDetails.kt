package com.louis.app.cavity.ui.bottle

import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.FileUriExposedException
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
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
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.model.Bottle
import com.louis.app.cavity.ui.LifecycleMaterialDialogBuilder
import com.louis.app.cavity.ui.bottle.adapter.BottleChipRecyclerAdapter
import com.louis.app.cavity.ui.bottle.adapter.JumpSmoothScroller
import com.louis.app.cavity.ui.bottle.adapter.ShowFilledReviewsRecyclerAdapter
import com.louis.app.cavity.ui.tasting.SpaceItemDecoration
import com.louis.app.cavity.util.*
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.louis.app.cavity.db.dao.BottleWithHistoryEntries
import com.louis.app.cavity.model.Tag
import com.louis.app.cavity.ui.ChipLoader
import com.louis.app.cavity.ui.settings.SettingsViewModel

class FragmentBottleDetails : Fragment(R.layout.fragment_bottle_details) {
    private lateinit var transitionHelper: TransitionHelper
    private lateinit var errorReporter: ErrorReporter
    private var _binding: FragmentBottleDetailsBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val bottleDetailsViewModel: BottleDetailsViewModel by viewModels()
    private val consumeGiftBottleViewModel: ConsumeGiftBottleViewModel by viewModels()
    private val args: FragmentBottleDetailsArgs by navArgs()

    private var hasRevealGrapeBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transitionHelper = TransitionHelper(this).apply {
            val previousDestination = findNavController().previousBackStackEntry?.destination?.id
            val enterOptions =
                if (previousDestination == R.id.search_dest) {
                    // Background is not colorSurface in search_dest, causing weird animations
                    TransitionHelper.ContainerTransformOptions(
                        Color.TRANSPARENT,
                        requireContext().themeColor(com.google.android.material.R.attr.colorSurface),
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

        errorReporter = SentryErrorReporter.getInstance(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val transition = getString(R.string.transition_bottle_details, args.wineId)
        ViewCompat.setTransitionName(view, transition)

        transitionHelper.setFadeThroughOnEnterAndExit()
        postponeEnterTransition()

        _binding = FragmentBottleDetailsBinding.bind(view)

        applyInsets()
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
            measureTitle()
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

    private fun measureTitle() {
        val titleStart = binding.backButton.run { right + marginEnd }
        val titleEnd = binding.favorite.run { left + marginStart }
        binding.bottleName.maxWidth = titleEnd - titleStart
    }

    private fun applyInsets() {
        val globalMotionLayout = binding.globalMotionLayout
        val motionLayout = binding.motionLayout

        globalMotionLayout?.prepareWindowInsets { _, windowInsets, _, top, _, _ ->
            globalMotionLayout.getConstraintSet(R.id.start)
                .setMargin(R.id.insetHelper, ConstraintSet.TOP, top)
            globalMotionLayout.getConstraintSet(R.id.end)
                .setMargin(R.id.insetHelper, ConstraintSet.TOP, top)

            windowInsets
        }

        motionLayout?.prepareWindowInsets { _, windowInsets, _, top, _, _ ->
            motionLayout.getConstraintSet(R.id.start).apply {
                setMargin(R.id.backButton, ConstraintSet.TOP, top)
                setMargin(R.id.favorite, ConstraintSet.TOP, top)
            }

            motionLayout.getConstraintSet(R.id.end).apply {
                setMargin(R.id.backButton, ConstraintSet.TOP, top)
                setMargin(R.id.favorite, ConstraintSet.TOP, top)
            }

            windowInsets
        }

        binding.scrollView.prepareWindowInsets(false) { view, windowInsets, _, _, right, bottom ->
            view.updatePadding(bottom = bottom, right = right)
            windowInsets
        }

        binding.container?.prepareWindowInsets(false)
        { view, windowInsets, left, top, right, bottom ->
            view.updatePadding(left = left, top = top, right = right)

            val marginLayoutParams =
                binding.bottlesList.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.updateMargins(bottom = bottom)

            windowInsets
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
                binding.fab.progress = 1 - progress

                if (!hasRevealGrapeBar && checkViewIsOnScreen(binding.grapeBar)) {
                    hasRevealGrapeBar = true
                    binding.grapeBar.triggerAnimation()

                    binding.scrollView.setOnScrollChangeListener(
                        null as View.OnScrollChangeListener?
                    )
                }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                if (currentId == R.id.start) {
                    shaper.interpolation = 1f
                    binding.fab.progress = 1f
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) =
                Unit
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

        var firstTime = true

        bottleDetailsViewModel.getBottlesForWine(args.wineId).observe(viewLifecycleOwner) {
            val checkedBottleId = bottleDetailsViewModel.getBottleId()
            val id = bottleAdapter.submitListWithPreselection(it, checkedBottleId ?: -1L)
            bottleDetailsViewModel.setBottleId(id)

            val hasAtLeastOneBottleConsumed = it.any { a -> a.bottle.consumed.toBoolean() }
            binding.buttonTastingLog.setVisible(hasAtLeastOneBottleConsumed)

            // Avoid weird DiffUtil animations conflict
            if (firstTime) {
                smoothScrollToCheckedChip(id, it)
                firstTime = false
            }
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

        bottleDetailsViewModel.replenishmentEntry.observe(viewLifecycleOwner) {
            val isAGift = it?.friends?.isNotEmpty() == true

            binding.givenBy.apply {
                setVisible(isAGift)
                val friend = it?.friends?.joinToString { friend -> friend.name } ?: return@apply
                setData(friend)

                val firstPicture =
                    it.friends.firstOrNull { f -> f.imgPath.isNotEmpty() } ?: return@apply

                val imgPath = firstPicture.imgPath
                AvatarLoader.requestAvatar(requireContext(), imgPath) { avatarBitmap ->
                    avatarBitmap?.let { drawable ->
                        setIcon(drawable)
                    }
                }
            }
        }

        bottleDetailsViewModel.getWineById(args.wineId).observe(viewLifecycleOwner) {
            with(binding) {
                bottleName.text = it.name
                cuvee.setVisible(it.cuvee.isNotBlank())
                cuvee.setData(it.cuvee)
            }

            showImage(it.imgPath.toUri())
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

        bottleDetailsViewModel.revertConsumptionEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { boundedBottle ->
                binding.coordinator.showSnackbar(R.string.back_in_stock, R.string.cancel) {
                    consumeGiftBottleViewModel.consumeBottle(boundedBottle)
                }
            }
        }

        bottleDetailsViewModel.removeFromTastingEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { bottleToTasting ->
                binding.coordinator.showSnackbar(
                    R.string.bottle_removed_from_tasting,
                    R.string.cancel
                ) {
                    bottleDetailsViewModel.cancelRemoveBottleFromTasting(
                        bottleId = bottleToTasting.first,
                        tastingId = bottleToTasting.second
                    )
                }
            }
        }

        bottleDetailsViewModel.removeTagEvent.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { tagToBottle ->
                binding.coordinator.showSnackbar(
                    R.string.tag_removed_from_bottle,
                    R.string.cancel
                ) {
                    bottleDetailsViewModel.cancelRemoveTag(
                        tagId = tagToBottle.first,
                        bottleId = tagToBottle.second
                    )
                }
            }
        }

        bottleDetailsViewModel.tags.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }

            binding.tagsScrollView.setVisible(!it.tags.isEmpty())

            ChipLoader.Builder()
                .with(lifecycleScope)
                .useInflater(layoutInflater)
                .toInflate(R.layout.chip_tag)
                .load(it.tags)
                .into(binding.tagsChipGroup)
                .closable { tag -> bottleDetailsViewModel.removeTag(tag as Tag) }
                .selectable(false)
                .build()
                .go()
        }
    }

    private fun setListeners() {
        binding.fab.setOnClickListener {
            navigateToAddBottle(-1)
        }

        binding.buttonEdit.setOnClickListener {
            val id = bottleDetailsViewModel.getBottleId()
            id?.let { navigateToAddBottle(it) }
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

        binding.buttonTastingLog.setOnClickListener {
            transitionHelper.setFadeThrough(navigatingForward = true)

            val action =
                FragmentBottleDetailsDirections.bottleDetailsToHistory(-1, args.wineId, true)
            findNavController().navigate(action)
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

    private fun smoothScrollToCheckedChip(
        checkedChipBottleId: Long,
        bottles: List<BottleWithHistoryEntries>?
    ) {
        if (bottles == null || checkedChipBottleId == -1L) {
            return
        }

        val position = bottles.indexOfFirst { it.bottle.id == checkedChipBottleId }

        if (position == -1) {
            return
        }

        val scroller = JumpSmoothScroller(requireContext(), jumpThreshold = 10).apply {
            targetPosition = position
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
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ) = false.also { startPostponedEnterTransition() }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ) = false.also { startPostponedEnterTransition() }
                })
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.bottlePicture)
        } catch (_: SecurityException) {
            // Do nothing
        }
    }

    private fun showPdf(pdf: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdf, "application/pdf")
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            errorReporter.captureMessage("Cannot open pdf: no pdf reader")
            binding.coordinator.showSnackbar(R.string.no_pdf_app)
        } catch (_: SecurityException) {
            errorReporter.captureMessage("Cannot open pdf: security exception")
            binding.coordinator.showSnackbar(R.string.base_error)
        } catch (_: FileUriExposedException) {
            errorReporter.captureMessage("Cannot open pdf: pdf uri seems wrong")
            bottleDetailsViewModel.clearPdfPath()
            binding.coordinator.showSnackbar(R.string.base_error)
        }
    }

    private fun navigateToAddBottle(bottleId: Long) {
        transitionHelper.setSharedAxisTransition(MaterialSharedAxis.Z, navigatingForward = true)
        val action =
            FragmentBottleDetailsDirections.bottleDetailsToEditBottle(args.wineId, bottleId)

        findNavController().navigate(action)
    }

    private fun updateUI(bottle: Bottle, lastBottleId: Long) {
        with(binding) {
            val consumed = bottle.consumed.toBoolean()
            val hasTasting = bottle.tastingId != null
            val shouldJumpDrawableState = bottle.id != lastBottleId
            val formattedPrice = bottle.price.let { if (it != -1F) it.toString() else "" }
            val priceAndCurrency =
                if (formattedPrice.isEmpty()) "" else "$formattedPrice ${bottle.currency}"

            buttonGroupInteract.setVisible(!consumed && !hasTasting)
            warningBanner.setVisible(consumed || hasTasting, true)

            val stringRes = if (consumed) R.string.consumed else R.string.bottle_used_in_tasting
            val buttonStringRes = if (consumed) R.string.cancel else R.string.retire

            bannerText.text = getString(stringRes)
            buttonRevertConsumption.text = getString(buttonStringRes)

            buttonRevertConsumption.setOnClickListener {
                if (consumed) {
                    bottleDetailsViewModel.revertBottleConsumption()
                } else {
                    bottleDetailsViewModel.removeBottleFromTasting()
                }
            }

            apogee.setData(bottle.apogee?.toString() ?: getString(R.string.unknown))
            buyLocation.setData(bottle.buyLocation)
            buyDate.setData(DateFormatter.formatDate(bottle.buyDate))
            capacity.setData(getString(bottle.bottleSize.stringRes))
            storageLocation.apply {
                val storageLocationEnabled = settingsViewModel.getEnableBottleStorageLocation()
                setVisible(bottle.storageLocation.isNotEmpty() && storageLocationEnabled)
                setData(bottle.storageLocation)
            }
            alcohol.apply {
                setVisible(bottle.alcohol != null)
                setData(bottle.alcohol.toString())
            }
            otherInfo.setData(bottle.otherInfo)
            buttonPdfIcon.isEnabled = bottle.hasPdf()
            favorite.isChecked = bottle.isFavorite.toBoolean()
            price.setData(priceAndCurrency)

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
