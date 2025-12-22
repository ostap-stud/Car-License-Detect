package com.github.ostap_stud.ui.analysis

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.github.ostap_stud.ui.live.OnObjectsDetectListener
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp

class CarLicenseImageAnalyzer(
    private val carInterpreter: Interpreter,
    private val licInterpreter: Interpreter,
    private val ocr: Interpreter,
    private val licNumRecognizer: LicenseNumberRecognizer,
    private val inputSize: Int = 640,
    private val confThreshold: Float = 0.4f,
    private val onObjectsDetectListener: OnObjectsDetectListener
) : ImageAnalysis.Analyzer{

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(640, 640, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(0f, 255f))
        .build()

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
     
        val prep = InputPreprocessor.preprocess(bmp, inputSize)

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

        Log.d("ROSETTA", ocr.getInputTensor(0).shape().toTypedArray().contentDeepToString())
        Log.d("ROSETTA", ocr.getOutputTensor(0).shape().toTypedArray().contentDeepToString())

        val licenseDetections = plates.map { plate ->
            val plateCropped = Bitmap.createBitmap(
                bmp, plate.x1.toInt(), plate.y1.toInt(),
                (plate.x2 - plate.x1).toInt(), (plate.y2 - plate.y1).toInt()
            )
//            val input = InputPreprocessor.preprocess(plateCropped, 100, 32)
//            val output = Array(1) { Array(26) { FloatArray(37) } }

            val input = InputPreprocessor.preprocess(plateCropped, 1000, 64)
            val output = Array(1) { Array(249) { FloatArray(97) } }

            ocr.run(input, output)
            val numberRecognized = OutputDecoder.ctcDecode(output)
            LicenseDetection(plate.x1, plate.y1, plate.x2, plate.y2, plate.score, plate.cls, numberRecognized)
        }



        /*val licenseDetections = licNumRecognizer.process(bmp, plates){ licDetection, numRecognized ->
            licDetection.numberText = numRecognized
        }*/

        onObjectsDetectListener.onObjectsDetected(
            cars, licenseDetections,
            prep.imageW.toFloat(), prep.imageH.toFloat()
        )

        image.close()
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


    companion object{
        private val TAG = "CarLicenseDetection"
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
    }
}