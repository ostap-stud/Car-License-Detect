package com.github.ostap_stud.analysis

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.github.ostap_stud.ui.live.OnObjectsDetectListener

class CarLicenseImageAnalyzer(
    private val onObjectsDetectListener: OnObjectsDetectListener
) : ImageAnalysis.Analyzer{

    /*private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()*/

    /*@OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val bitmap = image.toBitmap()

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        val processedImage = imageProcessor.process(tensorImage)
        val inputBuffer = processedImage.buffer

        Log.d(TAG, "Prepared input buffer: size=${inputBuffer.capacity()}")

        image.close()
    }*/

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {

        val bmp = InputPreprocessor.rotateBitmap(
            image.toBitmap(),
            image.imageInfo.rotationDegrees.toFloat()
        )

        CarLicenseDetector.process(bmp) { det, licDet, prep ->
            onObjectsDetectListener.onObjectsDetected(
                det, licDet, prep.imageW.toFloat(), prep.imageH.toFloat()
            )
        }

        image.close()
    }

}