package com.github.ostap_stud.analysis

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

object CarLicenseDetector {

    lateinit var carInterpreter: Interpreter
    lateinit var licInterpreter: Interpreter
    val licNumRecognizer: LicenseNumberRecognizer = LicenseNumberRecognizer()
    private val inputSize: Int = 640
    private val confThreshold: Float = 0.4f

    private val TAG = "CarLicenseDetector"
    val CAR_MODEL_FILENAME = "yolo11n_float32.tflite"
    val LICENSE_MODEL_FILENAME = "license_plate_model_float32.tflite"
    val LABELS = mapOf(0 to "License Plate", 2 to "Car")

    fun createInterpreter(context: Context, assetName: String): Interpreter {
        val modelBuffer = FileUtil.loadMappedFile(context, assetName)
        val options = Interpreter.Options()
        options.setNumThreads(4)
//             options.addDelegate(GpuDelegate()) // optional: add GPU delegate if desired and available
        return Interpreter(modelBuffer, options)
    }

    fun process(
        image: Bitmap,
        onCompleteFunction: (List<Detection>, List<LicenseDetection>, InputInterpreter) -> Unit
    ){
        val prep = InputPreprocessor.preprocess(image, inputSize)

        val carOutput = Array(1) { Array(84) { FloatArray(8400) } }
        carInterpreter.run(prep.byteBuffer, carOutput)

        Log.d("RAW", "CAR: cx=${carOutput[0][0][0]}, cy=${carOutput[0][1][0]}, w=${carOutput[0][2][0]}, h=${carOutput[0][3][0]}")

        val carDet = OutputDecoder.decode(carOutput, confThreshold, 2)
        val carNms = NonMaximumSuppression.nms(carDet)
        val cars = rescaleDetections(carNms, prep)

        val lpOutput = Array(1) { Array(5) { FloatArray(8400) } }
        licInterpreter.run(prep.byteBuffer, lpOutput)

        Log.d("RAW", "LIC: cx=${lpOutput[0][0][0]}, cy=${lpOutput[0][1][0]}, w=${lpOutput[0][2][0]}, h=${lpOutput[0][3][0]}, cls?=${lpOutput[0][4][0]}")

        val lpDet = OutputDecoder.decode(lpOutput, confThreshold, 0)
        val lpNms = NonMaximumSuppression.nms(lpDet)
        val plates = rescaleDetections(lpNms, prep)

        Log.d(TAG, "Found CARs: $cars")
        Log.d(TAG, "Found LICENSES: $plates")

        val licDetections = licNumRecognizer.process(image, plates)

        onCompleteFunction(cars, licDetections, prep)
    }

    private fun rescaleDetections(list: List<Detection>, prep: InputInterpreter): List<Detection> {
        return list.map { det ->
            var x1 = ((det.x1 * prep.inputW) - prep.dx) / prep.scale
            var y1 = ((det.y1 * prep.inputH) - prep.dy) / prep.scale
            var x2 = ((det.x2 * prep.inputW) - prep.dx) / prep.scale
            var y2 = ((det.y2 * prep.inputH) - prep.dy) / prep.scale

            x1 = x1.coerceIn(0f, prep.imageW.toFloat())
            y1 = y1.coerceIn(0f, prep.imageH.toFloat())
            x2 = x2.coerceIn(0f, prep.imageW.toFloat())
            y2 = y2.coerceIn(0f, prep.imageH.toFloat())

            det.copy(x1 = x1, y1 = y1, x2 = x2, y2 = y2)
        }
    }

}