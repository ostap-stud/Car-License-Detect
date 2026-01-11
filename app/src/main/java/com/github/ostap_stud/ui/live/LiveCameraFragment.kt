package com.github.ostap_stud.ui.live

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.ostap_stud.R
import com.github.ostap_stud.analysis.CarLicenseImageAnalyzer
import com.github.ostap_stud.analysis.Detection
import com.github.ostap_stud.analysis.InputPreprocessor
import com.github.ostap_stud.analysis.LicenseDetection
import com.github.ostap_stud.databinding.FragmentLiveCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface OnObjectsDetectListener{
    fun onObjectsDetected(
        carDetections: List<Detection>,
        plateDetection: List<LicenseDetection>,
        inputImageWidth: Float,
        inputImageHeight: Float
    )
}

class LiveCameraFragment : Fragment(), OnObjectsDetectListener {

    private lateinit var binding: FragmentLiveCameraBinding

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private val viewModel: LiveCameraViewModel by activityViewModels()
    private val navController by lazy { findNavController() }

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (!allPermissionsGranted()){
            Toast.makeText(requireContext(), "Permissions denied!", Toast.LENGTH_SHORT)
            navController.navigateUp()
        } else {
            startCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLiveCameraBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!allPermissionsGranted()){
            requestPermissionsLauncher.launch(REQUIRED_PREMISSIONS)
        }
        startCamera()
        binding.btnCapture.setOnClickListener {
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(requireContext()),
                object : OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        capturePhoto(image)
                        Log.e(TAG, "Photo captured successfully!")
                        cameraExecutor.shutdown()
                        navController.navigate(R.id.navigation_home)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        val msg = "Photo capturing error!"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, msg)
                    }
                }
            )
        }
    }

    private fun capturePhoto(image: ImageProxy) {
        viewModel.capturedBitmap = InputPreprocessor.rotateBitmap(
            image.toBitmap(), image.imageInfo.rotationDegrees.toFloat()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun startCamera(){
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext())
        cameraProvider.addListener({
            val provider = cameraProvider.get()
            val previewCase = Preview.Builder()
                .build()

            val imageAnalyzer = CarLicenseImageAnalyzer(
                onObjectsDetectListener = this@LiveCameraFragment
            )

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, imageAnalyzer)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, cameraSelector, previewCase, imageAnalysis, imageCapture)
                previewCase.surfaceProvider = binding.cameraPreview.surfaceProvider
            } catch (e: Exception){
                Log.e(TAG, "Camera provider failed to bind the use cases", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onObjectsDetected(
        carDetections: List<Detection>,
        plateDetection: List<LicenseDetection>,
        inputImageWidth: Float,
        inputImageHeight: Float
    ) {
        if (!isDetached){
            binding.detectionOverlay.setAndInvalidate(
                carDetections, plateDetection, inputImageWidth, inputImageHeight
            )
        }
    }

//    private fun startCamera(){
//        val context = requireContext()
//        val cameraController = LifecycleCameraController(context)
//        val cameraPreviewView = binding.cameraPreview
//
//        val carDetector = retrieveObjectDetectorFromAsset("yolo11n.tflite")
//
////        val imageAnalyzer = MlKitAnalyzer(
////            listOf(carDetector),
////            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
////            ContextCompat.getMainExecutor(context)
////        ) { result ->
////            val carDetectResults = result.getValue(carDetector)
////            if (carDetectResults == null || carDetectResults.size == 0 || carDetectResults.first() == null){
////                Log.d(TAG, "No cars detected")
////                return@MlKitAnalyzer
////            }
////            for (carDetect in carDetectResults){
////                Log.d(TAG, "Car Detected: ID(${carDetect.trackingId}) - Box: ${carDetect.boundingBox}")
////            }
////        }
//
//        val imageAnalyzer = ImageAnalysis.Builder()
//            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//            .build()
//            .also {
//                it.setAnalyzer(cameraExecutor, CarLicenseImageAnalyzer())
//            }
//
//        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//        cameraController.setImageAnalysisAnalyzer(cameraExecutor, imageAnalyzer)
//        cameraController.bindToLifecycle(this)
//        cameraPreviewView.controller = cameraController
//    }

    private fun allPermissionsGranted() = REQUIRED_PREMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = "LiveCameraFragment"
        private val REQUIRED_PREMISSIONS = mutableSetOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).toTypedArray()
    }

}