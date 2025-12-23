package com.github.ostap_stud.analysis

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.TimeUnit
import kotlin.math.max

class LicenseNumberRecognizer {

    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun process(
        inputImage: Bitmap,
        detections: List<Detection>
    ): List<LicenseDetection> {
        return detections.map { det ->
            val licDetection = LicenseDetection(
                det.x1, det.y1, det.x2, det.y2, det.score, det.cls
            )
            val licenseCropped = cropBitmap(
                inputImage, det.x1.toInt(), det.y1.toInt(),
                (det.x2 - det.x1).toInt(), (det.y2 - det.y1).toInt()
            )
            val image = InputImage.fromBitmap(licenseCropped, 0)

            try {
                val result = Tasks.await(textRecognizer.process(image), 500, TimeUnit.MILLISECONDS)
                licDetection.numberText = result.text.trim()
            } catch (e: Exception){
                Log.e("LicenseNumberRecognizer", "Error recognizing text: ${e.message}")
                licDetection.numberText = ""
            }

            licDetection
        }
    }

    private fun cropBitmap(src: Bitmap, x: Int, y: Int, width: Int, height: Int, inputImageSize: Float = 32f): Bitmap{
        val licCropped = Bitmap.createBitmap(src, x, y, width, height)
        return if (width < inputImageSize || height < inputImageSize){
            val scale = max(inputImageSize / width, inputImageSize / height)
            val newW = (scale * width).toInt()
            val newH = (scale * height).toInt()
            Bitmap.createScaledBitmap(licCropped, newW, newH, true)
        } else {
            licCropped
        }

    }

}