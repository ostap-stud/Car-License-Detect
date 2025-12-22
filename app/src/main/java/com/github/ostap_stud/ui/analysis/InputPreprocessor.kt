package com.github.ostap_stud.ui.analysis

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

data class InputInterpreter(
    val byteBuffer: ByteBuffer,
    val scale: Float,
    val dx: Int,
    val dy: Int,
    val inputW: Int,
    val inputH: Int,
    val imageW: Int,
    val imageH: Int
)

object InputPreprocessor {

    fun preprocess(bitmap: Bitmap, size: Int = 640): InputInterpreter {
        val w = bitmap.width
        val h = bitmap.height

        val scale = min(size.toFloat() / w, size.toFloat() / h)

        val newW = (w * scale).toInt()
        val newH = (h * scale).toInt()

        val dx = (size - newW) / 2
        val dy = (size - newH) / 2

        val resized = Bitmap.createScaledBitmap(bitmap, newW, newH, true)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(resized, dx.toFloat(), dy.toFloat(), null)

        val bb = ByteBuffer.allocateDirect(1 * size * size * 3 * 4)
        bb.order(ByteOrder.nativeOrder())

        val pixels = IntArray(size * size)
        output.getPixels(pixels, 0, size, 0, 0, size, size)

        for (i in pixels.indices) {
            val c = pixels[i]
            bb.putFloat(((c shr 16) and 0xFF) / 255f)
            bb.putFloat(((c shr 8) and 0xFF) / 255f)
            bb.putFloat((c and 0xFF) / 255f)
        }

        bb.rewind()

        return InputInterpreter(
            byteBuffer = bb,
            scale = scale,
            dx = dx,
            dy = dy,
            inputW = size,
            inputH = size,
            imageW = w,
            imageH = h
        )
    }

    fun rotateBitmap(source: Bitmap, degree: Float): Bitmap{
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(degree)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, rotationMatrix, true)
    }

}