package com.louis.app.cavity.ui.addwine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentCameraBinding
import com.louis.app.cavity.ui.Cavity
import com.louis.app.cavity.ui.SnackbarProvider
import com.louis.app.cavity.ui.addwine.FragmentAddWine.Companion.TAKEN_PHOTO_URI
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
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val askPermissions by lazy {
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            handlePermisionResults(it)
        }
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCameraBinding.bind(view)

        snackbarProvider = activity as SnackbarProvider

        if (hasPermissions()) {
            startCamera()
        } else {
            askPermissions.launch(REQUIRED_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(
            { bindPreview(cameraProviderFuture.get()) },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val metrics = DisplayMetrics().also { binding.previewView.display.getRealMetrics(it) }
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
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        } catch (e: Exception) {
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
                        binding.coordinator.showSnackbar(R.string.base_error)
                    }
                })
        }
    }

    private fun checkDirectory(): File? {
        val externalDir = requireActivity().getExternalFilesDir(null)

        if (externalDir != null) {
            val tempDir = File("$externalDir${Cavity.PHOTOS_DIRECTORY}")

            tempDir.apply {
                return if (!exists())
                    if (mkdir()) this else null
                else
                    this
            }
        }

        return null
        // show permission asking if not allowed, else show file error
    }

    private fun hasPermissions() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun handlePermisionResults(permissions: Map<String, Boolean>) {
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            findNavController().navigateUp()
            snackbarProvider.onShowSnackbarRequested(R.string.permissions_denied)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
