package com.louis.app.cavity.ui.addwine

import android.Manifest
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.postDelayed
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentCameraBinding
import com.louis.app.cavity.domain.error.ErrorReporter
import com.louis.app.cavity.domain.error.SentryErrorReporter
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addwine.FragmentAddWine.Companion.TAKEN_PHOTO_URI
import com.louis.app.cavity.ui.settings.SettingsViewModel
import com.louis.app.cavity.util.PermissionChecker
import com.louis.app.cavity.util.TransitionHelper
import com.louis.app.cavity.util.prepareWindowInsets
import com.louis.app.cavity.util.showSnackbar
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FragmentCamera : Fragment(R.layout.fragment_camera) {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var snackbarProvider: SnackbarProvider
    private lateinit var permissionChecker: PermissionChecker
    private lateinit var errorReporter: ErrorReporter
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val settingsViewModel: SettingsViewModel by activityViewModels()

    private var camera: Camera? = null

    companion object {
        const val TEMPLATE_ROTATION = -45f
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionHelper(this).apply {
            setFadeThrough(navigatingForward = true)
            setFadeThrough(navigatingForward = false)
        }

        errorReporter = SentryErrorReporter.getInstance(requireContext())

        permissionChecker = object : PermissionChecker(this, REQUIRED_PERMISSIONS) {
            override fun onPermissionsAccepted() {
                startCamera()
            }

            override fun onPermissionsDenied() {
                findNavController().navigateUp()
                snackbarProvider.onShowSnackbarRequested(R.string.permissions_denied)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCameraBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider
        permissionChecker.askPermissionsIfNecessary()
        cameraExecutor = Executors.newSingleThreadExecutor()

        applyInsets()
        setListener()
        rotateTemplate(settingsViewModel.getSkewBottle())
        scaleTemplate(settingsViewModel.getTemplateSize())
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            { bindPreview(cameraProviderFuture.get()) },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val metrics = DisplayMetrics()
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = binding.previewView.display.rotation

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(rotation)
            .build()

        binding.buttonCapture.setOnClickListener {
            saveImage(imageCapture)
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        } catch (e: Exception) {
            errorReporter.captureException(e)
            camera = null
            binding.coordinator.showSnackbar(R.string.camera_error)
        }
    }

    private fun saveImage(imageCapture: ImageCapture) {
        checkDirectory()?.let { outputDir ->
            val filename = "${System.currentTimeMillis()}.jpg"
            val file = File(outputDir, filename)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        with(binding.coordinator) {
                            postDelayed(50) {
                                binding.toggleTorch.isChecked = false
                                foreground = ColorDrawable(Color.WHITE)

                                postDelayed(100) {
                                    foreground = null

                                    findNavController().run {
                                        previousBackStackEntry?.savedStateHandle?.set(
                                            TAKEN_PHOTO_URI,
                                            Uri.fromFile(file).toString()
                                        )

                                        navigateUp()
                                    }
                                }
                            }
                        }
                    }

                    override fun onError(e: ImageCaptureException) {
                        errorReporter.captureException(e)
                        binding.coordinator.showSnackbar(R.string.base_error)
                    }
                })
        }
    }

    private fun checkDirectory(): File? {
        val externalDir = requireActivity().getExternalFilesDir(null)

        if (externalDir != null) {
            val tempDir = File("$externalDir")

            tempDir.apply {
                return if (!exists())
                    if (mkdir()) this else null
                else
                    this
            }
        }

        return null
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun applyInsets() {
        val infoPanelMargin = binding.infoPanel.marginTop
        binding.infoPanel.prepareWindowInsets { view, windowInsets, _, top, _, _ ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(top = infoPanelMargin + top)
            windowInsets
        }

        val buttonCaptureMargin = binding.buttonCapture.marginBottom
        binding.buttonCapture.prepareWindowInsets { view, windowInsets, _, _, _, bottom ->
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.updateMargins(bottom = buttonCaptureMargin + bottom)
            windowInsets
        }
    }

    private fun setListener() {
        binding.toggleSkewBottle.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            isChecked = settingsViewModel.getSkewBottle()
            jumpDrawablesToCurrentState()

            setOnCheckedChangeListener { _, isChecked ->
                settingsViewModel.setSkewBottle(isChecked)
                rotateTemplate(shouldRotate = isChecked)
            }
        }

        binding.toggleTorch.apply {
            thumbDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.switch_thumb,
                requireContext().theme
            )

            jumpDrawablesToCurrentState()

            setOnCheckedChangeListener { _, isChecked ->
                setTorchState(isChecked)
            }
        }
    }

    private fun rotateTemplate(shouldRotate: Boolean) {
        binding.bottleTemplate.doOnLayout {
            ObjectAnimator.ofFloat(it, "rotation", 0f, TEMPLATE_ROTATION).apply {
                duration = resources.getInteger(R.integer.cavity_motion_short).toLong()
                interpolator = FastOutSlowInInterpolator()
                if (shouldRotate) start() else reverse()
            }
        }
    }

    private fun scaleTemplate(scale: Float) {
        binding.bottleTemplate.scaleX = scale
        binding.bottleTemplate.scaleY = scale
    }

    private fun setTorchState(on: Boolean) {
        camera?.cameraControl?.enableTorch(on)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        camera = null
        cameraExecutor.shutdown()
    }
}
