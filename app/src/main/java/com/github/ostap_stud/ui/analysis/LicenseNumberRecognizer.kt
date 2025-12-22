package com.github.ostap_stud.ui.analysis

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class LicenseNumberRecognizer {

    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun process(
        inputImage: Bitmap,
        detections: List<Detection>,
        onNumberRecognition: (licDetection: LicenseDetection, numberRecognized: String) -> Unit,
    ): List<LicenseDetection>{
        return detections.map { det ->
            val licDetection = LicenseDetection(
                det.x1, det.y1, det.x2, det.y2, det.score, det.cls
            )
            val licenseCropped = Bitmap.createBitmap(
                inputImage, det.x1.toInt(), det.y1.toInt(), (det.x2 - det.x1).toInt(), (det.y2 - det.y1).toInt()
            )
            val image = InputImage.fromBitmap(licenseCropped, 0)

            textRecognizer.process(image)
                .addOnSuccessListener { result ->
                    onNumberRecognition(licDetection, result.text)
                }

            licDetection
        }
    }

}